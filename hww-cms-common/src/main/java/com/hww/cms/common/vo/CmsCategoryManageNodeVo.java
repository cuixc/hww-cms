package com.hww.cms.common.vo;

import com.hww.base.common.vo.IBaseVo;

import java.util.List;

/**
 * @Author: zhaoke
 * @Description: cms新闻分类管理模块VO
 */
public class CmsCategoryManageNodeVo implements IBaseVo {
    private Long parentId; // 父类ID
    private String parentName; // 父类分类名称
    private Long flowId; // 审核流程
    private Long categoryId; // 分类ID
    private String categoryName; // 分类名称
    private Short status; // 状态
    private Integer priority = 0; // 显示顺序
    private Short display; // 是否显示
    private String outLink; // 外链- 当typeId为外部链接时
    private String keywords; // 关键词
    private Integer siteId; // 所属站点
    private String description; // 描述
    private String operate; // v_add,v_edit

    private List<SelectSiteListVo> selectSiteListVos; // 站点下拉列表
    private List<SelectAuditFlowVo> selectAuditFlowVos; // 审核流程下拉列表

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Short getDisplay() {
        return display;
    }

    public void setDisplay(Short display) {
        this.display = display;
    }

    public String getOutLink() {
        return outLink;
    }

    public void setOutLink(String outLink) {
        this.outLink = outLink;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public List<SelectSiteListVo> getSelectSiteListVos() {
        return selectSiteListVos;
    }

    public void setSelectSiteListVos(List<SelectSiteListVo> selectSiteListVos) {
        this.selectSiteListVos = selectSiteListVos;
    }

    public List<SelectAuditFlowVo> getSelectAuditFlowVos() {
        return selectAuditFlowVos;
    }

    public void setSelectAuditFlowVos(List<SelectAuditFlowVo> selectAuditFlowVos) {
        this.selectAuditFlowVos = selectAuditFlowVos;
    }
}
