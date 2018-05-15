package com.hww.cms.common.dao;

import java.util.List;

import com.hww.base.common.dao.IBaseEntityDao;
import com.hww.cms.common.entity.CmsContentKeywords;

public interface CmsContentKeywordsDao extends IBaseEntityDao<Long, CmsContentKeywords>{
	
	List<CmsContentKeywords> queryCmsContentKeywords(String contentKeyword);
	
	
	boolean searchKeywordExits(String strKeyword);
}
