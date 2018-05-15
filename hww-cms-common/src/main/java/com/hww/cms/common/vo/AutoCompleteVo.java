package com.hww.cms.common.vo;

import java.io.Serializable;
import java.util.List;

public class AutoCompleteVo implements Serializable{

	
	private List<String> title;

	public List<String> getTitle() {
		return title;
	}

	public void setTitle(List<String> title) {
		this.title = title;
	}
	
}
