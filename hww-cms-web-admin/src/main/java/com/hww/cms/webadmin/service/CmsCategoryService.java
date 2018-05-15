package com.hww.cms.webadmin.service;

import com.hww.cms.common.dto.AllCategoryTreeDto;
import com.hww.cms.common.dto.CmsCategoryDto;
import com.hww.cms.common.entity.CmsCategory;
import com.hww.cms.common.vo.CmsCategoryVo;

import java.util.List;

public interface CmsCategoryService {


    List<CmsCategory> querySpecialList();

    boolean deleteCategory(CmsCategoryVo vo);

    List<AllCategoryTreeDto> listCmsCategory(Long userId, Integer siteId, Short status);

    CmsCategoryDto getCmsCategoryNodeDisplay(Long categoryId,String operate);

    void saveCmsCategoryNode(CmsCategoryDto cmsCategoryDto,Boolean b);

    void updateCmsCategoryNode(CmsCategoryDto cmsCategoryDto, Boolean b);
}
