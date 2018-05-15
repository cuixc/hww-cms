package com.hww.cms.webadmin.service;

import java.util.List;

import com.hww.cms.common.entity.CmsContentKeywords;

public interface CmsContentKeywordsService {
	
	List<CmsContentKeywords> queryCmsContentKeywords(String contentKeyword);
	
	void insertContentKeyword(String strKeyword);
}
