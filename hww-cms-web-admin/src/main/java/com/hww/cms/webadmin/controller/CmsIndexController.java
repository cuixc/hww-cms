package com.hww.cms.webadmin.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hww.sys.api.SysFeignClient;
import com.hww.sys.common.dto.SysModuleDto;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hww.framework.web.mvc.controller.AbsBaseController;

import java.util.List;

@Controller
public class CmsIndexController
	extends
		AbsBaseController {

	@Autowired
	private SysFeignClient sysFeignClient;

	@RequestMapping("index.do")
	public String index(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String username=(String) SecurityUtils.getSubject().getPrincipal();
		List<SysModuleDto> moduleDtos=sysFeignClient.listByUser(username);
		model.addAttribute("modules",moduleDtos);
		return "index";

	}
	
	@RequestMapping("console.do")
	public String console(HttpServletRequest request, HttpServletResponse response, ModelMap model) {


		return "console";

	}

}
