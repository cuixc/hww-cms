package com.hww.cms.common.dto;

import com.hww.base.common.dto.AbsBaseDto;

/**
 * @Author: zhaoke
 * @Description:  cms后台管理，查询分类树专用DTO
 */
public class AllCategoryTreeDto extends AbsBaseDto<Long> {
    private Long categoryId;//分类ID
    private Long parentId;//父类ID
    private String categoryName;//分类名称

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }


}
