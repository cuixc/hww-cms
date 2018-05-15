package com.hww.cms.webadmin.service.impl;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import com.hww.base.common.page.Pagination;
import com.hww.base.common.util.Finder;
import com.hww.base.util.BeanMapper;
import com.hww.base.util.R;
import com.hww.cms.common.entity.CmsCategory;
import com.hww.cms.common.entity.CmsContent;
import com.hww.cms.common.entity.CmsContentAudit;
import com.hww.cms.common.entity.CmsContentAuditFlowSteps;
import com.hww.cms.common.entity.CmsContentData;
import com.hww.cms.common.entity.CmsContentType;
import com.hww.cms.common.manager.CmsCategoryMng;
import com.hww.cms.common.manager.CmsContentAuditFlowStepsMng;
import com.hww.cms.common.manager.CmsContentAuditMng;
import com.hww.cms.common.manager.CmsContentDataMng;
import com.hww.cms.common.manager.CmsContentMng;
import com.hww.cms.common.manager.CmsContentTypeMng;
import com.hww.cms.common.util.EsContentCovertUtil;
import com.hww.cms.common.vo.CmsContentAuditVo;
import com.hww.cms.common.vo.CmsContentVo;
import com.hww.cms.webadmin.service.CmsRewriteHtml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hww.cms.webadmin.service.CmsContentAuditService;
import com.hww.cms.webadmin.service.CmsNewEsSyncFailService;
import com.hww.els.api.ElsFeignClient;
import com.hww.els.common.entity.ESContent;
import com.hww.file.api.FileFeignClient;
import com.hww.framework.common.exception.HServiceLogicException;
import com.hww.sys.api.SysFeignClient;
import com.hww.sys.common.dto.SysRoleDto;
import com.hww.sys.common.dto.SysUserDto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service("cmsContentAuditService")
@Transactional
public class CmsContentAuditServiceImpl implements CmsContentAuditService {

	@Autowired
	CmsContentAuditMng cmsContentAuditMng;

	@Autowired
	CmsContentMng cmsContentMng;

	@Autowired
	CmsContentAuditFlowStepsMng cmsContentAuditFlowStepsMng;

	@Autowired
	CmsContentDataMng cmsContentDataMng;

	@Autowired
	CmsCategoryMng cmsCategoryMng;
	@Autowired
	private ElsFeignClient elsFeignClient;

	@Autowired
	private CmsContentTypeMng cmsContentTypeMng;

	@Autowired
	FileFeignClient fileFeignClient;

	@Autowired
	private CmsRewriteHtml rewriteHtml;

	@Autowired
	private CmsNewEsSyncFailService cmsNewEsSyncFailService;
	@Resource
	private SysFeignClient sysFeignClient;

	@Override
	public void save(CmsContentAuditVo vo) {
		vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		vo.setLastModifyTime(new Timestamp(System.currentTimeMillis()));
		vo.setStatus(Short.valueOf("1"));
		CmsContentAudit entity = BeanMapper.map(vo, CmsContentAudit.class);
		cmsContentAuditMng.save(entity);

	}

	@Override
	public CmsContentAuditVo findByContentIdAndCategoryId(Long ContentId, Long CategortId) {
		Finder finder = Finder.create("from CmsContentAudit where 1=1");
		finder.append(" and ContentId=:ContentId").setParam("ContentId", ContentId);
		finder.append(" and CategortId=:CategortId").setParam("CategortId", CategortId);
		CmsContentAudit entity = (CmsContentAudit) cmsContentAuditMng.find(finder);
		CmsContentAuditVo vo = BeanMapper.map(entity, CmsContentAuditVo.class);
		return vo;
	}

	@Override
	public void ContentAuditupdate(CmsContentAuditVo vo) {
		CmsContentAudit entity = BeanMapper.map(vo, CmsContentAudit.class);
		cmsContentAuditMng.update(entity);
	}

	@Override
	public Pagination getMyAuditContent(CmsContentVo vo, SysUserDto user) {

		// Finder hqlFlow = Finder.create("from CmsContentAuditFlowSteps");
		// hqlFlow.append("where 1=1 and status>0");
		// hqlFlow.append("and roleId=:RoleIdP").setParam("RoleIdP", roleId);
		// hqlFlow.append("group by flow.flowId");
		// List<CmsContentAuditFlowSteps> listSteps = (List<CmsContentAuditFlowSteps>)
		// cmsContentAuditMng.find(hqlFlow);
		//
		// List<Long> listFlows = new ArrayList<Long>();
		// for (CmsContentAuditFlowSteps c : listSteps) {
		// listFlows.add(c.getFlow().getFlowId());
		// }
		// Pagination p=null;
		// Finder hql = Finder.create("from CmsContent");
		// hql.append(" where (auditstatus=0 or auditstatus=null)");
		// hql.append(" and status>0");
		// hql.append(" and auditFlowId in:FlowList").setParam("FlowList", listFlows);
		// hql.append(" order by contentId asc");
		// p = cmsContentAuditMng.find(hql, vo.getPageNo(), vo.getPageSize());
		//
		// return p;

		// 角色列表
		// List<SysRoleDto> roleDto = sysFeignClient.findRoleByUserId(user.getUserId());

		// Long count = 0L;
		//
		//
		// for (SysRoleDto s : roleDto) {
		// if (s.getRoleId() != null) {
		// Long i = cmsContentAuditMng.getMyAuditContentCount(vo, s.getRoleId());
		// count += i;
		// }
		// }
		//
		// List<CmsContent> list = new ArrayList<CmsContent>();
		//
		// for (SysRoleDto s : roleDto) {
		// if (s.getRoleId() != null) {
		// List<CmsContent> cmslist = cmsContentAuditMng.getMyAuditContent(vo,
		// s.getRoleId());
		// for (int i = 0; i < cmslist.size(); i++) {
		// CmsContent c = cmslist.get(i);
		// if (c.getContentType() == null) {
		// if (c.getContenttypeId() != null) {
		// CmsContentType type = cmsContentTypeMng.find(c.getContenttypeId());
		// c.setContentType(type);
		// // list.remove(i);
		// list.add(i, c);
		// }
		// }
		// }
		// }
		// }
		/*
		 * for (int i = 0; i < list.size(); i++) { CmsContent c = list.get(i); if
		 * (c.getContentType() == null) { if (c.getContenttypeId() != null) {
		 * CmsContentType type = cmsContentTypeMng.find(c.getContenttypeId());
		 * c.setContentType(type); list.remove(i); list.add(i, c); } } }
		 */
		List<SysRoleDto> roleDto = sysFeignClient.findRoleByUserId(user.getUserId());
		String roleId = "";
		for (SysRoleDto s : roleDto) {
			if (s.getRoleId() != null) {
				roleId += s.getRoleId() + ",";
			}
		}
		if (roleId.length() > 0) {
			roleId = roleId.substring(0, roleId.length() - 1);
		}

		Long count = cmsContentAuditMng.getMyAuditContentCount(vo, roleId);

		List<CmsContent> list = cmsContentAuditMng.getMyAuditContent(vo, roleId);

		Pagination p2 = new Pagination(vo.getPageNo(), 10, count, list);
		return p2;
	}

	@Override
	public boolean auditResultUpdate(CmsContentVo vo, Long roleId, SysUserDto user) {
		// TODO Auto-generated method stub
		Finder hqlContent = Finder.create("from CmsContent where 1=1");
		hqlContent.append(" and contentId=:ContentIdP").setParam("ContentIdP", vo.getContentId());
		List<CmsContent> entitys = (List<CmsContent>) cmsContentAuditMng.find(hqlContent);

		CmsContentData data = cmsContentDataMng.find(entitys.get(0).getContentId());
		CmsCategory category = cmsCategoryMng.find(data.getSrcCategoryId());
		List<SysRoleDto> roleDto = sysFeignClient.findRoleByUserId(user.getUserId());
		List<Long> roleIds=new ArrayList<>();
		for(SysRoleDto role:roleDto) {
			roleIds.add(role.getRoleId());
		}

		boolean flag = false;
		for (CmsContent entity : entitys) {

			Long flowId = category.getFlowId();
			Integer flowStep = (entity.getAuditStep() == null ? 0 : entity.getAuditStep()) + 1;
			Finder flowSteps = Finder.create("from CmsContentAuditFlowSteps");
			flowSteps.append("where 1=1 and status>0");
			flowSteps.append(" and flow.flowId=:FlowIdP").setParam("FlowIdP", flowId);
			flowSteps.append(" and roleId in :RoleIdP").setParam("RoleIdP", roleIds);

			List<CmsContentAuditFlowSteps> cmsContentAuditFlowStepss = (List<CmsContentAuditFlowSteps>) cmsContentAuditFlowStepsMng
					.find(flowSteps);
			if (cmsContentAuditFlowStepss == null || cmsContentAuditFlowStepss.size() < 1) {
				return false;
			}
			int i = 0;
			for (i = 0; i < cmsContentAuditFlowStepss.size(); i++) {
				if (cmsContentAuditFlowStepss.get(i).getStepIndex() == flowStep) {
					break;
				}
			}
			if (i == cmsContentAuditFlowStepss.size()) {
				flowStep = cmsContentAuditFlowStepss.get(0).getStepIndex();
			}

			Finder flowStepsCount = Finder.create("select count(*) from CmsContentAuditFlowSteps");
			flowStepsCount.append("where 1=1");
			flowStepsCount.append(" and flow.flowId=:FlowIdP").setParam("FlowIdP", flowId);
			flowStepsCount.append(" and status>0");

			Integer countSteps = Integer
					.parseInt(((((List<Long>) cmsContentAuditFlowStepsMng.find(flowStepsCount)).get(0)) + ""));

			if (entity.getAuditHisRecord() == null) {
				entity.setAuditHisRecord(1);
			}
			if (entity.getAuditHisRecord() == 0) {
				entity.setAuditHisRecord(1);
			}
			if (vo.getAuditStepResult() == 1 && countSteps == flowStep) {
				entity.setAuditStep(flowStep);
				entity.setAuditStepResult(1);
				entity.setAuditStatus(1);
				entity.setReleaseTime(new Timestamp(System.currentTimeMillis()));
				flag = true;
			} else if (vo.getAuditStepResult() == 1 && countSteps != flowStep) {
				entity.setAuditStep(flowStep);
				entity.setAuditStepResult(1);
				entity.setAuditStatus(0);
				if (countSteps < flowStep) {

				}
			} else {
				entity.setAuditStep(flowStep);
				entity.setAuditStepResult(0);
				entity.setAuditStatus(2);
			}
			entity.setLastModifyTime(new Timestamp(System.currentTimeMillis()));
			CmsContentData cmsContentData = cmsContentDataMng.find(entity.getContentId());
			CmsContentVo cmsContentVo = BeanMapper.map(entity, CmsContentVo.class);
			if (cmsContentData.getContent() != null) {
				cmsContentVo.setContent(cmsContentData.getContent());
			}

			try {
				// EsContentCovertUtil eSContentUtil = new EsContentCovertUtil();
				ESContent eSContent = EsContentCovertUtil.toEsContent(cmsContentVo);
				R r = elsFeignClient.createContentFeginApi(eSContent);
				if (r.getStatusCode() == 500) {
					throw new HServiceLogicException("同步失败：" + r.getMsg());
				}
			} catch (Exception e) {
				throw new HServiceLogicException("同步失败：" + e.getMessage());
			}

			cmsContentMng.update(entity);

			CmsContentAudit auditEntity = new CmsContentAudit();
			auditEntity.setAuditFlowId(entity.getAuditFlowId());
			auditEntity.setAuditHisRecord(entity.getAuditHisRecord());
			auditEntity.setAuditRole(roleId);
			auditEntity.setAuditStep(flowStep);
			auditEntity.setAuditUser(user.getUserId());
			auditEntity.setAuditUserStr(user.getUsername());
			auditEntity.setAutitStepResult(vo.getAuditStepResult());
			auditEntity.setContentId(entity.getContentId());
			auditEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
			auditEntity.setLastModifyTime(new Timestamp(System.currentTimeMillis()));
			auditEntity.setSiteId(1);
			auditEntity.setStatus(Short.parseShort("1"));
			try {
				cmsContentAuditMng.save(auditEntity);
			} catch (Exception e) {
				entity.setAuditStep(flowStep - 1);
				entity.setAuditStepResult(1);
				entity.setAuditStatus(0);
				cmsContentMng.update(entity);
			}
			// 静态化html
			if (flag) {
				rewriteHtml.writeToHtml(entity, data, user, "add");
			}
		}

		return true;
	}

	@Override
	public CmsContent getOneContent(CmsContentVo vo) {
		Finder finder = Finder.create("from CmsContent");
		finder.append("where 1=1");
		finder.append(" and contentId=:ContentIdP").setParam("ContentIdP", vo.getContentId());
		List<CmsContent> list = (List<CmsContent>) cmsContentMng.find(finder);
		if (list != null && list.size() > 0) {
			CmsContent c = list.get(0);
			c.setContentType(cmsContentTypeMng.find(c.getContenttypeId()));
			return c;
		}
		return new CmsContent();
	}

	@Override
	public CmsContentData getContentDate(CmsContentVo vo) {
		// TODO Auto-generated method stub
		return cmsContentDataMng.find(vo.getContentId());
	}

	@Override
	public boolean auditAllContent(String contentIds, Integer result, Long roleId, SysUserDto user) {
		// TODO Auto-generated method stub
		if (contentIds == null || contentIds.length() < 1) {
			return false;
		}
		String[] idsStr = contentIds.split(",");
		try {
			for (int i = 0; i < idsStr.length; i++) {
				CmsContentVo vo = new CmsContentVo();
				vo.setContentId(Long.parseLong(idsStr[i]));
				vo.setAuditStepResult(result);

				auditResultUpdate(vo, roleId, user);
			}

		} catch (Exception e) {
			return false;
		}

		// auditResultUpdate
		return true;
	}

}
