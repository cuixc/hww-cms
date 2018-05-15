package com.hww.cms.webadmin.service;

import com.hww.cms.common.entity.CmsContent;
import com.hww.cms.common.entity.CmsContentData;
import com.hww.sys.common.dto.SysUserDto;
import org.springframework.beans.factory.annotation.Value;

/**
 * 静态化新闻类
 *@author lh
 *@date 2018/3/6
 *@since 0.1
 */
public interface CmsRewriteHtml {

	void writeToHtml(CmsContent cmsContent, CmsContentData cmsContentData, SysUserDto user, String operation);

	void deleteHtml(CmsContent cmscontent);

}
