package net.swantrip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

@Component
public class TuniuCrawler {

	private static Log log = LogFactory.getLog(TuniuCrawler.class);

	@Autowired
	private RestTemplate rest;

	private String host = "swantrip.net";

	public void crawl_all() {
		int page = 1;
		int pageSize = 50;

		int totalCount = getTotalCount();
		int pageCount = totalCount / pageSize + (totalCount % pageSize > 0 ? 1 : 0);
		while (page <= pageCount) {
			if (!crawl(page, pageSize)) {
				return;
			}
			page++;
		}
	}

	public boolean crawl(int page, int pageSize) {

		String listurl = getListUrl(page, pageSize);

		AjaxList list = rest.getForObject(listurl, AjaxList.class);
		// log.warn(list);

		for (Note note : list.getData().getRows()) {
			String originUrl = getDetailUrl(note.getId());
			note.setName(note.getName().replace("途牛", "天鹅之旅"));
			note.setSummary(note.getSummary().replace("途牛", "天鹅之旅"));
			note.setOriginUrl(originUrl);

			try {
				Document _doc = Jsoup.connect(originUrl).get();
				List<String> tags = _doc.select(".tag-item").stream().map(e -> e.text().replace("#", ""))
						.collect(Collectors.toList());
				Elements elements = _doc.select(".travel-main");
				elements.select("img").forEach(e -> {
					String src = e.attr("data-src");
					if (StringUtils.hasText(src)) {
						e.attr("src", src);
					}
				});
				String content = elements.outerHtml().replace("途牛", "天鹅之旅");

				note.setTags(tags);
				note.setContent(content);

				if (StringUtils.isEmpty(content)) {
					log.error("not found:" + originUrl);
					return false;
				}

				postToWp(note.getName(), note.getContent(), note.getPublishTime(), null, note.getTags());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public int getPageCount(int pageSize) {
		String listurl = getListUrl(1, pageSize);
		AjaxList list = rest.getForObject(listurl, AjaxList.class);
		return list.getData().getPageCount();
	}

	public int getTotalCount() {
		String listurl = getListUrl(1, 1);
		String str = rest.getForObject(listurl, String.class);
		AjaxList list = new Gson().fromJson(str, AjaxList.class);
		return list.getData().getTotalCount();
	}

	private String getListUrl(int page, int size) {
		String listUrl = "https://trips.tuniu.com/travels/index/ajax-list?page=%d&limit=%d";
		return String.format(listUrl, page, size);

	}

	private String getDetailUrl(long id) {
		String noteUrl = "https://www.tuniu.com/trips/%d";
		return String.format(noteUrl, id);
	}

	private void postToWp(//
			String title, //
			String c, //
			String date, //
			List<String> categories, //
			List<String> tags) {
		try {
			HttpHeaders h = new HttpHeaders();
			// 请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
			h.setContentType(MediaType.APPLICATION_JSON);
			h.add("Authorization", "Basic ZmFuZHl2b246MTIzNDU2Nw==");

			WpPost p = new WpPost();
			p.setTitle(title);
			p.setContent(c);
			p.setStatus("publish");
			p.setDate(date);

			if (categories != null) {
				List<Integer> categoriyList = new ArrayList<Integer>();
				for (String cate : categories) {
					categoriyList.add(categoryId(cate));
				}
				p.setCategories(categoriyList);
			}

			if (tags != null) {
				List<Integer> tagList = new ArrayList<Integer>();
				for (String t : tags) {
					try {
						tagList.add(tagId(t));
					} catch (Exception e) {
					}
				}
				p.setTags(tagList);
			}

			HttpEntity<WpPost> entity = new HttpEntity<WpPost>(p, h);
			// 执行HTTP请求
			ResponseEntity<String> res = rest.postForEntity( //
					"https://" + host + "/wp-json/wp/v2/posts", entity, String.class);
			log.info(res.getStatusCode());
//			log.info(res.getBody());
		} catch (HttpClientErrorException e) {
			log.error(e.getResponseBodyAsString());
//			log.error("date:" + date);
			log.error(e);
			e.printStackTrace();
		} catch (RestClientException e) {
			log.error("date:" + date);
			log.error(e);
			e.printStackTrace();
		}
	}

	private int tagId(String tag) {
		String res = null;
		try {
			HttpHeaders h = new HttpHeaders();
			// 请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
			h.setContentType(MediaType.APPLICATION_JSON);
			h.add("Authorization", "Basic ZmFuZHl2b246MTIzNDU2Nw==");

			WpCategoryTag wpTag = new WpCategoryTag();
			wpTag.setName(tag);

			HttpEntity<WpCategoryTag> entity = new HttpEntity<WpCategoryTag>(wpTag, h);
			// 执行HTTP请求
			res = rest.postForObject( //
					"https://" + host + "/wp-json/wp/v2/tags", entity, String.class);
			return new JSONObject(res).optInt("id");
		} catch (HttpClientErrorException e) {
			res = e.getResponseBodyAsString();
			try {
				return new JSONObject(res).getJSONObject("data").optInt("term_id");
			} catch (JSONException ee) {
				ee.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int categoryId(String category) {
		String res = null;
		try {
			HttpHeaders h = new HttpHeaders();
			// 请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
			h.setContentType(MediaType.APPLICATION_JSON);
			h.add("Authorization", "Basic ZmFuZHl2b246MTIzNDU2Nw==");

			WpCategoryTag wpCategory = new WpCategoryTag();
			wpCategory.setName(category);

			HttpEntity<WpCategoryTag> entity = new HttpEntity<WpCategoryTag>(wpCategory, h);
			// 执行HTTP请求
			res = rest.postForObject( //
					"https://" + host + "/wp-json/wp/v2/categories", entity, String.class);
			return new JSONObject(res).optInt("id");
		} catch (HttpClientErrorException e) {
			res = e.getResponseBodyAsString();
			try {
				return new JSONObject(res).getJSONObject("data").optInt("term_id");
			} catch (JSONException ee) {
				ee.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	class WpCategoryTag {
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	class WpPost {

		private String title;
		private String content;
		private String status;
		private String date;
		private List<Integer> categories; //
		private List<Integer> tags;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public List<Integer> getCategories() {
			return categories;
		}

		public void setCategories(List<Integer> categories) {
			this.categories = categories;
		}

		public List<Integer> getTags() {
			return tags;
		}

		public void setTags(List<Integer> tags) {
			this.tags = tags;
		}
	}

}
