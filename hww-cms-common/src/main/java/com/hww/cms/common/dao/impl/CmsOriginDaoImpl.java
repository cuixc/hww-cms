package com.hww.cms.common.dao.impl;

import com.google.common.collect.Lists;
import com.hww.base.common.page.Pagination;
import com.hww.base.common.util.Finder;
import com.hww.base.util.StringUtils;
import com.hww.cms.common.dto.CmsOriginDto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hww.cms.common.dao.CmsOriginDao;
import com.hww.cms.common.entity.CmsOrigin;

import javax.persistence.EntityManager;

@Repository("cmsOriginDao")
public class CmsOriginDaoImpl extends LocalDataBaseDaoImpl<Long, CmsOrigin> implements CmsOriginDao {

	@Autowired
	private EntityManager entityManager;
	@Override
	public Pagination list(CmsOriginDto dto) {
		Finder finder = Finder.create("from CmsOrigin where 1=1");
		if (StringUtils.isNotBlank(dto.getOriginName())) {
			finder.append(" and originName=:originName").setParam("originName", dto.getOriginName());
		}
		if (StringUtils.isNotBlank(dto.getOriginUrl())) {
			finder.append(" and originUrl=:originUrl").setParam("originUrl", dto.getOriginUrl());
		}
		if (StringUtils.isNotBlank(dto.getLink())) {
			finder.append(" and link=:link").setParam("link", dto.getLink());
		}
		if (dto.getStatus() != null) {
			finder.append(" and status=:status").setParam("status", dto.getStatus());
		}
		Pagination p = find(finder, dto.getPageNo(), dto.getPageSize());
		return p;
	}

	@Override
	public List<CmsOrigin> listOrigin() {
		Finder finder = Finder.create("from CmsOrigin where 1=1");
		finder.append(" and status=:status").setParam("status",Short.valueOf("1"));
		List<CmsOrigin> list = (List<CmsOrigin>)find(finder);
		return list;
	}

	@Override
	public List<CmsOrigin> listCmsOriginByContentIds(List<Long> contentIds) {
		Finder finder=Finder.create("select b.origin_id as originId, b.origin_name as originName, b.origin_url as originUrl, b.site_id as siteId ,b.status as status, b.word as word ,b.link as link ");
		finder.append(" from cms_content_data a,cms_origin b where a.origin_id=b.origin_id and a.content_id in (:contentId)");
		finder.setParamList("contentId",contentIds);
		List<CmsOrigin> origins= (List<CmsOrigin>) findJoin(finder,CmsOrigin.class);
		if(origins==null)
			return Lists.newArrayList();
		return origins;
	}

	@Override
	public CmsOrigin getCmsOringByContentId(Long contentId) {
		Finder finder=Finder.create("select b.origin_id as originId, b.origin_name as originName, b.origin_url as originUrl, b.site_id as siteId ,b.status as status, b.word as word ,b.link as link ");
		finder.append(" from cms_content_data a,cms_origin b where a.origin_id=b.origin_id and a.content_id = :contentId");
		finder.setParam("contentId",contentId);
		List<CmsOrigin> origins= (List<CmsOrigin>) findJoin(finder,CmsOrigin.class);
		if(origins!=null && origins.size()>0){
			return origins.get(0);
		}
		return null;
	}
}
