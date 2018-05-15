package com.hww.cms.common.vo;

import com.hww.base.common.vo.IBaseVo;

/**
 * @Author: zhaoke
 * @Description:  cms后台管理：站点下拉列表专用VO
 */
public class SelectSiteListVo implements IBaseVo {

    private Integer siteId;
    private String siteName;

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}
