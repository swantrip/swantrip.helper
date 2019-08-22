package net.swantrip;

import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TuniuCrawler {

	private static Log log = LogFactory.getLog(TuniuCrawler.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private NoteService noteService;

	public void crawl() {

		String listurl = getListUrl(1, 10);

		AjaxList list = restTemplate.getForObject(listurl, AjaxList.class);
		log.warn(list);

		for (Note note : list.getData().getRows()) {
			String originUrl = getDetailUrl(note.getId());
			if (noteService.existsByOriginUrl(originUrl)) {
				continue;
			}

			note.setOriginUrl(originUrl);

			try {
				Document _doc = Jsoup.connect(originUrl).get();
				String tags = _doc.select(".tag-item").stream().map(e -> e.text().replace("#", ""))
						.collect(Collectors.joining(","));
				String content = _doc.select(".travel-main").outerHtml();
				note.setTags(tags);
				note.setContent(content);

				noteService.save(note);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
