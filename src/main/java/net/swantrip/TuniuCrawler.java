package net.swantrip;

import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class TuniuCrawler {

	private static Log log = LogFactory.getLog(TuniuCrawler.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private NoteService noteService;

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

		AjaxList list = restTemplate.getForObject(listurl, AjaxList.class);
		log.warn(list);

		for (Note note : list.getData().getRows()) {
			String originUrl = getDetailUrl(note.getId());
			if (noteService.existsByOriginUrl(originUrl)) {
				continue;
			}
			note.setName(note.getName().replace("途牛", "天鹅之旅"));
			note.setSummary(note.getSummary().replace("途牛", "天鹅之旅"));
			note.setOriginUrl(originUrl);

			try {
				Document _doc = Jsoup.connect(originUrl).get();
				String tags = _doc.select(".tag-item").stream().map(e -> e.text().replace("#", ""))
						.collect(Collectors.joining(","));
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

				noteService.save(note);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public int getPageCount(int pageSize) {
		String listurl = getListUrl(1, pageSize);
		AjaxList list = restTemplate.getForObject(listurl, AjaxList.class);
		return list.getData().getPageCount();
	}

	public int getTotalCount() {
		String listurl = getListUrl(1, 1);
		AjaxList list = restTemplate.getForObject(listurl, AjaxList.class);
		return list.getData().getTotalCount();
	}

	private String getListUrl(int page, int size) {
		String listUrl = "http://trips.tuniu.com/travels/index/ajax-list?page=%d&limit=%d";
		return String.format(listUrl, page, size);

	}

	private String getDetailUrl(long id) {
		String noteUrl = "http://www.tuniu.com/trips/%d";
		return String.format(noteUrl, id);
	}

}
