package com.hww.cms.webservice.service;

import com.hww.base.util.R;
import com.hww.cms.common.dto.HCmsInstrDto;

import java.util.List;

public interface CmsContentCacheProxyService {

//	CmsContentData loadCmsContentDataById(Long newsId);
	
//	CmsContent loadCmsContentById(Long newsId);
	
	List<Long> loadUninterestedContentIds(Long memberId);

//	List<Long> loadCmsCateIdsByColumnId(Long columnId);

	R newsUninterest(HCmsInstrDto uninstrDto);

//	List<AppRecommConfigDto> loadAllRecomm();

//	List<SnsTopicVo> loadTopicByIds(HBaseSnsQueryFeginApiDto queryFeginApiDto);
	
	

}
