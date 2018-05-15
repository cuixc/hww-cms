package com.hww.cms.common.manager.impl;

import com.hww.base.common.page.Pagination;
import com.hww.base.util.BeanMapper;
import com.hww.base.util.TimeUtils;
import com.hww.cms.common.dto.CmsOriginDto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.hww.base.common.manager.impl.BaseEntityMngImpl;
import com.hww.cms.common.dao.CmsOriginDao;
import com.hww.cms.common.entity.CmsOrigin;
import com.hww.cms.common.manager.CmsOriginMng;
import com.hww.config.redis.cache.HwwRedisCache;

@Service("cmsOriginMng")
public class CmsOriginMngImpl extends BaseEntityMngImpl<Long, CmsOrigin, CmsOriginDao>
        implements CmsOriginMng {

    @Autowired
    CmsOriginDao cmsOriginDao;
    @Autowired
    HwwRedisCache hwwRedisCache;

    private static final String cacahKey = "cms_getCmsOringByContentId";

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void loadCountForNews_delete_from_cache() {
        hwwRedisCache.evict(cacahKey);
    }

    public CmsOriginDao getCmsOriginDao() {
        return cmsOriginDao;
    }

    @Autowired
    public void setCmsOriginDao(CmsOriginDao cmsOriginDao) {
        super.setEntityDao(cmsOriginDao);
        this.cmsOriginDao = cmsOriginDao;
    }

    @Override
    public Pagination list(CmsOriginDto dto) {
        return cmsOriginDao.list(dto);
    }

    @Override
    public void saveOrigin(CmsOriginDto dto) {
        CmsOrigin cmsOrigin = BeanMapper.map(dto, CmsOrigin.class);
        cmsOrigin.setCreateTime(TimeUtils.getDateToTimestamp());
        cmsOrigin.setLastModifyTime(TimeUtils.getDateToTimestamp());
        cmsOriginDao.save(cmsOrigin);
        hwwRedisCache.evict(cacahKey, cacahKey + "_" + dto.getOriginId());
    }

    @Override
    public Boolean delete(CmsOrigin entity) {
        hwwRedisCache.evict(cacahKey, cacahKey + "_" + entity.getOriginId());
        return super.delete(entity);
    }

    @Override
    public void updateOrigin(CmsOriginDto dto) {
        CmsOrigin cmsOrigin = find(dto.getOriginId());
        cmsOrigin.setIcon(dto.getIcon());
        cmsOrigin.setLink(dto.getLink());
        cmsOrigin.setWord(dto.getWord());
        cmsOrigin.setOriginName(dto.getOriginName());
        cmsOrigin.setOriginUrl(dto.getOriginUrl());
        cmsOrigin.setStatus(dto.getStatus());
        cmsOrigin.setLastModifyTime(dto.getLastModifyTime());
        cmsOriginDao.update(cmsOrigin);

        hwwRedisCache.evict(cacahKey, cacahKey + "_" + dto.getOriginId());
    }

    @Override
    public List<CmsOrigin> listCmsOriginByContentIds(List<Long> contentIds) {
        return cmsOriginDao.listCmsOriginByContentIds(contentIds);
    }

    @Override
    public List<CmsOrigin> listOrigin() {

        return cmsOriginDao.listOrigin();
    }

    @Override
    public CmsOriginDto getCmsOringByContentId(Long contentId) {

        CmsOriginDto res = hwwRedisCache.get(cacahKey, cacahKey + "_" + contentId, CmsOriginDto.class);
        if (res == null) {
            CmsOrigin cmsOrigin = cmsOriginDao.getCmsOringByContentId(contentId);
            res = BeanMapper.map(cmsOrigin, CmsOriginDto.class);
            if (res == null) {
                return res;
            }
            hwwRedisCache.put(cacahKey, cacahKey + "_" + contentId, res);
            return res;
        }
        return res;
    }
}
