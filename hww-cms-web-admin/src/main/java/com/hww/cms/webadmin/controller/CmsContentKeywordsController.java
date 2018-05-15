package com.hww.cms.webadmin.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.hww.base.util.R;
import com.hww.cms.common.entity.CmsContentKeywords;
import com.hww.cms.webadmin.service.CmsContentKeywordsService;

@Controller
@RequestMapping("/keywords")
public class CmsContentKeywordsController {
	
	private static final Log log = LogFactory.getLog(CmsContentKeywordsController.class);
	
	@Autowired
	private CmsContentKeywordsService cmsContentKeywordsService;
	//{"data":[{"title":"\u5317\u4eac\u73b0\u4ee3"},{"title":"\u5317\u4eac\u57ce\u5efa\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u5efa\u5de5\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u9996\u90fd\u65c5\u6e38\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u533b\u836f\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u4e00\u8f7b\u63a7\u80a1\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u91d1\u9685\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u71d5\u4eac\u5564\u9152\u96c6\u56e2\u516c\u53f8"},{"title":"\u5317\u4eac\u5e02\u71c3\u6c14\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"},{"title":"\u5317\u4eac\u4f4f\u603b\u96c6\u56e2\u6709\u9650\u8d23\u4efb\u516c\u53f8"}]}
	@RequestMapping("searchCmsKeywords.do")
	@ResponseBody
    public Map<String,List<Map<String,String>>> searchCmsKeywords(String keyword) {
		
		List<CmsContentKeywords> list = cmsContentKeywordsService.queryCmsContentKeywords(keyword);
		
		List<Map<String,String>> data=list.stream().map(val->{
			Map<String,String> item=Maps.newHashMap();
			item.put("title", val.getKeywordContent());
			return item;
		}).collect(Collectors.toList());
		
		Map<String,List<Map<String,String>>> res=Maps.newHashMap();
		res.put("data", data);
		
        return res;
    }
	
	@RequestMapping("insertKeyword.do")
	@ResponseBody
	public R insertKeyword(String strKeyword) {
		cmsContentKeywordsService.insertContentKeyword(strKeyword);
		return R.ok();
	}
}
