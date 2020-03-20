package net.swantrip.chouti;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ChoutiCrawler {

	private static Log log = LogFactory.getLog(ChoutiCrawler.class);

	@Autowired
	private RestTemplate rest;

	private String host = "tumihua.cn";

	@SuppressWarnings("serial")
	Map<String, String> cates = new HashMap<String, String>() {
		{
			put("sh", "社会");
			put("gj", "世界");
			put("tech", "科技");
			put("ent", "娱乐");
			put("sport", "体育");
			put("finance", "财经");
			put("war", "军事");
		}
	};

	public void crawl_all() {
		cates.forEach((slug, category) -> {
			int page = 1;
			int pageCount = 40;
			while (page <= pageCount) {
				if (!crawl(category, slug, page)) {
					return;
				}
				page++;
			}
		});
	}

	public boolean crawl(String cateTxt, String category, int page) {

		String listurl = getListUrl(category, page);

		ChoutiList list = rest.getForObject(listurl, ChoutiList.class);
		// log.warn(list);

		for (ChoutiData item : list.getData()) {
			List<String> categories = new ArrayList<String>();
			categories.add(cateTxt);
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(item.getCreated_time() / 1000));
			System.out.println(date);

			postToWp(item.getTitle(), //
					item.getContent(), //
					item.getContent(), //
					item.getOriginal_img_url(), //
					item.getOriginalUrl(), //
					date, categories, null);
		}
		return true;
	}

	private String getListUrl(String category, int page) {
		String listUrl = "https://dig.chouti.com/category/list?category=%s&page=%d";
		return String.format(listUrl, category, page);

	}

	private void postToWp(//
			String title, //
			String content, //
			String excerpt, //
			String cover, //
			String linkto, //
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
			p.setContent(content);
			p.setStatus("publish");
			p.setDate(date);
			p.setExcerpt(excerpt);
			List<Map<String, String>> metadata = new ArrayList<Map<String, String>>();

			Map<String, String> coverMap = new HashMap<>();
			coverMap.put("key", "cover");
			coverMap.put("value", "https://uzshare.com/_p?" + cover);
			metadata.add(coverMap);

			Map<String, String> linktoMap = new HashMap<>();
			linktoMap.put("key", "linkto");
			linktoMap.put("value", linkto);
			metadata.add(linktoMap);

			p.setMetadata(metadata);

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
		private String excerpt;
		private List<Map<String, String>> metadata;

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

		public String getExcerpt() {
			return excerpt;
		}

		public void setExcerpt(String excerpt) {
			this.excerpt = excerpt;
		}

		public List<Map<String, String>> getMetadata() {
			return metadata;
		}

		public void setMetadata(List<Map<String, String>> metadata) {
			this.metadata = metadata;
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
