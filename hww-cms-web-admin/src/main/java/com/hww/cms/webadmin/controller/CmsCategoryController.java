package com.hww.cms.webadmin.controller;

import com.hww.base.common.vo.BaseTreeVo;
import com.hww.base.util.ArrayUtils;
import com.hww.base.util.BeanMapper;
import com.hww.base.util.R;
import com.hww.base.util.StringUtils;
import com.hww.cms.common.Constants;
import com.hww.cms.common.dto.AllCategoryTreeDto;
import com.hww.cms.common.dto.CmsCategoryDto;
import com.hww.cms.common.dto.CmsContentAuditFlowDto;
import com.hww.cms.common.entity.CmsCategory;
import com.hww.cms.common.manager.CmsCategoryMng;
import com.hww.cms.common.manager.CmsContentMng;
import com.hww.cms.common.vo.CmsCategoryManageNodeVo;
import com.hww.cms.common.vo.CmsCategoryVo;
import com.hww.cms.common.vo.SelectAuditFlowVo;
import com.hww.cms.common.vo.SelectSiteListVo;
import com.hww.cms.webadmin.service.CmsCategoryService;
import com.hww.cms.webadmin.service.CmsContentAuditFlowService;
import com.hww.cms.webadmin.util.SysUtils;
import com.hww.framework.web.mvc.controller.AbsBaseController;
import com.hww.sys.api.SysFeignClient;
import com.hww.sys.common.dto.SysSiteDto;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/category")
public class CmsCategoryController
	extends
		AbsBaseController {
	private static final Logger log = LoggerFactory.getLogger(CmsCategoryController.class);

	@Autowired
	CmsCategoryMng cmsCategoryMng;

	@Autowired
	SysFeignClient sysFeignClient;

	@Autowired
	Configuration publishFreeMarkerConfiguration;
	
	@Autowired
	CmsCategoryService cmsCategoryService;

	@Autowired
	CmsContentAuditFlowService cmsContentAuditFlowService;
	@Autowired
	CmsContentMng cmsContentMng;

	@RequestMapping(value = "index.do")
	public String index(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return "category/index";
	}

	@RequestMapping("v_add.do")
	public String add(HttpServletRequest request, HttpServletResponse response, ModelMap model, CmsCategoryVo vo) {
		// add页面查询父类id和name
		String operate = "v_add";
		Long parentId = vo.getParentId();
		CmsCategoryDto cmsCategoryDto = cmsCategoryService.getCmsCategoryNodeDisplay(parentId, operate);
		CmsCategoryManageNodeVo cmsCategoryManageNodeVo = BeanMapper.map(cmsCategoryDto, CmsCategoryManageNodeVo.class);
		cmsCategoryManageNodeVo.setSiteId(1);
		// 站点下拉表，查询站点
		SysSiteDto sysSiteDto=new SysSiteDto();
		List<SysSiteDto> sites=sysFeignClient.findSitelist(sysSiteDto);
		List<SelectSiteListVo> selectSiteList = BeanMapper.mapList(sites, SelectSiteListVo.class);
		cmsCategoryManageNodeVo.setSelectSiteListVos(selectSiteList);
		// 流程下拉表，查询下拉表
		List<CmsContentAuditFlowDto> auditDtoList=cmsContentAuditFlowService.getList(new CmsContentAuditFlowDto());
		if(auditDtoList==null) {
			auditDtoList=new ArrayList<>();
		}
		List<SelectAuditFlowVo> auditList = BeanMapper.mapList(auditDtoList, SelectAuditFlowVo.class);
		cmsCategoryManageNodeVo.setSelectAuditFlowVos(auditList);
		model.addAttribute("form", cmsCategoryManageNodeVo);
		return "category/add";
	}

	@RequestMapping(value = "v_tree.do")
	public String tree(HttpServletRequest request, HttpServletResponse response, ModelMap model) {

		return "category/treemain";

	}

	@RequestMapping(value = "v_edit.do")
	public String edit(HttpServletRequest request, HttpServletResponse response, CmsCategoryVo form, ModelMap model) {
		Long categoryId = form.getCategoryId();
		String operate = "v_edit";
		CmsCategoryDto cmsCategoryDto = cmsCategoryService.getCmsCategoryNodeDisplay(categoryId, operate);
		CmsCategoryManageNodeVo cmsCategoryManageNodeVo = BeanMapper.map(cmsCategoryDto, CmsCategoryManageNodeVo.class);
		// 审批流程下拉表
		List<CmsContentAuditFlowDto> auditDtoList=cmsContentAuditFlowService.getList(new CmsContentAuditFlowDto());
		if(auditDtoList==null) {
			auditDtoList=new ArrayList<>();
		}
		List<SelectAuditFlowVo> auditList = BeanMapper.mapList(auditDtoList, SelectAuditFlowVo.class);
		cmsCategoryManageNodeVo.setSelectAuditFlowVos(auditList);
		// 站点下拉表，查询站点
		SysSiteDto sysSiteDto=new SysSiteDto();
		List<SysSiteDto> sites=sysFeignClient.findSitelist(sysSiteDto);
		List<SelectSiteListVo> selectSiteList = BeanMapper.mapList(sites, SelectSiteListVo.class);
		cmsCategoryManageNodeVo.setSelectSiteListVos(selectSiteList);
		model.addAttribute("entity", cmsCategoryManageNodeVo);
		return "category/edit";
	}

	@RequestMapping("o_update.do")
	@ResponseBody
	public Boolean update(@Valid @ModelAttribute("form") CmsCategoryManageNodeVo form, BindingResult errors) {
		if (errors.hasErrors()) {
			return false;
		}
		CmsCategoryDto cmsCategoryDto = BeanMapper.map(form, CmsCategoryDto.class);
		cmsCategoryService.updateCmsCategoryNode(cmsCategoryDto,false);
		return true;
	}

	@RequestMapping("o_save.do")
	@ResponseBody
	public Boolean save(HttpServletRequest request, @Valid @ModelAttribute("form") CmsCategoryManageNodeVo form,
			BindingResult errors) {
		if (errors.hasErrors()) {
			return false;
		}
		CmsCategoryDto cmsCategoryDto = BeanMapper.map(form, CmsCategoryDto.class);
		cmsCategoryService.saveCmsCategoryNode(cmsCategoryDto ,true);
		return true;
	}

	@RequestMapping(value = "v_list.do")
	public String subList(HttpServletRequest request, HttpServletResponse response, ModelMap model,
			CmsCategoryVo form) {
//		Integer siteId = SysUtils.getSiteId(request);

		return "category/list";

	}

	/**
	 *
	 * @param request
	 * @param response
	 * @param model
	 * @param node
	 * @param cCategoryIds
	 *            被选中的项-源
	 * @param srcCategoryIdLock
	 *            true不能操作源id
	 * @param op
	 *            only parent 只显示父节点
	 */
	@RequestMapping(value = "enableCategoryJson.do")
	@ResponseBody
	public List<BaseTreeVo> allTree(HttpServletRequest request, HttpServletResponse response, ModelMap model,
			Integer node, String cCategoryIds, Integer op, Boolean srcCategoryIdLock, Integer srcCategoryId) {
		log.info("打开菜单列表页");
		if (node == null) {
			node = 0;
		}
		String[] categoryIds = null;
		Set<Integer> categoryIdSet = null;
		if (StringUtils.isNotBlank(cCategoryIds)) {
			if (cCategoryIds.lastIndexOf(",") == cCategoryIds.length()) {
				categoryIds = cCategoryIds.substring(0, cCategoryIds.length() - 1).split(",");
			}
			categoryIds = cCategoryIds.split(",");
			categoryIdSet = new HashSet<Integer>(categoryIds.length);
			for (String categoryId : categoryIds) {
				categoryIdSet.add(Integer.parseInt(categoryId));
			}
		}
//		Integer siteId = SysUtils.getSiteId(request);
		//TODO 写死
		Integer siteId=1;
		Long userId = SysUtils.getUserId(request);
		List<CmsCategory> list = null;
		if (op != null) {
			list = cmsCategoryMng.getRetrievingASinglePath(op, siteId);
		} else {
			/*list = cmsCategoryMng.getRetrievingFullTree(userId, siteId, null);*/
		}
		List<BaseTreeVo> tree = new ArrayList<BaseTreeVo>();
		for (CmsCategory cmsCategory : list) {
			BaseTreeVo m = new BaseTreeVo();
			m.setId(cmsCategory.getCategoryId());
			m.setChkDisabled(false);
			if (cmsCategory.getRgt() - cmsCategory.getLft() == 1 && cmsCategory.getParentId() != null) {
				m.setIsParent(false);
			} else {
				m.setIsParent(true);
			}
			if (cmsCategory.getParent() != null) {
				m.setpId(cmsCategory.getParent().getCategoryId());
			} else {
				m.setpId(Long.parseLong("0"));
			}
			m.setName(cmsCategory.getCategoryName());
			m.setAccessPath("content/v_list.do?categoryId=" + cmsCategory.getCategoryId());
			if (categoryIdSet != null && categoryIdSet.contains(cmsCategory.getCategoryId())) {
				m.setChecked(true);
				if (cmsCategory.getCategoryId().equals(srcCategoryId) && srcCategoryIdLock) {
					m.setChkDisabled(true);
				}
			} else {
				m.setChecked(false);
			}
			tree.add(m);
		}
		return tree;
	}

	@RequestMapping("allCategoryJson.do")
	@ResponseBody
	public List<BaseTreeVo> systemC(HttpServletRequest request, HttpServletResponse response, ModelMap model,
			Integer node,Short status) {
		if (node == null) {
			node = 0;
		}
		List<BaseTreeVo> tree = new ArrayList<BaseTreeVo>();
		//Integer siteId = SysUtils.getSiteId(request);
		Integer siteId  = 1;
		//Long userId = SysUtils.getUserId(request);
		Long userId = 1L;
		List<AllCategoryTreeDto> allCategoryTreeDtos = cmsCategoryService.listCmsCategory(null, siteId, status);
		for (AllCategoryTreeDto allCategoryTreeDto : allCategoryTreeDtos) {
			BaseTreeVo m = new BaseTreeVo();
			m.setId(allCategoryTreeDto.getCategoryId());
			m.setChkDisabled(false);
			m.setIsParent(true);
			if  (allCategoryTreeDto.getParentId() != null) {
				m.setpId(allCategoryTreeDto.getParentId());
			} else {
				m.setpId(Long.parseLong("0"));
			}
			m.setName(allCategoryTreeDto.getCategoryName());
			m.setAccessPath("category/v_list.do?categoryId=" + allCategoryTreeDto.getCategoryId());
			m.setChecked(false);
			tree.add(m);
		}
		return tree;
	}
	
	/**
	 * 区分单页、外部链接、列表、专题树
	 * @param typeId
	 * @param parentId 为空查全部，为0查根节点，为其他查对应树
	 * @return
	 */
	@RequestMapping("getCategoryJsonByType.do")
	@ResponseBody
	public List<BaseTreeVo> getCategoryJsonByType(HttpServletRequest request, HttpServletResponse response, ModelMap model,
			Short typeId,@RequestParam(name="parentId",required=false)Long parentId) {
		List<BaseTreeVo> tree = new ArrayList<BaseTreeVo>();
		//Integer siteId = SysUtils.getSiteId(request);
		Integer siteId  = 1;
		//Long userId = SysUtils.getUserId(request);
		
		List<CmsCategory> categoryList = cmsCategoryMng.getCategorysByType(typeId, siteId,parentId);
		for (int i=0;i< categoryList.size();i++) {
			CmsCategory cmsCategory = categoryList.get(i);
			BaseTreeVo m = new BaseTreeVo();
			m.setId(cmsCategory.getCategoryId());
			m.setChkDisabled(false);
			if (cmsCategory.getParentId() != null) {
				m.setIsParent(false);
				m.setpId(cmsCategory.getParent().getCategoryId());
			} else {
				m.setIsParent(true);
				m.setpId(Long.parseLong("0"));
			}
			m.setName(cmsCategory.getCategoryName());
			m.setChecked(false);
			tree.add(m);
		}
		return tree;
	}

	@RequestMapping("o_delete.do")
	public String delete(HttpServletRequest request, HttpServletResponse response, CmsCategoryVo form) {
		Collection<Long> categoryIds = null;
		if (form != null) {

			if (StringUtils.isNotBlank(form.getAllIDCheck())) {
				String[] ids = StringUtils.split(form.getAllIDCheck(), ",");
				if (ArrayUtils.isNotEmpty(ids)) {
					categoryIds = new ArrayList<Long>(ids.length);
				}
				for (String str : ids) {
					if (StringUtils.isNumeric(str)) {
						categoryIds.add(Long.parseLong(str));
					}
				}
			}
			if (form.getCategoryId() != null) {// 单个删除
				categoryIds = new ArrayList<Long>(1);
				categoryIds.add(form.getCategoryId());
			}
		}
		if (categoryIds != null) {
			cmsCategoryMng.updateStatusByProperty(Constants.CATEGORY_STATUS_DELETED, "categoryId", categoryIds);
		}

		return "redirect:v_list.do?parentId=" + form.getParentId();
	}
	@RequestMapping(value = "v_delete_category.do")
	@ResponseBody
	public R getCmsCategoryById(HttpServletRequest request, HttpServletResponse response, CmsCategoryVo form, ModelMap model) {
		try {
			cmsCategoryService.deleteCategory(form);
		}catch(Exception e) {
			return R.error("删除失败！");
		}
		return R.ok("成功");
	}


}
