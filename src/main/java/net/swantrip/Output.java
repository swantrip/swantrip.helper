package net.swantrip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Output {

	@Autowired
	private NoteService noteService;

	private String path = "/Users/feng/workspace/swantrip/swantrip.net/source/_posts/";

	private String template = "---\r\n" + //
			"layout: post\r\n" + //
			"title: \"%s\"\r\n" + //
			"date: %s\r\n" + //
			"categories: [[\"游记\"]]\r\n" + //
			"tags: %s\r\n" + //
			"keywords: %s\r\n" + //
			"description: \"%s\"\r\n" + //
			"img: \"%s\"\r\n" + //
			"---\r\n\r\n" + //
			"%s";

	public void outHexo() {
		for (Note note : noteService.list()) {
			String filepath = path + note.getId() + ".html";
			String title = note.getName();
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(note.getPublishTime());
			String tags = "[[\"" + note.getTags().replace(",", "\"],[\"") + "\"]]";
			String keywords = note.getTags();
			String description = note.getSummary();
			String img = note.getPicUrl();

			String content = String.format(template, //
					title, //
					date, //
					tags, //
					keywords, //
					description, //
					img, //
					note.getContent());
			try {
				IOUtils.write(content, new FileOutputStream(filepath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
