package net.swantrip.chouti;

import java.util.Date;

import lombok.Data;

@Data
public class ChoutiList {
	private boolean success;
	private int code;
	private ChoutiData[] data;
}

@Data
class ChoutiData {
	private String title;
	private String img_url;
	private String original_img_url;
	private String originalUrl;
	private long created_time;
	private String domain;
	private String content;
}
