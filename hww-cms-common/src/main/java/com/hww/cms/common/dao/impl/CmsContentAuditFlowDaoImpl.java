package com.hww.cms.common.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hww.base.common.util.Finder;
import com.hww.cms.common.dao.CmsContentAuditFlowDao;
import com.hww.cms.common.entity.CmsCategory;
import com.hww.cms.common.entity.CmsContentAuditFlow;
import com.hww.cms.common.vo.CmsContentAuditFlowVo;

@Repository("cmsContentAuditFlowDao")
public class CmsContentAuditFlowDaoImpl extends LocalDataBaseDaoImpl<Long, CmsContentAuditFlow>
		implements CmsContentAuditFlowDao {

	@Override
	public List<CmsContentAuditFlow> getList(CmsContentAuditFlow cmsContentAuditFlow) {
		// TODO Auto-generated method stub
		Finder f = Finder.create("from CmsContentAuditFlow bean");
		f.append(" where 1=1 and bean.status=1");
		if (cmsContentAuditFlow.getFlowId() != null) {
			String s = " and bean.flowId =" + cmsContentAuditFlow.getFlowId();
			f.append(s);
		}

		if (cmsContentAuditFlow.getFlowName() != null) {
			String s = " and bean.flowName like '%" + cmsContentAuditFlow.getFlowName() + "%'";
			f.append(s);
		}

		if (cmsContentAuditFlow.getRemark() != null) {
			String s = " and bean.remark like '%" + cmsContentAuditFlow.getRemark() + "%'";
			f.append(s);
		}

		f.append(" order by bean.createTime desc,bean.lastModifyTime desc");
		return (List<CmsContentAuditFlow>) find(f);
	}

}
