package com.hww.cms.common.manager;

import com.hww.base.common.manager.IBaseEntityMng;
import com.hww.cms.common.dao.CmsContentAuditFlowDao;
import com.hww.cms.common.dto.CmsContentAuditFlowDto;
import com.hww.cms.common.entity.CmsContentAuditFlow;

import java.util.List;

public interface CmsContentAuditFlowMng extends IBaseEntityMng<Long, CmsContentAuditFlow,CmsContentAuditFlowDao> {

	
	/**
	 * 获取审核流程列表
	 * @param vo vo参数
	 * @return 审核流程列表
	 */
	public List<CmsContentAuditFlowDto> getList(CmsContentAuditFlowDto cmsContentAuditFlowDto) ;
	
	public void saveEntity(CmsContentAuditFlow entity);
}
