package com.hww.cms.webservice.controller;

import com.hww.base.common.vo.BaseTreeVo;
import com.hww.cms.common.dto.AllCategoryTreeDto;
import com.hww.cms.webservice.service.CmsCategoryForAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cms/category/admin")
public class CmsCategoryForAdminController {

	@Autowired
	CmsCategoryForAdminService cmsCategoryForAdminService;
	@RequestMapping(value = "allCategoryJson.do", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public List<BaseTreeVo> allCategoryJson() {
		List<BaseTreeVo> tree = new ArrayList<BaseTreeVo>();
		List<AllCategoryTreeDto> allCategoryTreeDtos = cmsCategoryForAdminService.getRetrievingFullTree();
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
}
