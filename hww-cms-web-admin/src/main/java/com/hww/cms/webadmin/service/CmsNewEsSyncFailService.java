package com.hww.cms.webadmin.service;

import com.hww.cms.common.entity.CmsNewEsSyncFail;

public interface CmsNewEsSyncFailService {

	void save(CmsNewEsSyncFail cmsNewEsSyncFail);
	
	
	void doAync();
	
	

	
}
