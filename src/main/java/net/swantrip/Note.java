package net.swantrip;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "note")
public class Note implements Serializable {

	private static final long serialVersionUID = 7144425803920583495L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 标题
	 */
	@Column(name = "name")
	private String name;

	/**
	 * 摘要
	 */
	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "publish_time")
	private Date publishTime;

	@Column(name = "pic_url", length = 2048)
	private String picUrl;

	/**
	 * 逗号分隔
	 */
	@Column(name = "tags")
	private String tags;

	/**
	 * 内容
	 */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "content")
	private String content;

	/**
	 * 源地址
	 */
	@Column(name = "origin_url", length = 2048)
	private String originUrl;

}