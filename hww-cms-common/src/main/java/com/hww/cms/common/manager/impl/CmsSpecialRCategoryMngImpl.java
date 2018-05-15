package com.hww.cms.common.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.hww.base.common.manager.impl.BaseEntityMngImpl;
import com.hww.cms.common.dao.CmsSpecialRCategoryDao;
import com.hww.cms.common.entity.CmsSpecialRCategory;
import com.hww.cms.common.manager.CmsSpecialRCategoryMng;
import com.hww.config.redis.cache.HwwRedisCache;

@Service("cmsSpecialRCategoryMng")
@Transactional
public class CmsSpecialRCategoryMngImpl extends BaseEntityMngImpl<Long, CmsSpecialRCategory, CmsSpecialRCategoryDao>
		implements CmsSpecialRCategoryMng {

	CmsSpecialRCategoryDao cmsSpecialRCategoryDao;

	@Autowired
	public void setCmsSpecialRCategoryDao(CmsSpecialRCategoryDao cmsSpecialRCategoryDao) {
		super.setEntityDao(cmsSpecialRCategoryDao);
		this.cmsSpecialRCategoryDao = cmsSpecialRCategoryDao;
	}

	@Autowired
	HwwRedisCache hwwRedisCache;

	private static final String cacheKey = "cms_loadCmsCateIdsBySpecialId";

	@Scheduled(fixedDelay = 1000 * 60 * 2)
	public void loadCountForNews_delete_from_cache() {
		hwwRedisCache.evict(cacheKey);
	}

	@Override
	public List<CmsSpecialRCategory> loadBySpecialId(Long specialId) {
		// if (specialId == null) {
		// return new ArrayList<CmsSpecialRCategory>();
		// }
		// return cmsSpecialRCategoryDao.selectBySpecialId(specialId);

		// =========from cacahe--------------
		if (specialId == null) {
			return new ArrayList<CmsSpecialRCategory>();
		}
		List<CmsSpecialRCategory> res = hwwRedisCache.get(cacheKey, cacheKey + "_" + specialId, List.class);
		if (res == null) {
			res = cmsSpecialRCategoryDao.selectBySpecialId(specialId);
			if (res == null) {
				return new ArrayList<CmsSpecialRCategory>();
			}
			hwwRedisCache.put(cacheKey, cacheKey + "_" + specialId, res);
			return res;
		}
		return res;
		// =========from cacahe--------------
	}

	@Override
	public List<Long> loadCmsCateIdsBySpecialId(Long specialId) {
		List<CmsSpecialRCategory> list = cmsSpecialRCategoryDao.selectBySpecialId(specialId);
		if (list != null && !list.isEmpty()) {
			return list.stream().map(val -> val.getCategoryId()).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	@Override
	public void deleteRelationBySpecialId(Long specialId) {
		hwwRedisCache.evict(cacheKey);
		cmsSpecialRCategoryDao.deleteRelationBySpecialId(specialId);

	}

	@Override
	public Boolean delete(CmsSpecialRCategory entity) {
		hwwRedisCache.evict(cacheKey);
		return super.delete(entity);
	}

	@Override
	public CmsSpecialRCategory save(CmsSpecialRCategory entity) {
		hwwRedisCache.evict(cacheKey);
		return super.save(entity);
	}

	@Override
	public CmsSpecialRCategory update(CmsSpecialRCategory entity) {
		hwwRedisCache.evict(cacheKey);
		return super.update(entity);
	}

}
