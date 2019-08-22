package net.swantrip;

@lombok.Data
public class AjaxList {
	private boolean success;
	private Data data;
}

@lombok.Data
class Data {
	private int pageCount;
	private int totalCount;
	private Note[] rows;
}
