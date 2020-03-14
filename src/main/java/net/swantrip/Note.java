package net.swantrip;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Note implements Serializable {

	private static final long serialVersionUID = 7144425803920583495L;

	private Long id;

	/**
	 * 标题
	 */
	private String name;

	/**
	 * 摘要
	 */
	private String summary;

	private String publishTime;

	private String picUrl;

	private List<String> tags;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 源地址
	 */
	private String originUrl;

}