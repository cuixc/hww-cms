package com.hww.cms.api;

import com.hww.base.common.page.Pagination;
import com.hww.base.common.vo.BaseTreeVo;
import com.hww.base.util.R;
import com.hww.cms.common.dto.CmsContentDataDto;
import com.hww.cms.common.dto.CmsContentDto;
import com.hww.cms.common.dto.HCmsQueryDto;
import com.hww.sns.common.dto.SnsTopicToCmsDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "hww-cms-web-service-consumer")
public interface CmsFeignClient {

	@RequestMapping(value = "/cms/content/loadNewsNoPostAndTopicFeginApi.do", method = RequestMethod.POST)
	CmsContentDataDto loadNewsNoPostAndTopicFeginApi(@RequestBody HCmsQueryDto cmsQueryDto);

	// @RequestMapping(value = "/cms/content/loadNewsNoPostAndTopicFeginApi.do",
	// method = RequestMethod.POST)
	// CmsContentDataVo loadNewsDetail(@RequestBody HCmsQueryDto cmsQueryDto);

	/*  zhaoke 没用就删掉*/
	@RequestMapping("/cms/content/update_auditstatu.do")
	public String updateAuditstatus(@RequestBody CmsContentDto cmsContentDto);

	@RequestMapping("/cms/content/list.do")
	public Pagination list(@RequestBody(required = false) CmsContentDto cmsContentDto);

	// =====hww-app-web-admin=============
	@RequestMapping(value = "/cms/category/admin/allCategoryJson.do")
	public List<BaseTreeVo> allCategoryJson();

	@RequestMapping(value = "/cms/tocontent/snsToCmsContentFeginApi.do", method = RequestMethod.POST)
	R toCmsContent(@RequestBody SnsTopicToCmsDto snsTopicToCmsDto);

	/**
	 * 分页查询所有新闻,并且是时间倒序，最新的新闻显示在最上边
	 * @param cmsQueryDto
	 * @return
	 */
	@RequestMapping(value = "/cms/content/listAllCmsContentByPage.do")
	List<CmsContentDto> listAllCmsContentByPage(@RequestBody HCmsQueryDto cmsQueryDto);

}
