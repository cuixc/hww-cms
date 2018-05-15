package com.hww.cms.common.manager.impl;

import com.hww.base.common.manager.impl.BaseEntityMngImpl;
import com.hww.base.common.util.Finder;
import com.hww.cms.common.dao.CmsContentAuditFlowStepsDao;
import com.hww.cms.common.entity.CmsContentAuditFlowSteps;
import com.hww.cms.common.manager.CmsContentAuditFlowStepsMng;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("cmsContentAuditFlowStepsMng")
@Transactional
public class CmsContentAuditFlowStepsMngImpl
		extends BaseEntityMngImpl<Long, CmsContentAuditFlowSteps, CmsContentAuditFlowStepsDao>
		implements CmsContentAuditFlowStepsMng {

	CmsContentAuditFlowStepsDao cmsContentAuditFlowStepsDao;

	public CmsContentAuditFlowStepsDao getCmsContentAuditFlowStepsDao() {
		return cmsContentAuditFlowStepsDao;
	}

	@Autowired
	public void setCmsContentAuditFlowStepsDao(CmsContentAuditFlowStepsDao cmsContentAuditFlowStepsDao) {
		super.setEntityDao(cmsContentAuditFlowStepsDao);
		this.cmsContentAuditFlowStepsDao = cmsContentAuditFlowStepsDao;
	}

	@Override
	public List<CmsContentAuditFlowSteps> getList(Long flowId) {
		// TODO Auto-generated method stub
		return cmsContentAuditFlowStepsDao.getList(flowId);
	}

	@Override
	public List<?> findSql(Finder finder,Class<?> o) {
		// TODO Auto-generated method stub
		return cmsContentAuditFlowStepsDao.findJoin(finder, o);
	}

}
