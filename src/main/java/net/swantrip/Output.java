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

	private String path = "C:\\uzblog\\swantrip\\";

	private String template = "---\r\n" + //
			"title: \"%s\"\r\n" + //
			"date: %s\r\n" + //
			"keywords: %s\r\n" + //
			"description: \"%s\"\r\n" + //
			"tags: %s\r\n" + //
			"categories: [\"游记\"]\r\n" + //
			"---\r\n\r\n" + //
			"%s";

	public void out() {
		for (Note note : noteService.list()) {
			String filepath = path + note.getId() + ".html";
			String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+08:00").format(note.getPublishTime());
			String tags = "[\"" + note.getTags().replace(",", "\",\"") + "\"]";
			String content = String.format(template, //
					note.getName(), //
					date, //
					tags, //
					note.getSummary(), //
					tags, //
					note.getContent());
			try {
				IOUtils.write(content, new FileOutputStream(filepath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
