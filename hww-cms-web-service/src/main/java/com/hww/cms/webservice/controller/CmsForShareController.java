package com.hww.cms.webservice.controller;

import com.hww.cms.common.dto.CmsContentDataDto;
import com.hww.cms.webservice.service.CmsContentService;
import com.hww.file.api.FileFeignClient;
import com.hww.file.common.dto.FileInfoDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping
public class CmsForShareController {
	@Resource
	private CmsContentService cmsContentService;

	@Autowired
	private FileFeignClient fileFeignClient;

	@RequestMapping("/pshare/{CategoryId}/content_{contentId}_{page}.html")
	public String shareCms(@PathVariable Long CategoryId, @PathVariable Long contentId, @PathVariable Long page,
			ModelMap map) {
		CmsContentDataDto contentDateVo = cmsContentService.loadNewsDetailForShare(contentId);
		if (contentDateVo == null) {
			return "share/404";
		}
		// 图集的话，调用file服务解析图集信息
		if (5 == (contentDateVo.getContenttypeId())) {
			List<FileInfoDto> fileInfoDtos = fileFeignClient.listFileInfoByIds(contentDateVo.getAttachmentIds());
			List<FileInfoDto> fileInfos = new ArrayList<>(fileInfoDtos.size());
			for (FileInfoDto file : fileInfoDtos) {
				FileInfoDto fileInfo = new FileInfoDto();
				fileInfo.setFileDesc(file.getFileDesc());
				fileInfo.setFileName(file.getFileName());
				fileInfo.setFileId(file.getFileId());
				//zhaoke fileInfo.setFileRelativePath(file.getFileVisitUrl());
				fileInfos.add(fileInfo);
			}
			contentDateVo.setFileInfoList(fileInfos);
		}
		// 图文类型的话，需要解析正文中的图片标签，加上一个class样式，防止图片溢出
		if (2 == contentDateVo.getContenttypeId()) {
			Whitelist whitelist = Whitelist.basicWithImages();
			String html = Jsoup.clean(contentDateVo.getContent(), whitelist);
			html=html.replaceAll("\\s{2,}", "");
			Document document = Jsoup.parse(html);
			Elements imgs = document.select("img");
			for (Element element : imgs) {
				Element parent = element.parent();
				// 给父节点的p标签加上class
				if (parent.tagName().equals("p"))
					parent.addClass("imgs_c");
				else
					element.addClass("imgs_c");

			}

			contentDateVo.setContent(document.body().html());
		}
		map.addAttribute("contentDateVo", contentDateVo);
		return "share/content";

	}

}
