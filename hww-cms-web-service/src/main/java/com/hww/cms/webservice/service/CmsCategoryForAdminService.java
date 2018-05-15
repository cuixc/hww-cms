package com.hww.cms.webservice.service;

import com.hww.cms.common.dto.AllCategoryTreeDto;

import java.util.List;

public interface CmsCategoryForAdminService {

	List<AllCategoryTreeDto> getRetrievingFullTree();
}
