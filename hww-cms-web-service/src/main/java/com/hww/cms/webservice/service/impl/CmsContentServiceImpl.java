package com.hww.cms.webservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hww.app.api.AppFeignClient;
import com.hww.app.common.dto.AppBehaviorDto;
import com.hww.app.common.dto.AppRecommConfigDto;
import com.hww.base.common.util.Finder;
import com.hww.base.util.*;
import com.hww.cms.common.dto.*;
import com.hww.cms.common.entity.CmsContent;
import com.hww.cms.common.entity.CmsContentData;
import com.hww.cms.common.entity.CmsOrigin;
import com.hww.cms.common.entity.CmsSpecial;
import com.hww.cms.common.manager.CmsContentDataMng;
import com.hww.cms.common.manager.CmsContentMng;
import com.hww.cms.common.manager.CmsOriginMng;
import com.hww.cms.common.util.LocationUtils;
import com.hww.cms.common.vo.CmsContentDataVo;
import com.hww.cms.common.vo.CmsContentVo;
import com.hww.cms.common.vo.HCmsSpecialListVo;
import com.hww.cms.common.vo.add.SnsTopicVo;
import com.hww.cms.webservice.service.CmsContentCacheProxyService;
import com.hww.cms.webservice.service.CmsContentService;
import com.hww.cms.webservice.service.CmsSpecialService;
import com.hww.els.api.ElsFeignClient;
import com.hww.els.common.HSearchDto;
import com.hww.els.common.entity.ESRecommendHis;
import com.hww.file.api.FileFeignClient;
import com.hww.sns.api.SnsFeignClient;
import com.hww.sns.common.dto.HBaseSnsQueryDto;
import com.hww.sns.common.dto.HBaseSnsQueryFeginApiDto;
import com.hww.sns.common.dto.SnsPostDto;
import com.hww.sns.common.dto.SnsTopicDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import com.hww.sns.common.vo.NearTopicVo;
@Service("cmsContentService")
@Transactional
public class CmsContentServiceImpl implements CmsContentService {

	private static final Log log = LogFactory.getLog(CmsContentServiceImpl.class);

	@Autowired
	private CmsContentMng cmsContentMng;
	@Autowired
	private CmsContentDataMng cmsContentDataMng;
	@Autowired
	private SnsFeignClient snsFeignClient;
	@Autowired
	private FileFeignClient fileInfoFeignClient;

	@Autowired
	private ElsFeignClient elsFeignClient;

	@Autowired
	private AppFeignClient appFeignClient;

	@Autowired
	private CmsSpecialService cmsSpecialService;

	@Autowired
	private CmsContentCacheProxyService cmsContentCacheProxyService;
	// ------------------------------新闻详情加载--------------------
	@Autowired
	private CmsOriginMng cmsOriginMng;

	@Override
	public CmsContentDataDto loadNewsDetailWithoutPostAndTopic(HCmsQueryDto cmsQueryDto) {
		CmsContentDto cmsContentDto = cmsContentMng.findOneByContentIdForService(cmsQueryDto.getContentId());
		if (cmsContentDto == null || cmsContentDto.getId() == null) {
			return null;
		}
		CmsContentDataDto cmsContentDataDto = cmsContentDataMng
				.loadCmsContentDataByContentIdForSerice(cmsQueryDto.getContentId());
		if (cmsContentDataDto == null) {
			cmsContentDataDto = new CmsContentDataDto();
		}
		// 复制cmsContent到cmsContentDataVo
		CopyBean.copyNotNull(cmsContentDto, cmsContentDataDto);

		Long memberId = cmsQueryDto.getMemberId();

		constructCmsContentDataDtoForDetail(cmsContentDataDto, cmsContentDto, memberId);

		return cmsContentDataDto;
	}

	@Override
	public List<CmsContentDto> listAllCmsContentByPage(HCmsQueryDto queryDto) {
		List<CmsContentDto> cmsContentDtos = cmsContentMng.listAllCmsContentByPage(queryDto);
		// 去重
		cmsContentDtos = cmsContentDtos.parallelStream().distinct().collect(Collectors.toList());
		// 时间倒序
		cmsContentDtos = cmsContentDtos.stream().sorted((o1, o2) -> {
			return o2.getCreateTime().compareTo(o1.getCreateTime());
		}).collect(Collectors.toList());
		return cmsContentDtos;
	}

	/**
	 * 如果有新增的内容类型，应该修改去重实现
	 *
	 * @param homeCmsSnsList 除开置顶新闻的列表
	 * @param topNewsList    置顶新闻
	 * @return
	 */
	@Override
	public List<Map<String, Object>> distinctRecommendData(List<Map<String, Object>> homeCmsSnsList,
			List<CmsContentVo> topNewsList) {
		if (homeCmsSnsList == null || homeCmsSnsList.size() == 0)
			return Lists.newArrayList();
		List<Map<String, Object>> newList = new ArrayList<>(homeCmsSnsList.size());
		// 正文id
		Set<Long> contentIds = new HashSet<>(16);
		// 专题id
		Set<Long> specialContentIds = new HashSet<>(4);
		// 得到首页新闻列表的唯一id
		for (Map<String, Object> map : homeCmsSnsList) {
			Object obj;
			if ((obj = map.get("content")) != null && obj instanceof CmsContentVo) {
				CmsContentVo cmsContentVo = (CmsContentVo) obj;
				contentIds.add(cmsContentVo.getContentId());
			} else if ((obj = map.get("contents")) != null && obj instanceof HCmsSpecialListVo) {
				HCmsSpecialListVo specialListVo = (HCmsSpecialListVo) obj;
				specialContentIds.add(specialListVo.getSpecialId());
			}
		}
		// 遍历置顶新闻，已经置顶了的新闻不再下面的列表显示出来
		if (topNewsList != null && topNewsList.size() > 0) {
			for (CmsContentVo contentVo : topNewsList) {
				contentIds.remove(contentVo.getContentId());
			}
		}
		// 最后得到新的新闻列表
		for (Map<String, Object> map : homeCmsSnsList) {
			Object obj = map.get("content");
			if (obj != null && obj instanceof CmsContentVo) {
				CmsContentVo cmsContentVo = (CmsContentVo) obj;
				Long id = cmsContentVo.getContentId();
				if (id != null && contentIds.contains(id)) {
					newList.add(map);
					contentIds.remove(id);
				}
				continue;
			}
			obj = map.get("contents");
			if (obj != null && obj instanceof HCmsSpecialListVo) {
				HCmsSpecialListVo specialListVo = (HCmsSpecialListVo) obj;
				Long specialId = specialListVo.getSpecialId();
				if (specialId != null && specialContentIds.contains(specialId)) {
					newList.add(map);
					specialContentIds.remove(specialId);
				}
				continue;
			}
			// 最后这个操作是，防止后期再增加推荐类型，忘记修改去重实现，而加的
			newList.add(map);

		}
		return newList;
	}

	@Override
	public CmsContentDataDto loadNewsDetail(HCmsQueryDto cmsQueryDto) {
		CmsContentDto cmsContent = cmsContentMng.findOneByContentIdForService(cmsQueryDto.getContentId());
		if (cmsContent == null || cmsContent.getId() == null) {
			return null;
		}
		CmsContentDataDto cmsContentData = cmsContentDataMng
				.loadCmsContentDataByContentIdForSerice(cmsQueryDto.getContentId());
		if (cmsContentData == null) {
			cmsContentData = new CmsContentDataDto();
		}

		// 复制cmsContent到cmsContentDataDto
		CopyBean.copyNotNull(cmsContent, cmsContentData);

		if (cmsContent.getHiddenMap() != null) {
			cmsContentData.setHiddenMap(cmsContent.getHiddenMap());
		}
		Long memberId = cmsQueryDto.getMemberId();

        if (cmsContent.getHiddenMap() != null) {
            cmsContentData.setHiddenMap(cmsContent.getHiddenMap());
        }
		// 加载爆料
		List<SnsTopicDto> newsTopicVoList = loadNewsTopicListByNewsId(cmsContent.getId(), memberId);
		cmsContentData.setSnsTopicList(newsTopicVoList);
		// 加载评论列表
		HBaseSnsQueryDto snsQueryDto = HBaseSnsQueryDto.newForNewsQuery(cmsContent.getId(), memberId)
				.setPageNo(cmsQueryDto.getPageNo()).setPageSize(cmsQueryDto.getPageSize());
		List<SnsPostDto> postVoList = loadNewsPostListByNewsId(snsQueryDto);
		cmsContentData.setSnsPostVoList(postVoList);
		try {
			AppBehaviorDto behaviorDto = new AppBehaviorDto(cmsQueryDto.getMemberId(), cmsContent.getContentId(),
					AppBehaviorDto.BevType.b0_xq, AppBehaviorDto.BevValue.b1_ok, AppBehaviorDto.PlatType.b0_news);
			careateDetaiBehavir(behaviorDto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		constructCmsContentDataDtoForDetail(cmsContentData, cmsContent, memberId);
		if (2 == cmsContentData.getContenttypeId()) {
			if (cmsContent != null && com.hww.base.util.StringUtils.isNotBlank(cmsContentData.getContent())) {
				Whitelist whitelist = Whitelist.basicWithImages();
				String html = Jsoup.clean(cmsContentData.getContent(), whitelist);
				html = html.replace("\\s{2,}", "");
				Document document = Jsoup.parse(html);
				Elements imgs = document.select("img");
				for (Element element : imgs) {
					Element parent = element.parent();
					// 给父节点的p标签加上class
					if (parent.tagName().equals("p"))
						parent.addClass("imgs");
				}
				cmsContentData.setContent(document.body().html());
			}
		}
		return cmsContentData;
	}

	@Async
	public void careateDetaiBehavir(AppBehaviorDto behaviorDto) {
		try {
			appFeignClient.createBehavior(behaviorDto);
		} catch (Exception e) {
		}
	}

	@Override
	public CmsContentDataDto loadNewsDetailForShare(Long contentId) {
		CmsContentDto cmsContent = cmsContentMng.findOneByContentIdForService(contentId);
		if (cmsContent == null || !"1".equals(cmsContent.getStatus().toString())
				|| !"1".equals(cmsContent.getAuditStatus().toString())) {
			log.debug("----cmsContentCacheProxyService.loadCmsContentById(" + contentId + ")为空");
			return null;
		}

		CmsContentDataDto cmsContentData = cmsContentDataMng.loadCmsContentDataByContentIdForSerice(contentId);
		if (cmsContentData == null) {
			log.debug("----cmsContentCacheProxyService.loadCmsContentDataById(" + contentId + ")为空");
			cmsContentData = new CmsContentDataDto();
		}

		// 复制cmsContent到cmsContentDataDto
		CopyBean.copyNotNull(cmsContent, cmsContentData);
		constructCmsContentDataDtoForDetail(cmsContentData, cmsContent, null);
		return cmsContentData;
	}

	// =========================列表相关================================

	@Override
	public List<CmsContentVo> loadCmsContentByIds(List<Long> contentIds, Long memberId, boolean enableUnintrested) {
		if (contentIds == null) {
			return Lists.newArrayList();
		}
		List<Long> uninterestedContentIds = Lists.newArrayList();
		if (enableUnintrested) {
			if (memberId != null) {
				uninterestedContentIds = cmsContentCacheProxyService.loadUninterestedContentIds(memberId);
				contentIds.removeAll(uninterestedContentIds);// 除去不感兴趣的数据====暂时先直接除去
			}
		}

		List<CmsContent> cmsContentList = cmsContentMng.loadCmsContentList(contentIds);
		if (cmsContentList == null || cmsContentList.isEmpty()) {
			return Lists.newArrayList();
		}

		List<CmsContentVo> cmsContentVoList = BeanMapper.mapList(cmsContentList, CmsContentVo.class);

		if (cmsContentVoList == null || cmsContentVoList.isEmpty()) {
			return Lists.newArrayList();
		}
		// 用户收藏状态---显示？
		for (CmsContentVo cmsContentVo : cmsContentVoList) {
			constructContentVoForList(cmsContentVo, memberId);
		}
		initialOriginForlist(cmsContentVoList);
		return cmsContentVoList;
	}

	// @Scheduled(fixedDelay = 1000 * 60 * 1)
	// @CacheEvict(value = "cms_loadCmsContentByColumnId", allEntries = true)
	// public void loadCmsContentByColumnId_delete_from_cache() {
	// log.debug("----oadCmsContentByColumnId_delete_from_cache------------");
	// }

	// 按照频道查询
	// @Cacheable(value = "cms_loadCmsContentByColumnId", key =
	// "'loadCmsContentByColumnId_'+#cmsQueryDto.tocacheKey()")
	@Override
	public List<CmsContentVo> loadCmsContentByColumnId(HCmsQueryDto cmsQueryDto) {
		log.debug("==========cms_loadCmsContentByColumnId====================");
		List<Long> cmsCategoryIds = appFeignClient.loadCmsCateIdsByColumnId(cmsQueryDto.getColumnId());
		if (cmsCategoryIds == null) {
			return Lists.newArrayList();
		}
		List<Long> uninterestedContentIds = Lists.newArrayList();
		if (cmsQueryDto.getMemberId() != null) {
			uninterestedContentIds = cmsContentCacheProxyService.loadUninterestedContentIds(cmsQueryDto.getMemberId());
		}
		List<CmsContent> cmsContentList = cmsContentMng.loadCmsContentByCateIds(cmsQueryDto, cmsCategoryIds,
				uninterestedContentIds);
		if (cmsContentList == null || cmsContentList.isEmpty()) {
			return Lists.newArrayList();
		}

		List<CmsContentVo> cmsContentVoList = BeanMapper.mapList(cmsContentList, CmsContentVo.class);

		if (cmsContentVoList == null || cmsContentVoList.isEmpty()) {
			return Lists.newArrayList();
		}
		// ====================manuallySortList=========start============================================
		/*
		 * List<CmsContentVo> manuallySortList = cmsContentVoList.stream() .filter(val
		 * -> (val.getManuallySortNum() != null && val.getManuallySortNum().intValue()
		 * <= cmsContentVoList.size()))// 获取有排序字段的新闻,超过链表size的sortnum直接忽略 .sorted((v1,
		 * v2) -> { return v1.getManuallySortNum().intValue() -
		 * v2.getManuallySortNum().intValue(); })// 按照位置进行排序
		 * .collect(Collectors.toList());
		 * 
		 * 
		 * if (manuallySortList != null && !manuallySortList.isEmpty()) {
		 * List<CmsContentVo> manuallySortListCopy = new
		 * ArrayList<CmsContentVo>(manuallySortList);
		 * cmsContentVoList.removeAll(manuallySortListCopy);// 先从列表删除
		 * 
		 * for (CmsContentVo manuallySort : manuallySortList) {
		 * 
		 * cmsContentVoList.add(manuallySort.getManuallySortNum() - 1, manuallySort); }
		 * }
		 */
		// ====================manuallySortList=======end===============================================

		for (CmsContentVo cmsContentVo : cmsContentVoList) {
			constructContentVoForList(cmsContentVo, cmsQueryDto.getMemberId());
		}
		initialOriginForlist(cmsContentVoList);
		return cmsContentVoList;
	}

	// @Scheduled(fixedDelay = 1000 * 60 * 2)
	// @CacheEvict(value = "cms_cmsContentBySpecilId_1", allEntries = true)
	// public void loadCmsContentBySpecilId_delete_from_cache() {}
	//
	// // 按照专题查询
	// @Cacheable(value = "cms_cmsContentBySpecilId_1", key =
	// "'cms_cmsContentBySpecilId_1_'+#cmsQueryDto.tocacheKey()")
	@Override
	public List<CmsContentVo> loadCmsContentBySpecilId(HCmsQueryDto cmsQueryDto) {
		List<Long> cmsCategoryIds = cmsSpecialService.loadCmsCateIdsBySpecialId(cmsQueryDto.getSpecialId());
		if (cmsCategoryIds == null) {
			return null;
		}
		List<Long> uninterestedContentIds = Lists.newArrayList();
		if (cmsQueryDto.getMemberId() != null) {
			uninterestedContentIds = cmsContentCacheProxyService.loadUninterestedContentIds(cmsQueryDto.getMemberId());
		}
		// 过滤掉专题本身带的三条
		String specailInitIds = cmsQueryDto.getSpecailInitIds();
		if (specailInitIds != null && !specailInitIds.isEmpty()) {
			List<Long> specailInitIdsLong = Stream.of(specailInitIds.split(",")).map(val -> Long.valueOf(val))
					.collect(Collectors.toList());
			if (specailInitIdsLong != null && specailInitIdsLong.size() > 0) {
				uninterestedContentIds.addAll(specailInitIdsLong);
			}
		}
		// cmsQueryDto.setPageNo(1).setPageSize(200);//加载更多不分页

		List<CmsContent> cmsContentList = cmsContentMng.loadCmsContentByCateIds(cmsQueryDto, cmsCategoryIds,
				uninterestedContentIds);

		List<CmsContentVo> cmsContentVoList = BeanMapper.mapList(cmsContentList, CmsContentVo.class);
		if (cmsContentVoList == null || cmsContentVoList.isEmpty()) {
			return Lists.newArrayList();
		}
		for (CmsContentVo cmsContentVo : cmsContentVoList) {
			constructContentVoForList(cmsContentVo, cmsQueryDto.getMemberId());

		}
		initialOriginForlist(cmsContentVoList);
		return cmsContentVoList;
	}

	// @Scheduled(fixedDelay = 1000 * 60 * 3)
	// @CacheEvict(value = "cms_topNews", allEntries = true)
	// public void loadTopNews_delete_from_cache() {
	// log.debug("----loadTopNews_delete_from_cache------------");
	// }
	//
	// @Cacheable(value = "cms_topNews", key =
	// "'loadTopNews_'+#cmsQueryDto.memberId")
	@Override
	public List<CmsContentVo> loadTopNews(HCmsQueryDto cmsQueryDto, boolean enableUnintrested) {
		List<Long> uninterestedContentIds = Lists.newArrayList();
		if (enableUnintrested) {
			if (cmsQueryDto.getMemberId() != null) {
				uninterestedContentIds = cmsContentCacheProxyService
						.loadUninterestedContentIds(cmsQueryDto.getMemberId());
			}
		}

		List<CmsContent> cmsContentList = cmsContentMng.queryTopNewList(cmsQueryDto, uninterestedContentIds);
		// 直接去掉重复
		cmsContentList = cmsContentList.stream().distinct().collect(Collectors.toList());

		List<CmsContentVo> cmsContentVoList = BeanMapper.mapList(cmsContentList, CmsContentVo.class);
		if (cmsContentVoList == null || cmsContentVoList.isEmpty()) {
			return Lists.newArrayList();
		}
		// validateNewKeywordsExist-
		for (CmsContentVo cmsContentVo : cmsContentVoList) {
			constructContentVoForList(cmsContentVo, cmsQueryDto.getMemberId());
		}
		initialOriginForlist(cmsContentVoList);
		return cmsContentVoList;
	}

	@Override
	public List<CmsContentDto> loadUserCollectNews(HCmsQueryDto cmsQueryDto) {
		if (cmsQueryDto.getMemberId() == null) {
			return Lists.newArrayList();
		}
		// 用户行为类别0 详情查看， 1点赞 ，2 收藏，3评论，4爆料，5 (不感兴趣 内容太水，看过了)
		List<Long> contentIdList = appFeignClient
				.loadBehaviorContentIds(new AppBehaviorDto().setMemberId(cmsQueryDto.getMemberId())
						.setBevType(AppBehaviorDto.BevType.b2_sc).setPlateType(AppBehaviorDto.PlatType.b0_news));
		if (contentIdList != null && !contentIdList.isEmpty()) {

			contentIdList = contentIdList.stream().distinct().collect(Collectors.toList());
		}
		List<CmsContentDto> cmsContentList = cmsContentMng.loadCmsContentLists(cmsQueryDto, contentIdList);
		Set<Long> uniqueSet = Sets.newHashSet();
		List<CmsContentDto> cmsContentDtoList = Lists.newArrayList();
		for (CmsContentDto cmsContent : cmsContentList) {
			if (!uniqueSet.contains(cmsContent.getContentId())) {
				cmsContentDtoList.add(cmsContent);
				uniqueSet.add(cmsContent.getContentId());
			}
		}
		if (cmsContentDtoList == null || cmsContentDtoList.isEmpty()) {
			return Lists.newArrayList();
		}
		for (CmsContentDto cmsContentDto : cmsContentDtoList) {
			constructContentDtoForList(cmsContentDto, cmsQueryDto.getMemberId());
			cmsContentDto.setCollectionFlag(true);
		}
		initOriginForlist(cmsContentDtoList);
		return cmsContentDtoList;
	}

	// ================================private==========================================================

	private CmsContentVo constructContentVoForList(CmsContentVo cmsContentVo, Long memberId) {
		// 如果是视频的话--直接加载视频
		// if(cmsContentVo.getContenttypeId()!=null&&cmsContentVo.getContenttypeId().equals(new
		// Long(6))) {
		// String thumIds=cmsContentVo.getThumbIds();
		// String vedioUrl=fileInfoFeignClient.getVedioUrlsByids(thumIds);
		// cmsContentVo.setVedioUrl(vedioUrl);
		// }
		// 如果是视频
		if (cmsContentVo.getContenttypeId() != null && cmsContentVo.getContenttypeId().equals(new Long(6))) {
			String attIds = cmsContentVo.getAttachmentIds();
			if (org.springframework.util.StringUtils.hasLength(attIds)) {
				attIds = attIds.replaceAll(",", "");
				String vedioUrl = fileInfoFeignClient.getVedioUrlsByids(attIds);
				cmsContentVo.setVedioUrl(vedioUrl);
			}
		}
		// 处理封面--支持多封面
		if (org.springframework.util.StringUtils.hasLength(cmsContentVo.getThumbIds())) {
			String thumUrls = fileInfoFeignClient.getUrlByFileId(cmsContentVo.getThumbIds());
			cmsContentVo.setThumbUrl(thumUrls);
		}

		// 用户行为类别0 详情查看， 1点赞 ，2 收藏，3评论，4爆料，5 不感兴趣，6内容太水，7看过了
		Map<String, Integer> behaviorCountMap = appFeignClient.behaviorCount(cmsContentVo.getContentId(),
				AppBehaviorDto.PlatType.b0_news);
		cmsContentVo.setNewsCollectCounts(behaviorCountMap.get("2"));
		cmsContentVo.setNewsFollowCounts(behaviorCountMap.get("1"));
		// cmsContentVo.setNewsPostCounts(behaviorCountMap.get("3"));
		// cmsContentVo.setTopicCounts(behaviorCountMap.get("4"));

		Integer topicCount = snsFeignClient.topicCountsByNewIdFeginApi(cmsContentVo.getContentId());
		cmsContentVo.setTopicCounts(topicCount == null ? 0 : topicCount);
		Integer postCount = snsFeignClient.loadCountForNewsFeginApi(cmsContentVo.getContentId());
		cmsContentVo.setNewsPostCounts(postCount == null ? 0 : postCount);

		if (memberId != null) {
			String collectionFlag = appFeignClient.behaviorExist(
					new AppBehaviorDto(memberId, cmsContentVo.getContentId(), AppBehaviorDto.BevType.b2_sc)
							.setPlateType(AppBehaviorDto.PlatType.b0_news));
			cmsContentVo.setCollectionFlag("y".equals(collectionFlag));
		}
		// 置空地理位置信息
		if (cmsContentVo.getHiddenMap() != null && cmsContentVo.getHiddenMap() == 0) {
			cmsContentVo.setLocation("");
			cmsContentVo.setLatitude(0d);
			cmsContentVo.setLongitude(0d);
		}
		return cmsContentVo;
	}
	private CmsContentDto constructContentDtoForList(CmsContentDto cmsContentDto, Long memberId) {
		// 如果是视频
		if (cmsContentDto.getContenttypeId() != null && cmsContentDto.getContenttypeId().equals(new Long(6))) {
			String attIds = cmsContentDto.getAttachmentIds();
			if (org.springframework.util.StringUtils.hasLength(attIds)) {
				attIds = attIds.replaceAll(",", "");
				String vedioUrl = fileInfoFeignClient.getVedioUrlsByids(attIds);
				cmsContentDto.setVedioUrl(vedioUrl);
			}
		}
		// 处理封面--支持多封面
		if (org.springframework.util.StringUtils.hasLength(cmsContentDto.getThumbIds())) {
			String thumUrls = fileInfoFeignClient.getUrlByFileId(cmsContentDto.getThumbIds());
			cmsContentDto.setThumbUrl(thumUrls);
		}

		// 用户行为类别0 详情查看， 1点赞 ，2 收藏，3评论，4爆料，5 不感兴趣，6内容太水，7看过了
		Map<String, Integer> behaviorCountMap = appFeignClient.behaviorCount(cmsContentDto.getContentId(),
				AppBehaviorDto.PlatType.b0_news);
		cmsContentDto.setNewsCollectCounts(behaviorCountMap.get("2"));
		cmsContentDto.setNewsFollowCounts(behaviorCountMap.get("1"));

		Integer topicCount = snsFeignClient.topicCountsByNewIdFeginApi(cmsContentDto.getContentId());
		cmsContentDto.setTopicCounts(topicCount == null ? 0 : topicCount);
		Integer postCount = snsFeignClient.loadCountForNewsFeginApi(cmsContentDto.getContentId());
		cmsContentDto.setNewsPostCounts(postCount == null ? 0 : postCount);

		if (memberId != null) {
			String collectionFlag = appFeignClient.behaviorExist(
					new AppBehaviorDto(memberId, cmsContentDto.getContentId(), AppBehaviorDto.BevType.b2_sc)
							.setPlateType(AppBehaviorDto.PlatType.b0_news));
			cmsContentDto.setCollectionFlag("y".equals(collectionFlag));
		}
		// 置空地理位置信息
		if (cmsContentDto.getHiddenMap() != null && cmsContentDto.getHiddenMap() == 0) {
			cmsContentDto.setLocation("");
			cmsContentDto.setLatitude(0d);
			cmsContentDto.setLongitude(0d);
		}
		return cmsContentDto;
	}
	private void constructCmsContentDataVoForDetail(CmsContentDataVo cmsContentDataVo, CmsContent cmsContent,
			Long memberId) {
		cmsContentDataVo.setContenttypeId(cmsContent.getContenttypeId());
		cmsContentDataVo.setThumbUrl(cmsContent.getThumbUrl());
		cmsContentDataVo.setThumIds(cmsContent.getThumbIds());
		cmsContentDataVo.setLongitude(cmsContent.getLongitude());
		cmsContentDataVo.setLatitude(cmsContent.getLatitude());

		cmsContentDataVo.setSummary(cmsContent.getSummary());

		if (cmsContent.getReleaseTime() != null) {
			cmsContentDataVo.setReleaseTimeStr(TimeUtils.simpleTimeConverter(cmsContent.getReleaseTime()));
			cmsContentDataVo.setRangeNowTimeStr(TimeUtils.calculateTime(cmsContent.getReleaseTime()));
		}
		cmsContentDataVo.setLinkUrl(cmsContent.getLinkUrl());
		// 如果是视频的话--直接加载视频
		if (cmsContent.getContenttypeId() != null && cmsContent.getContenttypeId().equals(new Long(6))) {
			String attachmentIds = cmsContent.getAttachmentIds();
			if (attachmentIds != null) {
				attachmentIds = attachmentIds.replaceAll(",", "");
				String vedioUrl = fileInfoFeignClient.getVedioUrlsByids(attachmentIds);
				cmsContentDataVo.setVedioUrl(vedioUrl);
			}

		}
		// 如果是图集
		if (cmsContent.getContenttypeId() != null && cmsContent.getContenttypeId().equals(new Long(5))) {
			if (StringUtils.isNotEmpty(cmsContent.getAttachmentIds())) {
				String byids = fileInfoFeignClient.getUrlAndDescJsonsByIds(cmsContent.getAttachmentIds());
				if (StringUtils.isNotEmpty(byids)) {
					// JSONArray sss=JSONArray.parseArray(byids);
					List<Map> atlasContent = JSONArray.parseArray(byids, Map.class);// 过时方法
					cmsContentDataVo.setAtlasContent(atlasContent);
					// cmsContentDataVo.setContent(obj.toString());
				}
			}
		}

		// ThumbIds--缩略图
		// List<FileInfo> thumbFileList=
		// loadThumbFileInfoList(cmsContent.getThumbIds());
		// cmsContentDataVo.setFileInfoList(thumbFileList);
		// 处理封面--支持多封面
		if (org.springframework.util.StringUtils.hasLength(cmsContent.getThumbIds())) {
			String thumUrls = fileInfoFeignClient.getUrlByFileId(cmsContent.getThumbIds());
			cmsContentDataVo.setThumbUrl(thumUrls);
		}
		// 相关新闻
		// List<CmsContent>
		// abountNewList=cmsContentMng.loadAbountCmsContentList(cmsContent.getAboutNewIds());
		// cmsContentDataVo.setCmsContentList(abountNewList);

		//// 用户行为类别用户行为类别0 详情查看， 1点赞 ，2 收藏，3评论，4爆料，5不感兴趣 (不感兴趣，内容太水，看过了)
		Map<String, Integer> behaviorCountMap = appFeignClient.behaviorCount(cmsContent.getId(),
				AppBehaviorDto.PlatType.b0_news);

		cmsContentDataVo.setNewsCollectCounts(behaviorCountMap.get("2"));
		cmsContentDataVo.setNewsFollowCounts(behaviorCountMap.get("1"));

		// 评论需更改
		// cmsContentDataVo.setNewsPostCounts(behaviorCountMap.get("3"));
		// cmsContentDataVo.setTopicCounts(behaviorCountMap.get("4"));

		Integer topicCount = snsFeignClient.topicCountsByNewIdFeginApi(cmsContent.getContentId());
		cmsContentDataVo.setTopicCounts(topicCount == null ? 0 : topicCount);
		Integer postCount = snsFeignClient.loadCountForNewsFeginApi(cmsContent.getContentId());
		cmsContentDataVo.setNewsPostCounts(postCount == null ? 0 : postCount);

		//
		if (memberId != null) {
			AppBehaviorDto appBehaviorDto = new AppBehaviorDto().setMemberId(memberId).setContentId(cmsContent.getId())
					.setBevType(2);
			String collectionFlag = appFeignClient
					.behaviorExist(appBehaviorDto.setPlateType(AppBehaviorDto.PlatType.b0_news));
			cmsContentDataVo.setCollectionFlag("y".equals(collectionFlag));
		}

		if (memberId != null) {
			AppBehaviorDto appBehaviorDto2 = new AppBehaviorDto().setMemberId(memberId).setContentId(cmsContent.getId())
					.setBevType(1);
			String followFlag = appFeignClient
					.behaviorExist(appBehaviorDto2.setPlateType(AppBehaviorDto.PlatType.b0_news));
			cmsContentDataVo.setFollowFlag("y".equals(followFlag));
		}
		if (cmsContentDataVo.getHiddenMap() != null && cmsContentDataVo.getHiddenMap() == 0) {
			cmsContentDataVo.setLatitude(0d);
			cmsContentDataVo.setLongitude(0d);
		}
	}

	private void constructCmsContentDataDtoForDetail(CmsContentDataDto cmsContentDataDto, CmsContentDto cmsContent,
													Long memberId) {
		cmsContentDataDto.setContenttypeId(cmsContent.getContenttypeId());
		cmsContentDataDto.setThumbUrl(cmsContent.getThumbUrl());
		cmsContentDataDto.setThumIds(cmsContent.getThumbIds());
		cmsContentDataDto.setLongitude(cmsContent.getLongitude());
		cmsContentDataDto.setLatitude(cmsContent.getLatitude());

		cmsContentDataDto.setSummary(cmsContent.getSummary());

		if (cmsContent.getReleaseTime() != null) {
			cmsContentDataDto.setReleaseTimeStr(TimeUtils.simpleTimeConverter(cmsContent.getReleaseTime()));
			cmsContentDataDto.setRangeNowTimeStr(TimeUtils.calculateTime(cmsContent.getReleaseTime()));
		}
		cmsContentDataDto.setLinkUrl(cmsContent.getLinkUrl());
		// 如果是视频的话--直接加载视频
		if (cmsContent.getContenttypeId() != null && cmsContent.getContenttypeId().equals(new Long(6))) {
			String attachmentIds = cmsContent.getAttachmentIds();
			if (attachmentIds != null) {
				attachmentIds = attachmentIds.replaceAll(",", "");
				String vedioUrl = fileInfoFeignClient.getVedioUrlsByids(attachmentIds);
				cmsContentDataDto.setVedioUrl(vedioUrl);
			}

		}
		// 如果是图集
		if (cmsContent.getContenttypeId() != null && cmsContent.getContenttypeId().equals(new Long(5))) {
			if (StringUtils.isNotEmpty(cmsContent.getAttachmentIds())) {
				String byids = fileInfoFeignClient.getUrlAndDescJsonsByIds(cmsContent.getAttachmentIds());
				if (StringUtils.isNotEmpty(byids)) {
					// JSONArray sss=JSONArray.parseArray(byids);
					List<Map> atlasContent = JSONArray.parseArray(byids, Map.class);// 过时方法
					cmsContentDataDto.setAtlasContent(atlasContent);
					// cmsContentDataDto.setContent(obj.toString());
				}
			}
		}

		// ThumbIds--缩略图
		// List<FileInfo> thumbFileList=
		// loadThumbFileInfoList(cmsContent.getThumbIds());
		// cmsContentDataDto.setFileInfoList(thumbFileList);
		// 处理封面--支持多封面
		if (org.springframework.util.StringUtils.hasLength(cmsContent.getThumbIds())) {
			String thumUrls = fileInfoFeignClient.getUrlByFileId(cmsContent.getThumbIds());
			cmsContentDataDto.setThumbUrl(thumUrls);
		}
		// 相关新闻
		// List<CmsContent>
		// abountNewList=cmsContentMng.loadAbountCmsContentList(cmsContent.getAboutNewIds());
		// cmsContentDataDto.setCmsContentList(abountNewList);

		//// 用户行为类别用户行为类别0 详情查看， 1点赞 ，2 收藏，3评论，4爆料，5不感兴趣 (不感兴趣，内容太水，看过了)
		Map<String, Integer> behaviorCountMap = appFeignClient.behaviorCount(cmsContent.getId(),
				AppBehaviorDto.PlatType.b0_news);

		cmsContentDataDto.setNewsCollectCounts(behaviorCountMap.get("2"));
		cmsContentDataDto.setNewsFollowCounts(behaviorCountMap.get("1"));

		// 评论需更改
		// cmsContentDataDto.setNewsPostCounts(behaviorCountMap.get("3"));
		// cmsContentDataDto.setTopicCounts(behaviorCountMap.get("4"));

		Integer topicCount = snsFeignClient.topicCountsByNewIdFeginApi(cmsContent.getContentId());
		cmsContentDataDto.setTopicCounts(topicCount == null ? 0 : topicCount);
		Integer postCount = snsFeignClient.loadCountForNewsFeginApi(cmsContent.getContentId());
		cmsContentDataDto.setNewsPostCounts(postCount == null ? 0 : postCount);

		//
		if (memberId != null) {
			AppBehaviorDto appBehaviorDto = new AppBehaviorDto().setMemberId(memberId).setContentId(cmsContent.getId())
					.setBevType(2);
			String collectionFlag = appFeignClient
					.behaviorExist(appBehaviorDto.setPlateType(AppBehaviorDto.PlatType.b0_news));
			cmsContentDataDto.setCollectionFlag("y".equals(collectionFlag));
		}

		if (memberId != null) {
			AppBehaviorDto appBehaviorDto2 = new AppBehaviorDto().setMemberId(memberId).setContentId(cmsContent.getId())
					.setBevType(1);
			String followFlag = appFeignClient
					.behaviorExist(appBehaviorDto2.setPlateType(AppBehaviorDto.PlatType.b0_news));
			cmsContentDataDto.setFollowFlag("y".equals(followFlag));
		}
		if (cmsContentDataDto.getHiddenMap() != null && cmsContentDataDto.getHiddenMap() == 0) {
			cmsContentDataDto.setLatitude(0d);
			cmsContentDataDto.setLongitude(0d);
		}
	}

	private List<SnsPostDto> loadNewsPostListByNewsId(HBaseSnsQueryDto snsQueryDto) {
		if (snsQueryDto.getPageSize() == null || snsQueryDto.getPageNo() == null) {
			return Lists.newArrayList();
		}
		List<SnsPostDto> snsPostVoList = new ArrayList<SnsPostDto>();
		snsPostVoList = snsFeignClient.newsPostList(snsQueryDto);
		return snsPostVoList;
	}

	private List<SnsTopicDto> loadNewsTopicListByNewsId(Long contentId, Long currentUserId) {
		List<SnsTopicDto> snsTopicList = new ArrayList<SnsTopicDto>();
		if (contentId == null) {
			return snsTopicList;
		}
		// 最多给三个爆料
		HBaseSnsQueryDto snsQueryDto = HBaseSnsQueryDto.newForNewsQuery(contentId, currentUserId).setPageNo(1)
				.setPageSize(3);
		snsTopicList = snsFeignClient.newsTpoicList(snsQueryDto);
		return snsTopicList;
	}
	// private List<FileInfo> loadThumbFileInfoList(String ids ){
	// if (StringUtils.isNotEmpty(ids)) {
	// List<FileInfo> fileInfoList =
	// fileInfoFeignClient.queryFileInfoListByIds(ids);
	// return fileInfoList;
	// } else {
	// return Lists.newArrayList();
	// }
	// }

	// private List<Long> loadCmsCateIdsByColumnId(Long columnId){
	// if(columnId!=null) {
	// return appFeignClient.loadCmsCateIdsByColumnId(columnId);
	// }
	// return Lists.newArrayList();
	// }
	// =====================================home======================================================================================================
	// 上滑动--查询推荐历史
	@Override
	public List<Map<String, Object>> homeCmsSnsUpPull(HCmsIndexDto queryDto) {
		Set<Long> uniqueSet = Sets.newHashSet();
		List<Map<String, Object>> dataSource = Lists.newArrayList();

		String indexIds = queryDto.getIndexIds();
		log.debug("indexIds:" + indexIds);
		List<Long> indexIdList = Lists.newArrayList();
		if (indexIds != null && org.springframework.util.StringUtils.hasText(indexIds)) {
			indexIdList = Stream.of(indexIds.split(",")).map(val -> Long.valueOf(val)).collect(Collectors.toList());
		}
		// 从es里获取推荐历史
		HSearchDto HSearchDto = new HSearchDto().setMemberId(queryDto.getMemberId()).setImei(queryDto.getImei())
				.setHisids(indexIdList).setPageNo(queryDto.getPageNo()).setPageSize(queryDto.getPageSize());

		List<ESRecommendHis> hisList = elsFeignClient.searchRecommHisByPage(HSearchDto);

		if (hisList == null || hisList.isEmpty()) {
			return dataSource;
		}
		log.debug("AAAAA===hisList:" + String.join(",",
				hisList.stream().map(val -> String.valueOf(val.getContentId())).collect(Collectors.toList())));

		// 分三类进行处理
		List<Long> cmsContentVoIdList = hisList.stream().filter(val -> val.getType().intValue() == 1)
				.map(val -> val.getContentId()).collect(Collectors.toList());
		List<Long> cmsSpecialVoIdList = hisList.stream().filter(val -> val.getType().intValue() == 2)
				.map(val -> val.getContentId()).collect(Collectors.toList());
		List<Long> topicVoIdList = hisList.stream().filter(val -> val.getType().intValue() == 3)
				.map(val -> val.getContentId()).collect(Collectors.toList());
		// log.debug("AAAAA===cmsContentVoIdList:" + String.join(",",
		// cmsContentVoIdList.stream().map(val ->
		// String.valueOf(val)).collect(Collectors.toList())));
		// log.debug("AAAAA===cmsSpecialVoIdList:" + String.join(",",
		// cmsSpecialVoIdList.stream().map(val ->
		// String.valueOf(val)).collect(Collectors.toList())));
		// log.debug("AAAAA===topicVoIdList:" + String.join(",",
		// topicVoIdList.stream().map(val ->
		// String.valueOf(val)).collect(Collectors.toList())));

		cmsContentVoIdList = cmsContentVoIdList.stream().distinct().collect(Collectors.toList());
		cmsSpecialVoIdList = cmsSpecialVoIdList.stream().distinct().collect(Collectors.toList());
		topicVoIdList = topicVoIdList.stream().distinct().collect(Collectors.toList());
		// log.debug("BBBBB===cmsContentVoIdList:" + String.join(",",
		// cmsContentVoIdList.stream().map(val ->
		// String.valueOf(val)).collect(Collectors.toList())));
		// log.debug("BBBBB===cmsSpecialVoIdList:" + String.join(",",
		// cmsSpecialVoIdList.stream().map(val ->
		// String.valueOf(val)).collect(Collectors.toList())));
		// log.debug("BBBBB===topicVoIdList:" + String.join(",",
		// topicVoIdList.stream().map(val ->
		// String.valueOf(val)).collect(Collectors.toList())));

		// 加载新闻
		List<CmsContentVo> cmsContentVoList = this.loadCmsContentByIds(cmsContentVoIdList, queryDto.getMemberId(),
				true);
		// 推荐历史上拉刷新添加新闻状态过滤防止新闻被删除---仍然出现在推荐历史的情况
		try {
			cmsContentVoList = cmsContentVoList.stream()
					.filter(val -> val.getStatus() != null && val.getStatus().intValue() == 1)
					.collect(Collectors.toList());
		} catch (Exception e) {

		}

		// log.debug("CCCCC===cmsContentVoList:" + String.join(",",
		// cmsContentVoList.stream().map(val ->
		// String.valueOf(val.getContentId())).collect(Collectors.toList())));
		for (CmsContentVo vo : cmsContentVoList) {
			Map<String, Object> cmsContentVoItem = Maps.newHashMap();
			cmsContentVoItem.put("type", "1");
			cmsContentVoItem.put("content", vo);
			if (!uniqueSet.contains(vo.getContentId())) {
				dataSource.add(cmsContentVoItem);
				uniqueSet.add(vo.getContentId());
			}

		}
		// log.debug("DDDDDD===dataSource:" + String.join(",",
		// dataSource.stream()
		// .map(val -> "" + ((CmsContentVo)
		// val.get("content")).getContentId()).collect(Collectors.toList())));

		// 加载专题
		for (Long spid : cmsSpecialVoIdList) {
			HCmsSpecialListVo cmsSpecialVo = cmsSpecialService.loadCmsSpecialListVoById(new HCmsQueryDto()
					.setSpecialId(spid).setMemberId(queryDto.getMemberId()).setPageNo(1).setPageSize(1));
			if (cmsSpecialVo != null) {
				Map<String, Object> cmsSpecialVoItem = Maps.newHashMap();
				cmsSpecialVoItem.put("type", "2");
				cmsSpecialVoItem.put("contents", cmsSpecialVo);
				dataSource.add(cmsSpecialVoItem);
			}
		}
		// log.debug("EEEE===dataSource:" + String.join(",", dataSource.stream()
		// .map(val -> "" + ((CmsContentVo)
		// val.get("content")).getContentId()).collect(Collectors.toList())));

		List<Long> topicVoIdList_2 = Lists.newArrayList();

		Map<Long, CmsContentData> dataContainer = Maps.newHashMap();

		for (Long cmsContentId : topicVoIdList) {
			CmsContentData cmsDataFromSns = cmsContentDataMng.loadCmsContentDataByContentId(cmsContentId);
			if (cmsDataFromSns != null) {
				topicVoIdList_2.add(cmsDataFromSns.getSnsOrginId());
				dataContainer.put(cmsDataFromSns.getSnsOrginId(), cmsDataFromSns);
			}
		}

		// 加载新鲜事
		HBaseSnsQueryFeginApiDto queryFeginApiDto = new HBaseSnsQueryFeginApiDto().setMemberId(queryDto.getMemberId())
				.setImei(queryDto.getImei()).setLatitude(queryDto.getLatitude()).setLongitude(queryDto.getLongitude())
				.setTopicIds(topicVoIdList_2);

		// vo.getContentId()
		List<SnsTopicDto> snsTopicDtos = snsFeignClient.loadTopicByIdsFeginApi(queryFeginApiDto);
		// zhaoke
		List<SnsTopicVo> recommTopicHisList = BeanMapper.mapList(snsTopicDtos, SnsTopicVo.class);
		for (SnsTopicVo vo : recommTopicHisList) {
			CmsContentData cmsDataFromSns = dataContainer.get(vo.getTopicId());
			vo.setTitle(cmsDataFromSns.getContent());
			vo.setContent(cmsDataFromSns.getContent());
			Map<String, Object> topicVoItem = Maps.newHashMap();
			topicVoItem.put("type", "3");
			topicVoItem.put("content", vo);
			dataSource.add(topicVoItem);
		}
		// log.debug("FFFFFF===dataSource:" + String.join(",", dataSource.stream()
		// .map(val -> "" +
		// ((CmsContentVo)val.get("content")).getContentId()).collect(Collectors.toList())));

		return dataSource;
	}

	// ===============================================================================
	// 0~200m 权重需要 ≥1 近在咫尺，目光可及
	// 200m~2km 权重需要 ≥2 就在身边，步行可及
	// 2km~10km 权重需要 ≥3 同一片区域
	// 10km~40km（市） 权重需要≥4 同一个低级行政单位
	// 40km~300km（省/州/欧洲一个国） 权重需要≥5 同一个中级行政单位
	// 300km~3000km（国/欧盟） 权重需要≥6 同一个高级行政单位
	// 3000km+ 权重需要≥7 全球范围
	private double priorityToDistance(Integer priority) {
		priority = priority == null ? 10 : priority;
		switch (priority) {
		case 1:
			return 200D;
		case 2:
			return 2000D;
		case 3:
			return 10000D;
		case 4:
			return 40000D;
		case 5:
			return 300000D;
		case 6:
			return 3000000D;
		case 7:
			return 3000001D;
		default:
			return 3000001D;
		}
	}

	private double cacuateScore(HCmsIndexDto queryDto, CmsContentVo vo) {
		Double lat = queryDto.getLatitude();// 经度
		Double lon = queryDto.getLongitude();
		if (lat == null || lon == null) {
			lat = 39.924896D;
			lon = 116.403694D;
		}
		// 距离
		double distance = 1;
		try {
			distance = LocationUtils.getDistance(lat.doubleValue(), lon.doubleValue(), Double.valueOf(vo.getLatitude()),
					Double.valueOf(vo.getLongitude()));
		} catch (Exception e) {
			distance = 1;
		}
		// 权重
		double priorityToDistance = (vo.getPriority() == null || vo.getPriority() > 12) ? 12 : vo.getPriority();
		// 约定的公示
		double res = Math.sqrt(Math.pow(Math.log(distance * 0.3), 2) + Math.pow(priorityToDistance, 2));
		return res;
	}

	private List<CmsContentVo> filterAndConstructColumnData1(int finalPutDataCount, Set<Long> contentIdsHaveUsed,
			List<CmsContentVo> cmsContentVoList) {

		List<CmsContentVo> voList = cmsContentVoList.subList(0, finalPutDataCount);

		return voList;
	}

	private List<CmsContentVo> filterAndConstructColumnData(int finalPutDataCount, Set<Long> contentIdsHaveUsed,
			List<Long> contentIdsInReHis, List<CmsContentVo> cmsContentVoList) {

		List<CmsContentVo> voList = new ArrayList<CmsContentVo>();
		// --第一次循环-------本页面没有而且历史里也没有的--
		for (int i = 0; i < finalPutDataCount; i++) {// 要凑够这个数
			for (CmsContentVo vo : cmsContentVoList) {
				if (!contentIdsHaveUsed.contains(vo.getContentId()) && !contentIdsInReHis.contains(vo.getContentId())) {
					voList.add(vo);
					contentIdsHaveUsed.add(vo.getContentId());
					if (voList.size() >= finalPutDataCount) {
						return voList;
					}
				}
			}
		}
		// 循环2-数据没有凑够数的话----本页面没有没有的--
		if (voList.size() < finalPutDataCount) {// 经过循环1之后还不够的话---
			for (CmsContentVo vo : cmsContentVoList) {
				if (!contentIdsHaveUsed.contains(vo.getContentId()) && (!voList.contains(vo))) {
					voList.add(vo);
					contentIdsHaveUsed.add(vo.getContentId());
					if (voList.size() >= finalPutDataCount) {
						return voList;
					}
				}
			}
		}
		// 循环3-数据没有凑够数的话----把数据填充进去--
		// if(voList.size()<finalPutDataCount) {//经过循环2之后还不够的话---返回数据列表没有的就加进去
		// for(CmsContentVo vo: cmsContentVoList) {
		// if((!voList.contains(vo))) {
		// voList.add(vo);
		// if(voList.size()>=finalPutDataCount) {
		// return voList;
		// }
		// }
		// }
		// }
		return voList;
	}

	@Async
	public void asyncHis(List<ESRecommendHis> recommendHisList) {
		elsFeignClient.createRecommHis(recommendHisList);
	}

	@Override
	public List<Map<String, Object>> initialOrigin(List<Map<String, Object>> cmsContentVos) {
		for (Map<String, Object> val : cmsContentVos) {
			Object obj = val.get("content");
			if (obj != null && obj instanceof CmsContentVo) {
				CmsContentVo cmsContentVo = (CmsContentVo) obj;
				CmsOriginDto originDto = cmsOriginMng.getCmsOringByContentId(cmsContentVo.getContentId());
				//  zhaoke
				CmsOrigin origin = new CmsOrigin();
				try {
					BeanUtils.copyProperties(origin,originDto);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				cmsContentVo.setOrigin(origin);
			}
		}
		return cmsContentVos;
	}

	/**
	 * 初始化文章来源
	 * @param cmsContentList
	 * @return
	 */
	@Override
	public List<CmsContentVo> initialOriginForlist(List<CmsContentVo> cmsContentList) {
		cmsContentList.parallelStream().forEach(cmsContentVo -> {
			if (cmsContentVo.getContentId() != null) {
				CmsOriginDto originDto = cmsOriginMng.getCmsOringByContentId(cmsContentVo.getContentId());
				//  zhaoke
				CmsOrigin origin = new CmsOrigin();
				try {
					BeanUtils.copyProperties(origin,originDto);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				cmsContentVo.setOrigin(origin);
			}
		});
		return cmsContentList;
	}

	public List<CmsContentDto> initOriginForlist(List<CmsContentDto> cmsContentList) {
		cmsContentList.parallelStream().forEach(cmsContentDto -> {
			if (cmsContentDto.getContentId() != null) {
				CmsOriginDto origin = cmsOriginMng.getCmsOringByContentId(cmsContentDto.getContentId());
				cmsContentDto.setOrigin(origin);
			}
		});
		return cmsContentList;
	}

	// 每十分钟取最近两天的新闻
	@Scheduled(fixedRate = 1000 * 60 * 10)
	public List<CmsContentVo> loadCmsContent() {
		log.debug("==========cms_loadCmsContentByColumnId====================");
		/*
		 * List<Long> cmsCategoryIds =
		 * cmsContentCacheProxyService.loadCmsCateIdsByColumnId(cmsQueryDto.
		 * getColumnId()); if (cmsCategoryIds == null) { return Lists.newArrayList(); }
		 * List<Long> uninterestedContentIds = Lists.newArrayList(); if
		 * (cmsQueryDto.getMemberId() != null) { uninterestedContentIds =
		 * cmsContentCacheProxyService.loadUninterestedContentIds(cmsQueryDto.
		 * getMemberId()); }
		 */
		// List<CmsContent> cmsContentList =
		// cmsContentMng.loadCmsContentByCateIds(cmsQueryDto,
		// cmsCategoryIds,uninterestedContentIds);
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - 2);
		Finder f = Finder.create("from CmsContent where auditStatus=1 and status=1 and releaseTime>:releaseTimeP")
				.setParam("releaseTimeP", new Date(now.getTimeInMillis()));
		// Finder f = Finder.create("from CmsContent where auditStatus=1 and status=1
		// ");

		List<CmsContent> cmsContentList = (List<CmsContent>) cmsContentMng.find(f, 1, 1000).getList();
		if (cmsContentList == null || cmsContentList.isEmpty()) {
			return Lists.newArrayList();
		}

		List<CmsContentVo> cmsContentVoList = BeanMapper.mapList(cmsContentList, CmsContentVo.class);
		// ====================manuallySortList=========start============================================
		/*
		 * List<CmsContentVo> manuallySortList = cmsContentVoList.stream() .filter(val
		 * -> (val.getManuallySortNum() != null && val.getManuallySortNum().intValue()
		 * <= cmsContentVoList.size()))// 获取有排序字段的新闻,超过链表size的 // sortnum // 直接忽略
		 * .sorted((v1, v2) -> { return v1.getManuallySortNum().intValue() -
		 * v2.getManuallySortNum().intValue(); })// 按照位置进行排序
		 * .collect(Collectors.toList());
		 * 
		 * if (manuallySortList != null && !manuallySortList.isEmpty()) {
		 * cmsContentVoList.removeAll(manuallySortList);// 先从列表删除 // 按照位置重新插入列表 for
		 * (CmsContentVo manuallySort : manuallySortList) {
		 * cmsContentVoList.add(manuallySort.getManuallySortNum() - 1, manuallySort); }
		 * }
		 */
		// ====================manuallySortList=======end===============================================

		for (CmsContentVo cmsContentVo : cmsContentVoList) {
			constructContentVoForList(cmsContentVo, null);
		}

		ZSetOperations zSetOperations = writeRedisTemplate.opsForZSet();
		if (cmsContentVoList != null && cmsContentVoList.size() > 0) {
			zSetOperations.removeRangeByScore("PersonalizedRecommendation", 0, now.getTimeInMillis());
		}
		for (CmsContentVo cmsContentVo : cmsContentVoList) {
			if (cmsContentVo != null && cmsContentVo.getReleaseTime() != null) {
				zSetOperations.add("PersonalizedRecommendation", cmsContentVo, cmsContentVo.getReleaseTime().getTime());
			}
		}

		return cmsContentVoList;
	}

	@SuppressWarnings("rawtypes")
	@Autowired
	@Qualifier("readRedisTemplate")
	RedisTemplate readRedisTemplate;
	
	
	@SuppressWarnings("rawtypes")
	@Autowired
	@Qualifier("writeRedisTemplate")
	RedisTemplate writeRedisTemplate;	
	
//	@Autowired
//	RedisTemplate redisTemplate;

	@Override
	public List<Map<String, Object>> homeCmsSns(HCmsIndexDto queryDto, List<Long> toNewsIds) {
		// TODO Auto-generated method stub
		ZSetOperations zSetOperations = readRedisTemplate.opsForZSet();
		Calendar minCal = Calendar.getInstance();
		minCal.set(Calendar.HOUR_OF_DAY, 0);
		minCal.set(Calendar.SECOND, 0);
		minCal.set(Calendar.MINUTE, 0);
		minCal.set(Calendar.MILLISECOND, 0);
		minCal.set(Calendar.DATE, minCal.get(Calendar.DATE) - 2);// 两天前
		long min = minCal.getTimeInMillis();
		Calendar maxCal = Calendar.getInstance();
		maxCal.set(Calendar.HOUR_OF_DAY, 24);
		maxCal.set(Calendar.SECOND, 0);
		maxCal.set(Calendar.MINUTE, 0);
		maxCal.set(Calendar.MILLISECOND, 0);
		long max = maxCal.getTimeInMillis();
		Set<CmsContentVo> personalizedRecommendation = zSetOperations.rangeByScore("PersonalizedRecommendation", min,
				max);

		long a = System.currentTimeMillis();
		log.debug("-----------start-||" + a / 1000);
		Long memberId = queryDto.getMemberId();
		String imei = queryDto.getImei();
		// 最近的500条推荐记录--尽量不重复出现在首页中
		List<Long> contentIdsInReHis = elsFeignClient
				.searchRecommHis(new HSearchDto().setMemberId(memberId).setImei(imei).setPageNo(1).setPageSize(500));// .setPlateType(0)
		long b = System.currentTimeMillis();
		log.debug("-----after---elsFeignClient.searchRecommHis.----|t1: +" + (b - a) / 1000 + "||t2: " + (b - a) / 1000
				+ "");

		// 过滤推荐过的内容
		// filtePersonalizedRecommendation(personalizedRecommendation,
		// contentIdsInReHis);
		personalizedRecommendation = personalizedRecommendation.stream()
				.filter(val -> (!contentIdsInReHis.contains(val.getContentId()))).collect(Collectors.toSet());

		Set<Long> contentIdsIndexHaveUsed = Sets.newHashSet();// 用来存放在频道里已经有的新闻id，防止后面的频道里
		// 出现重复的新闻
		// 这是置顶的新闻id
		contentIdsIndexHaveUsed.addAll(toNewsIds);
		// 返回数据
		List<Map<String, Object>> dataListGloble = Lists.newArrayList();
		// 本次推荐的id---要同步到es中
		List<ESRecommendHis> thisTimeRecommendHis = Lists.newArrayList();
		// List<Long> indexIds=Lists.newArrayList();//存放本屏数据
		// 后台设置的每个频道最多推荐数
		List<AppRecommConfigDto> recommAllLists = appFeignClient.loadAllRecomm();
		recommAllLists = recommAllLists.stream().filter(val -> val.getRecommNum() != null && val.getRecommNum() > 0)
				.collect(Collectors.toList());
		long c = System.currentTimeMillis();
		log.debug("-----after---elsFeignClient.loadAllRecomm.----|t1: +" + (c - b) / 1000 + "||t2: " + (c - a) / 1000
				+ "");
		// 栏目推荐=================================》目列表 1 栏目 2 专题 3
		List<Map<String, Object>> dataListForColumnRecomm = Lists.newArrayList();//
		// 过滤出频道的
		List<AppRecommConfigDto> recommColumnLists = recommAllLists.stream()
				.filter(val -> val.getType().intValue() == 1).collect(Collectors.toList());
		if (recommColumnLists != null && recommColumnLists.size() > 0) {
			Set<CmsContentVo> everyRecommContent = null;
			List<CmsContentVo> cmsContentVoList = null;
			// 在这里是频道循环
			for (AppRecommConfigDto recomm : recommColumnLists) {
				int recommNum = recomm.getRecommNum() == null ? 0 : recomm.getRecommNum();
				log.debug(recomm.getColumnId() + "================recommNum:" + recommNum);
				List<Long> cmsCategoryIds = appFeignClient.loadCmsCateIdsByColumnId(recomm.getColumnId());
				log.debug(cmsCategoryIds + "对应的cms分类");
				// 可推荐的两天内的新闻
				if (personalizedRecommendation != null && personalizedRecommendation.size() > 0) {
					// personalizedRecommendation
					// .forEach(val -> log.debug("推荐的id:" + val.getContentId() + "-" +
					// val.getCategoryId()));
					everyRecommContent = personalizedRecommendation.stream()
							.filter(val -> cmsCategoryIds.contains(val.getCategoryId())).collect(Collectors.toSet());
					everyRecommContent = everyRecommContent.stream()
							.filter(val -> (!contentIdsIndexHaveUsed.contains(val.getCategoryId())))
							.collect(Collectors.toSet());
					// 计算到底放多少条数据
					int finalPutDataCount = recommNum < everyRecommContent.size() ? recommNum
							: (everyRecommContent != null ? everyRecommContent.size() : 0);

					// 排序逻辑
					if (queryDto.getLongitude() != null && queryDto.getLatitude() != null) {
						// 按照公式进行排序
						cmsContentVoList = everyRecommContent.stream().sorted((v1, v2) -> {
							double s1 = cacuateScore(queryDto, v1);
							double s2 = cacuateScore(queryDto, v2);
							return s1 < s2 ? 1 : 0;
						}).collect(Collectors.toList());
					}

					// 取推荐最大个数的稿件
					List<CmsContentVo> voListAfterFilter = filterAndConstructColumnData1(finalPutDataCount,
							contentIdsIndexHaveUsed, cmsContentVoList);
					log.debug("================everyRecommContent:" + everyRecommContent.size()
							+ "================cmsContentVoList:" + cmsContentVoList.size()
							+ "================finalPutDataCount:" + finalPutDataCount
							+ "================voListAfterFilter:" + voListAfterFilter.size());
					// 对确认的推荐进行组装
					for (CmsContentVo vo : voListAfterFilter) {
						// if(contentIdsIndexHaveUsed.contains(vo.getContentId()))
						// {
						// continue;
						// }
						if (vo.getSnsOrginId() != null) {// 内容化过来的数据

							HBaseSnsQueryFeginApiDto queryFeginApiDto = new HBaseSnsQueryFeginApiDto()
									.setMemberId(queryDto.getMemberId()).setImei(queryDto.getImei())
									.setLatitude(queryDto.getLatitude()).setLongitude(queryDto.getLongitude())
									.setTopicIds(Lists.newArrayList(vo.getSnsOrginId()));
							// zhaoke
							List<SnsTopicDto> snsTopicDtos = snsFeignClient.loadTopicByIdsFeginApi(queryFeginApiDto);
							List<SnsTopicVo> recommTopicList = BeanMapper.mapList(snsTopicDtos, SnsTopicVo.class);
							if (recommTopicList != null && !recommTopicList.isEmpty()) {
								SnsTopicVo snsvo = recommTopicList.get(0);
								// snsvo.setTopicId(vo.getSnsOrginId());
								snsvo.setTitle(vo.getContent());
								snsvo.setContent(vo.getContent());
								Map<String, Object> topicVoItem = Maps.newHashMap();
								topicVoItem.put("type", "3");
								topicVoItem.put("content", snsvo);
								dataListForColumnRecomm.add(topicVoItem);
								thisTimeRecommendHis.add(new ESRecommendHis(memberId, imei, 3, vo.getContentId(), 0));
								contentIdsIndexHaveUsed.add(vo.getContentId());
							}

						} else {

							Map<String, Object> cmsContentVoItem = Maps.newHashMap();
							cmsContentVoItem.put("type", "1");
							cmsContentVoItem.put("content", vo);
							dataListForColumnRecomm.add(cmsContentVoItem);
							thisTimeRecommendHis.add(new ESRecommendHis(memberId, imei, 1, vo.getContentId(), 0));
							contentIdsIndexHaveUsed.add(vo.getContentId());
						}

					}
				}
				everyRecommContent = null;
				cmsContentVoList = null;
			}
		}
		dataListGloble.addAll(dataListForColumnRecomm);
		long d = System.currentTimeMillis();
		log.debug("-----after---dataListForColumnRecomm----|t1: +" + (d - c) / 1000 + "||t2: " + (d - a) / 1000 + "");
		// =============栏目推荐结束=====================================

		// 专题 =======================》 1 栏目 2 专题 3 新鲜事
		List<Map<String, Object>> dataListForSpecial = Lists.newArrayList();// 专题数据容器

		List<AppRecommConfigDto> specialRecommList = recommAllLists.stream()
				.filter(val -> val.getType().intValue() == 2).collect(Collectors.toList());
		if (specialRecommList != null && specialRecommList.size() > 0) {
			Integer specialRecommNum = specialRecommList.get(0).getRecommNum();// 推荐的专题数量
			if (specialRecommNum != null && specialRecommNum.intValue() > 0) {

				// List<CmsSpecial> spcialList=
				// cmsSpecialService.loadNotTopLeveSpecialList(Short.valueOf("1"));
				// 只有顶级专题才会被推荐
				List<CmsSpecial> spcialList = cmsSpecialService.loadFirstLeveSpecialList(Short.valueOf("1"));
				if (spcialList != null && !spcialList.isEmpty()) {
					List<CmsSpecial> recommSpList = spcialList.stream()
							.sorted((v1, v2) -> v2.getRecommPriority() - v1.getRecommPriority())
							.collect(Collectors.toList());
					if (recommSpList != null && recommSpList.size() > specialRecommNum) {
						recommSpList = recommSpList.subList(0, specialRecommNum);
					}
					for (CmsSpecial sp : recommSpList) {
						HCmsSpecialListVo cmsSpecialVo = cmsSpecialService
								.loadCmsSpecialListVoById(new HCmsQueryDto().setSpecialId(sp.getSpecialId())
										.setMemberId(queryDto.getMemberId()).setPageNo(1).setPageSize(1));
						if (cmsSpecialVo != null) {
							Map<String, Object> cmsSpecialVoItem = Maps.newHashMap();
							cmsSpecialVoItem.put("type", "2");
							cmsSpecialVoItem.put("contents", cmsSpecialVo);
							dataListForSpecial.add(cmsSpecialVoItem);

							thisTimeRecommendHis.add(new ESRecommendHis(memberId, imei, 2, sp.getSpecialId(), 3));
							contentIdsIndexHaveUsed.add(sp.getSpecialId());
						}
					}

				}
			}
		}
		dataListGloble.addAll(dataListForSpecial);
		long e = System.currentTimeMillis();
		log.debug("-----after---dataListForSpecial----|t1: +" + (e - d) / 1000 + "||t2: " + (e - a) / 1000 + "");

		asyncHis(thisTimeRecommendHis);
		log.debug("-----after---asyncHis----||" + (System.currentTimeMillis() - a) / 1000);
		Map<String, Object> lastOne = Maps.newHashMap();
		lastOne.put("indexIds", contentIdsIndexHaveUsed);
		dataListGloble.add(lastOne);
		return dataListGloble;
	}
}
