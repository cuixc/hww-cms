package com.hww.cms.common.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.hww.base.common.manager.impl.BaseEntityMngImpl;
import com.hww.base.common.util.Finder;
import com.hww.cms.common.dao.CmsSpecialDao;
import com.hww.cms.common.entity.CmsSpecial;
import com.hww.cms.common.manager.CmsSpecialMng;
import com.hww.config.redis.cache.HwwRedisCache;

@Service("cmsSpecialMng")
@Transactional
public class CmsSpecialMngImpl extends BaseEntityMngImpl<Long, CmsSpecial, CmsSpecialDao> implements CmsSpecialMng {

	CmsSpecialDao cmsSpecialDao;
	@Autowired
	public void setCmsSpecialDao(CmsSpecialDao cmsSpecialDao) {
		super.setEntityDao(cmsSpecialDao);
		this.cmsSpecialDao = cmsSpecialDao;
	}

    @Autowired
    HwwRedisCache hwwRedisCache;
    
    private static final String cacheKeyFirstLeve="cms_loadFirstLeveSpecialList";
    private static final String cacheKeyNotTopLeve="cms_loadNotTopLeveSpecialList";
    private static final String cacheKeyByParentId="cms_loadByParentId";
    private static final String cacheKeyView="cms_loadSpecialView";
    
	 
	 @Scheduled(fixedDelay = 1000*60*10)
	 public void delete_from_cache() {
		 hwwRedisCache.evict(cacheKeyFirstLeve);
		 hwwRedisCache.evict(cacheKeyByParentId);
		 hwwRedisCache.evict(cacheKeyView);
		 hwwRedisCache.evict(cacheKeyNotTopLeve);
	 }
	 
	 
	@Override
	public List<CmsSpecial> loadFirstLeveSpecialList(Short status) {
//		Finder finder = Finder.create("from CmsSpecial where  parent is null");
//		if(status!=null) {
//			finder.append("and status=:status");
//			finder.setParam("status", status);
//		}
//		List<CmsSpecial> cmsSpecialList = (List<CmsSpecial>) cmsSpecialDao.find(finder);
//		return cmsSpecialList;
		
		//=========from cacahe--------------
		
		List<CmsSpecial> res =	hwwRedisCache.get(cacheKeyFirstLeve, cacheKeyFirstLeve+"_"+status, List.class);
		if(res==null) {
			Finder finder = Finder.create("from CmsSpecial where  parent is null");
			if(status!=null) {
				finder.append("and status=:status");
				finder.setParam("status", status);
			}
			res = (List<CmsSpecial>) cmsSpecialDao.find(finder);
			if(res==null) {
				return Lists.newArrayList();
			}
			 hwwRedisCache.put(cacheKeyFirstLeve, cacheKeyFirstLeve+"_"+status, res);
			return res;
		}
		return res;
		//=========from cacahe--------------
		
	}
	
	 
	@Override
	public  List<CmsSpecial> loadNotTopLeveSpecialList(Short status){
//		Finder finder = Finder.create("from CmsSpecial where  parent is not null ");
//		if(status!=null) {
//			finder.append("and status=:status");
//			finder.setParam("status", status);
//		}
//		List<CmsSpecial> cmsSpecialList = (List<CmsSpecial>) cmsSpecialDao.find(finder);
//		return cmsSpecialList;
		
		//=========from cacahe--------------
		List<CmsSpecial> res =	hwwRedisCache.get(cacheKeyNotTopLeve, cacheKeyNotTopLeve+"_"+status, List.class);
		if(res==null) {
			Finder finder = Finder.create("from CmsSpecial where  parent is not null ");
			if(status!=null) {
				finder.append("and status=:status");
				finder.setParam("status", status);
			}
			res =(List<CmsSpecial>) cmsSpecialDao.find(finder);
			if(res==null) {
				return Lists.newArrayList();
			}
			hwwRedisCache.put(cacheKeyNotTopLeve, cacheKeyNotTopLeve+"_"+status, res);
			return res;
		}
		return res;
		
		//=========from cacahe--------------
		
	}
	 
	@Override
	public List<CmsSpecial> loadByParentId(Long parentId){
		
//		return cmsSpecialDao.selectByParentId(parentId);
		
		//=========from cacahe--------------
		List<CmsSpecial> res =	hwwRedisCache.get(cacheKeyByParentId, cacheKeyByParentId+"_"+parentId, List.class);
		if(res==null) {
			res =cmsSpecialDao.selectByParentId(parentId);
			if(res==null) {
				return Lists.newArrayList();
			}
			hwwRedisCache.put(cacheKeyByParentId, cacheKeyByParentId+"_"+parentId, res);
			return res;
		}
		return res;
		//=========from cacahe--------------
	}


	@Override
	public List<CmsSpecial> getSpecialByJson(Long parentId) {
		List<CmsSpecial> list = cmsSpecialDao.getSpecialByJson(parentId);
		if (list == null) {
			return new ArrayList<CmsSpecial>();
		}
		return list;
	}


	/**
	 * 更改special状态 0：禁用 ，1：使用
	 * 
	 * @param specialId
	 *            id
	 * @param status
	 *            状态
	 * @return
	 */
	@Override
	public boolean updateSpecialStatus(Long specialId, Short status) {
		Finder hql = Finder.create("update");
		hql.append(CmsSpecial.class.getName());

		if (status != null) {
			hql.append(" set status=:statusP");
			hql.setParam("statusP", status);
		}

		hql.append(" where specialId=:specialIdP");
		hql.setParam("specialIdP", specialId);

		cmsSpecialDao.update(hql);
		
		delete_from_cache();
		
		return true;
	}


	 
	@Override
	public CmsSpecial loadSpecialView(Long specialId) {
		
//		return cmsSpecialDao.find(specialId);
		
		
		//=========from cacahe--------------
		CmsSpecial res =	hwwRedisCache.get(cacheKeyView, cacheKeyView+"_"+specialId, CmsSpecial.class);
		if(res==null) {
			res =cmsSpecialDao.find(specialId);
			if(res==null) {
				return res;
			}
			hwwRedisCache.put(cacheKeyView, cacheKeyView+"_"+specialId, res);
			return res;
		}
		return res;
		//=========from cacahe--------------
	}


	@Override
	public Boolean delete(CmsSpecial entity) {
		//=========from cacahe--------------
		delete_from_cache();
		//=========from cacahe--------------
		
		return super.delete(entity);
	}


	@Override
	public CmsSpecial save(CmsSpecial entity) {
		//=========from cacahe--------------
		delete_from_cache();
		//=========from cacahe--------------
		
		return super.save(entity);
	}


	@Override
	public CmsSpecial update(CmsSpecial entity) {
		//=========from cacahe--------------
		delete_from_cache();
		//=========from cacahe--------------
		
		return super.update(entity);
	}



}
