package com.hww.cms.webservice.service;

import com.hww.cms.common.dto.CmsContentDataDto;
import com.hww.cms.common.dto.CmsContentDto;
import com.hww.cms.common.dto.HCmsIndexDto;
import com.hww.cms.common.dto.HCmsQueryDto;
import com.hww.cms.common.vo.CmsContentVo;

import java.util.List;
import java.util.Map;

public interface CmsContentService {

	// CmsContentData loadCmsContentDataById(Long newsId);

	// CmsContent loadCmsContentById(Long newsId);

	CmsContentDataDto loadNewsDetail(HCmsQueryDto cmsQueryDto);

	// CmsContentDataVo loadAtlasDetail(HCmsQueryDto cmsQueryDto);

	List<CmsContentVo> loadTopNews(HCmsQueryDto cmsQueryDto, boolean enableUnintrested);

	List<CmsContentDto> loadUserCollectNews(HCmsQueryDto cmsQueryDto);

	// 按照频道查询
	List<CmsContentVo> loadCmsContentByColumnId(HCmsQueryDto cmsQueryDto);

	// 按照专题查询
	List<CmsContentVo> loadCmsContentBySpecilId(HCmsQueryDto cmsQueryDto);
	
	// List<CmsContentVo> loadCmsContentByContentType(HCmsQueryDto cmsQueryDto);

	List<CmsContentVo> loadCmsContentByIds(List<Long> contentIds, Long memberId, boolean enableUnintrested);

	// List<CmsContentDataVo> searchNews(SearchNewsVo vo);

	List<Map<String, Object>> homeCmsSns(HCmsIndexDto cmsQueryDto, List<Long> toNewsIds);

	List<Map<String, Object>> homeCmsSnsUpPull(HCmsIndexDto queryDto);

	CmsContentDataDto loadNewsDetailForShare(Long contentId);

	CmsContentDataDto loadNewsDetailWithoutPostAndTopic(HCmsQueryDto cmsQueryDto);

	/**
	 * 该方法，只根据分页查询所有新闻,并且是时间倒序，最新的新闻显示在最上边
	 * @param queryDto
	 * @return
	 */
	List<CmsContentDto> listAllCmsContentByPage(HCmsQueryDto queryDto);


	/**
	 * 对推荐出来的数据去重
	 * @param homeCmsSnsList 除开置顶新闻的列表
	 * @param topNewsList 置顶新闻
	 * @return
	 */
	List<Map<String, Object>> distinctRecommendData(List<Map<String, Object>> homeCmsSnsList,List<CmsContentVo> topNewsList);

    List<Map<String,Object>> initialOrigin(List<Map<String, Object>> distinctHomeCmsSnsList);

    List<CmsContentVo> initialOriginForlist(List<CmsContentVo> cmsContentList);

	

    // Pagination listContent(CmsContentDto cmsContentDto);

	// List<CmsContentVo> queryPromoteNewList(QueryContentVo vo);

	// List<CmsContentVo> queryOtherNews(QueryContentVo vo);

	// CmsMemberBehaviorVo newFollow(CmsMemberBehaviorVo vo);

	// void newUninterested(CmsMemberBehaviorVo vo);

	// SnsTopicVo saveNewTopic(SnsTopicVo vo);

	// List<SnsTopicVo> newTopicList(Long newId, Long userId, String imei);

	// CmsMemberBehaviorVo newCollection(CmsMemberBehaviorVo vo);

	// boolean newPostFollow(ThumbUpVo vo);

	// boolean newTopicFollow(ThumbUpVo vo);

	// List<SnsPostVo> newPostList(SnsPostVo vo);

	// SnsTopicVo newTopicDetail(SnsTopicVo vo);

	// SnsPostVo newPostDetail(SnsPostVo vo);

	// SnsPostVo newPostOnReview(SnsPostVo vo);

	// List<SnsTopicVo> searchTopics(SearchNewsVo vo);

	// List<CmsContentDataVo> searchVideos(SearchNewsVo vo);

	// boolean removePost(SnsPostVo vo);

	// boolean removeTopic(SnsTopicVo vo);

	// List<CmsContentVo> cmsContentCategory(CmsContentVo vo);

	// List<UCenterMemberVo> searchMembers(SearchNewsVo vo);

	// /**
	// * 置顶新闻
	// *
	// * @author lyb
	// * @email 674142624@qq.com
	// * @date 2017年12月14日 下午3:39:46
	// * @return
	// * @version v0.1
	// */
	// List<CmsContentVo> topCmsContentList();

	// List<Map<String,Object>> homeCmsSns(CmsContentVo vo);

	// /**
	// * 专题下新闻更多
	// *
	// * @author lyb
	// * @email 674142624@qq.com
	// * @date 2017年12月15日 下午6:22:55
	// * @return
	// * @version v0.1
	// */
	// List<CmsContentVo> contentsBycategoryId(CmsContentVo vo);

	// List<NearTopicVo> cmsNearPeoples(NearTopicVo vo);

	// List<CmsContentVo> nearContents(CmsContentVo vo);
}
