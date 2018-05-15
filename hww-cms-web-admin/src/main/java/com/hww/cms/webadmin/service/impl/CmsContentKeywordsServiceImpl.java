package com.hww.cms.webadmin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.hww.cms.common.entity.CmsContentKeywords;
import com.hww.cms.common.manager.CmsContentKeywordsMng;
import com.hww.cms.webadmin.service.CmsContentKeywordsService;


@Service("cmsContentKeywordsService")
@Transactional
public class CmsContentKeywordsServiceImpl implements CmsContentKeywordsService {

	@Autowired
	private CmsContentKeywordsMng cmsContentKeywordsMng;
	@Override
	public List<CmsContentKeywords> queryCmsContentKeywords(String contentKeyword) {
		if(!StringUtils.hasText(contentKeyword)) {
			return Lists.newArrayList();
		}
		String keys=contentKeyword;
		if(keys.contains(",")) {
			keys=org.apache.commons.lang3.StringUtils.substringAfterLast(contentKeyword, ",");
		}
		
		if(!StringUtils.hasText(keys)) {
			return Lists.newArrayList();
		}
		List<CmsContentKeywords> list=cmsContentKeywordsMng.queryCmsContentKeywords(keys);
		return list==null?Lists.newArrayList():list;
	}
	
	@Override
	public void insertContentKeyword(String strKeyword) {

		if(!StringUtils.hasText(strKeyword)) {
			return;
		}
		
		String[] array=strKeyword.split(",");
		if(array!=null&&array.length>0) {
			for(String str: array) {
				cmsContentKeywordsMng.insertContentKeyword(str);
			}
		}
		
	}
	

}
