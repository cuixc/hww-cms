package com.hww.cms.webservice.service.impl;

import com.google.common.collect.Lists;
import com.hww.app.api.AppFeignClient;
import com.hww.app.common.dto.AppBehaviorDto;
import com.hww.base.util.R;
import com.hww.cms.common.dto.HCmsInstrDto;
import com.hww.cms.webservice.service.CmsContentCacheProxyService;
import com.hww.framework.common.exception.HServiceLogicException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
//不能再同一个类中调用被注解缓存了的方法
//import com.hww.sns.common.vo.NearTopicVo;
@Service("cmsContentCacheProxyService")
@Transactional
public class CmsContentCacheProxyServiceImpl implements CmsContentCacheProxyService {

	private static final Log log = LogFactory.getLog(CmsContentCacheProxyServiceImpl.class);
	
//	@Autowired
//	private CmsContentMng cmsContentMng;
//	@Autowired
//	private CmsContentDataMng cmsContentDataMng;
	@Autowired
	private AppFeignClient appFeignClient;
//	@Autowired SnsFeignClient snsFeignClient;
	
//	 	@Scheduled(fixedDelay = 1000*60*2)
//	   @CacheEvict(value = "cms_loadAllRecomm",allEntries=true)
//	   public void loadAllRecomm_delete_from_cache() {
//		   log.debug("----loadCmsCateIdsByColumnId_delete_from_cache------------");
//	   }
//	@Cacheable(value = "cms_loadAllRecomm",key="'loadAllRecomm'")
//	@Override
//	public List<AppRecommConfigDto> loadAllRecomm(){
//		
//		return appFeignClient.loadAllRecomm();
//	}
	//List<AppRecommConfigDto>  recommAllLists = appFeignClient.loadAllRecomm();
	
	
	
//	   @Scheduled(fixedDelay = 1000*60*5)
//	   @CacheEvict(value = "cms_loadCmsCateIdsByColumnId",allEntries=true)
//	   public void loadCmsCateIdsByColumnId_delete_from_cache() {
//		   log.debug("----loadCmsCateIdsByColumnId_delete_from_cache------------");
//	   }
//	  
//	@Cacheable(value = "cms_loadCmsCateIdsByColumnId",key="'loadCmsCateIdsByColumnId_'+#columnId")
//	@Override
//	public List<Long> loadCmsCateIdsByColumnId(Long columnId){
//		if(columnId!=null) {
//			return appFeignClient.loadCmsCateIdsByColumnId(columnId);
//		}
//		return Lists.newArrayList();
//	}
	
	
//	   @Scheduled(fixedDelay = 1000*60*10)
//	   @CacheEvict(value = "cms_loadCmsContentDataById",allEntries=true)
//	   public void loadCmsContentDataById_delete_from_cache() {}
//	   
//    @Cacheable(value = "cms_loadCmsContentDataById",key="'loadCmsContentDataById_'+#contentId")
//    @Override
//	public CmsContentData loadCmsContentDataById(Long contentId) {
//		if( contentId==null) {
//			return null;
//		}
//		CmsContentData cmsContentData = cmsContentDataMng.loadCmsContentDataByContentIdForSerice(contentId) ;
//		return cmsContentData;
//	}
    
//    @Scheduled(fixedDelay = 1000*60*10)
//	   @CacheEvict(value = "cms_loadCmsContentById",allEntries=true)
//	   public void loadCmsContentById_delete_from_cache() { }
//    @Cacheable(value = "cms_loadCmsContentById",key="'loadCmsContentById_'+#contentId")
//    @Override
//	public CmsContent loadCmsContentById(Long contentId) {
//    	if( contentId==null) {
//			return null;
//		}
//    	CmsContent cmsContent = cmsContentMng.findOneByContentIdForService(contentId);
//    	return cmsContent;
//	}
    
    
//    @Cacheable(value = "cms_loadUninterestedContentIds",key="'loadUninterestedContentIds_'+#memberId")
    @Override
	public  List<Long> loadUninterestedContentIds(Long memberId){
		//用户行为类别0 详情查看， 1点赞 ，2 收藏，3评论，4爆料，5 不感兴趣，6内容太水，7看过了
		if(memberId==null) {
			return Lists.newArrayList();
		}
		List<Long> contentIds= appFeignClient.loadBehaviorContentIds(new AppBehaviorDto().setMemberId(memberId).setBevType(AppBehaviorDto.BevType.b5_gxq).setPlateType(AppBehaviorDto.PlatType.b0_news));
		return contentIds;
	}
//    @CacheEvict(value = "cms_loadUninterestedContentIds",key="'loadUninterestedContentIds_'+#uninstrDto.memberId")
    @Override
    public R newsUninterest(HCmsInstrDto uninstrDto) {
        try {
        	  AppBehaviorDto behaviorDto =new AppBehaviorDto(uninstrDto.getMemberId(),uninstrDto.getContentId(), AppBehaviorDto.BevType.b5_gxq, AppBehaviorDto.BevValue.b1_ok,  AppBehaviorDto.PlatType.b0_news).setPlateType(AppBehaviorDto.PlatType.b0_news);
        	  behaviorDto.setKeywords(uninstrDto.getUninterestWords());
        	  return appFeignClient.createBehavior(behaviorDto);
        }  catch (HServiceLogicException e) {
            return R.error(1,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return R.error(1, "操作失败");
        }
    }
    
//    @Scheduled(fixedDelay = 1000*60*3)
//	   @CacheEvict(value = "cms_loadTopicByIds",allEntries=true)
//	   public void loadTopicByIds_delete_from_cache() {
//		   log.debug("----loadTopicByIds_delete_from_cache------------");
//	   }
//    
//    @CacheEvict(value = "cms_loadTopicByIds",key="'loadTopicByIds_'+#queryFeginApiDto.toCachekey()")
//    @Override
//    public  List<SnsTopicVo> loadTopicByIds(HBaseSnsQueryFeginApiDto queryFeginApiDto){
//    	List<SnsTopicVo> recommTopicList=snsFeignClient.loadTopicByIdsFeginApi(queryFeginApiDto);
//    	return recommTopicList;
//    }
    
    
    
    
    
}
