package com.hww.cms.common.dto;


import com.hww.base.common.dto.AbsBaseDto;

/**
 * @Author: zhaoke
 * @Description: cms后台管理，审核流程下拉列表专用DTO
 */
public class CmsContentAuditFlowDto extends AbsBaseDto<Long> {
    private Long flowId;
    private String flowName;
    private Short status;
    private String remark;
    private Integer siteId;

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }
}
