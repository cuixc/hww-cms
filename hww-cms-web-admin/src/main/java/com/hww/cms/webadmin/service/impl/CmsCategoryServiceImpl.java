package com.hww.cms.webadmin.service.impl;

import com.hww.base.common.util.Finder;
import com.hww.base.util.BeanMapper;
import com.hww.cms.common.dto.AllCategoryTreeDto;
import com.hww.cms.common.dto.CmsCategoryDto;
import com.hww.cms.common.entity.CmsCategory;
import com.hww.cms.common.manager.CmsCategoryMng;
import com.hww.cms.common.vo.CmsCategoryVo;
import com.hww.cms.webadmin.service.CmsCategoryService;
import com.hww.sys.api.SysFeignClient;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service("cmsCategoryService")
@Transactional
public class CmsCategoryServiceImpl implements CmsCategoryService {

    @Autowired
    CmsCategoryMng cmsCategoryMng;
    @Autowired
    SysFeignClient sysFeignClient;

    @Override
    public List<CmsCategory> querySpecialList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteCategory(CmsCategoryVo vo) {
        List<CmsCategory> list = new ArrayList<>();
        CmsCategory entity = cmsCategoryMng.find(vo.getCategoryId());
        list.add(entity);
        while (list.size() > 0) {
            CmsCategory cur = list.get(0);
            Finder finder = Finder.create("from CmsCategory");
            finder.append("where 1=1");
            finder.append("and parent.categoryId=:PId").setParam("PId", cur.getCategoryId());
            List<CmsCategory> l = (List<CmsCategory>) cmsCategoryMng.find(finder);
            list.addAll(l);
            cur.setDisplay(Short.valueOf("0"));
            cur.setStatus(Short.valueOf("-1"));
            cmsCategoryMng.update(cur);
            list.remove(0);
        }
        return false;
    }

    @Override
    public List<AllCategoryTreeDto> listCmsCategory(Long userId, Integer siteId, Short status) {
        List<AllCategoryTreeDto> allCategoryTreeDtos = cmsCategoryMng.getRetrievingFullTree(userId, siteId, status);
        return allCategoryTreeDtos;
    }

    @Override
    public CmsCategoryDto getCmsCategoryNodeDisplay(Long categoryId, String operate) {
        CmsCategoryDto cmsCategoryDto = new CmsCategoryDto();
        if ("v_add".equals(operate)) {
            CmsCategory parent = cmsCategoryMng.find(categoryId); // 查询父类名称用
            cmsCategoryDto.setParentId(parent.getCategoryId());
            cmsCategoryDto.setParentName(parent.getCategoryName());
        } else if("v_edit".equals(operate)){
            CmsCategory cmsCategory = cmsCategoryMng.find(categoryId);
            cmsCategoryDto = BeanMapper.map(cmsCategory,CmsCategoryDto.class);
            if(cmsCategory.getParent() != null){
                cmsCategoryDto.setParentId(cmsCategory.getParent().getCategoryId());
                cmsCategoryDto.setParentName(cmsCategory.getParent().getCategoryName());
            }
        }
        return cmsCategoryDto;
    }

    @Override
    public void saveCmsCategoryNode(CmsCategoryDto cmsCategoryDto,Boolean b) {
        CmsCategory entity = new CmsCategory();
        try {
            BeanUtils.copyProperties(entity, cmsCategoryDto);
            //TODO 暂时写死
            Integer siteId=1;
            CmsCategory parentCmsCategory = cmsCategoryMng.find(cmsCategoryDto.getParentId());
            entity.setSiteId(siteId);
            entity.setParent(parentCmsCategory);
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            entity.setRgt(Long.parseLong("3"));
            entity.setLft(Long.parseLong("2"));
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cmsCategoryMng.saveOrUpdateCategoryAndAndRelationshipWidthTpl(entity, cmsCategoryDto, b);
        CmsCategory parentCmsCategory = cmsCategoryMng.find(cmsCategoryDto.getParentId());
        String relatedCategoryId = parentCmsCategory.getRelatedCategoryId();
        CmsCategory parentCmsCategory_entity = cmsCategoryMng.find(entity.getCategoryId());
        relatedCategoryId = relatedCategoryId + "," + parentCmsCategory_entity.getCategoryId();
        parentCmsCategory_entity.setRelatedCategoryId(relatedCategoryId);
        cmsCategoryMng.saveOrUpdateCategoryAndAndRelationshipWidthTpl(parentCmsCategory_entity, cmsCategoryDto, b);
    }

    @Override
    public void updateCmsCategoryNode(CmsCategoryDto cmsCategoryDto, Boolean b) {
        CmsCategory entity = new CmsCategory();
        CmsCategory parent = cmsCategoryMng.find(cmsCategoryDto.getParentId());
        try {
            BeanUtils.copyProperties(entity, cmsCategoryDto);

            entity.setParent(parent);
            entity.setSiteId(1);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cmsCategoryMng.saveOrUpdateCategoryAndAndRelationshipWidthTpl(entity, cmsCategoryDto, b);
    }


}
