package com.hww.cms.common.manager;

import java.util.List;

import com.hww.base.common.manager.IBaseEntityMng;
import com.hww.cms.common.dao.CmsContentKeywordsDao;
import com.hww.cms.common.entity.CmsContentKeywords;

public interface CmsContentKeywordsMng extends IBaseEntityMng<Long, CmsContentKeywords, CmsContentKeywordsDao>{
	
	List<CmsContentKeywords> queryCmsContentKeywords(String contentKeyword);
	
	void insertContentKeyword(String strKeyword);

}
