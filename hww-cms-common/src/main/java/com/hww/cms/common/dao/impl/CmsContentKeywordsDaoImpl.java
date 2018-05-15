package com.hww.cms.common.dao.impl;

import com.google.common.collect.Lists;
import com.hww.base.common.util.Finder;
import com.hww.cms.common.dao.CmsContentKeywordsDao;
import com.hww.cms.common.entity.CmsContentKeywords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;


@Repository("cmsContentKeywordsDao")
public class CmsContentKeywordsDaoImpl extends LocalDataBaseDaoImpl<Long, CmsContentKeywords> implements CmsContentKeywordsDao {
	
	@Autowired
	private EntityManager entityManager;
	@Override
	public List<CmsContentKeywords> queryCmsContentKeywords(String contentKeyword) {
		Finder finder = Finder.create("from CmsContentKeywords where 1=1");
		if(contentKeyword != null && !contentKeyword.equals("")) {
			finder.append(" and keywordContent like '%"+contentKeyword+"%'");
		}
		finder.setMaxResults(30);
		List<CmsContentKeywords> cmsConKeyList = (List<CmsContentKeywords>) find(finder);
		if (null == cmsConKeyList || cmsConKeyList.size() == 0) {
			
			return Lists.newArrayList();
		}
		return cmsConKeyList;
	}
	
	@Override
	public boolean searchKeywordExits(String strKeyword) {
		Finder finder = Finder.create("from CmsContentKeywords where ");
		finder.append("keywordContent=:keywordContent").setParam("keywordContent", strKeyword);
		List<CmsContentKeywords> contentList = (List<CmsContentKeywords>) find(finder);
		if (null == contentList || contentList.size() == 0) {
			return false;
		}else {
			return true;
		}
		
	}
	
}
