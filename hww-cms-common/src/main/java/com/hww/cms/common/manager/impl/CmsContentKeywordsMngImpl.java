package com.hww.cms.common.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hww.base.common.manager.impl.BaseEntityMngImpl;
import com.hww.base.util.TimeUtils;
import com.hww.cms.common.dao.CmsContentKeywordsDao;
import com.hww.cms.common.entity.CmsContentKeywords;
import com.hww.cms.common.manager.CmsContentKeywordsMng;


@Service("cmsContentKeywordsMng")
public class CmsContentKeywordsMngImpl extends BaseEntityMngImpl<Long, CmsContentKeywords, CmsContentKeywordsDao> 
	implements CmsContentKeywordsMng {

	@Autowired
	CmsContentKeywordsDao cmsContentKeywordsDao;
	
	@Autowired
	public void setCmsContentKeywordsDao(CmsContentKeywordsDao cmsContentKeywordsDao) {
		super.setEntityDao(cmsContentKeywordsDao);
		this.cmsContentKeywordsDao = cmsContentKeywordsDao;
	}
	
	@Override
	public List<CmsContentKeywords> queryCmsContentKeywords(String contentKeyword) {
		
		return cmsContentKeywordsDao.queryCmsContentKeywords(contentKeyword);
	}

	@Override
	public void insertContentKeyword(String strKeyword) {
		if(!StringUtils.hasText(strKeyword)) {
			return;
		}
		boolean cb =  cmsContentKeywordsDao.searchKeywordExits(strKeyword.trim());
		if(cb==true) {
			return;
		}
		CmsContentKeywords entity = new CmsContentKeywords();
		entity.setKeywordContent(strKeyword);
		entity.setStatus(Short.valueOf("1"));
		entity.setCreateTime(TimeUtils.getDateToTimestamp());
		entity.setLastModifyTime(TimeUtils.getDateToTimestamp());
		cmsContentKeywordsDao.save(entity);
	}

}
