package com.hww.cms.common.vo;

import com.hww.base.common.vo.IBaseVo;

/**
 * @Author: zhaoke
 * @Description: cms后台管理：审核流程下拉列表专用VO
 */
public class SelectAuditFlowVo implements IBaseVo {
    private Long flowId;

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

    private String flowName;
}
