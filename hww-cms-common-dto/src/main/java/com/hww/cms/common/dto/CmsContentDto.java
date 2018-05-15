package com.hww.cms.common.dto;

import com.hww.base.common.dto.AbsBaseDto;
import com.hww.file.common.dto.FileInfoDto;
import com.hww.sns.common.dto.SnsPostDto;
import com.hww.sns.common.dto.SnsTopicDto;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class CmsContentDto
        extends
        AbsBaseDto<Long> {

    private Long contentId; //id
    private Integer auditStatus; //审核状态
    private Long categoryId; // 内容分类id
    private String title; // 标题
    private String shortTitle; // 短标题
    private String summary; //摘要
    private Integer parentId; // 父分类id
    private String content; //内容
    private String categoryName; // 内容名称
    private Integer lft; // 分类节点左值
    private Integer rgt; // 分类节点右值
    private Integer priority; // 顺序
    private Short display; // 是否显示
    private Integer depth; // 深度
    private String description; // 描述
    private String outLink; // 外链- 当typeId为外部链接时
    private Short typeId; // 区分单页、外部链接、列表、专题
    private String allIDCheck; // 选中id列表
    private Integer siteId;// 站点id
    private Short status;// 状态
    private Timestamp createTime;// 创建时间
    private Timestamp lastModifyTime;// 最后修改时间
    private Integer pageNo; // 页码
    private Integer pageSize; //每页条数
    private Integer auditHisRecord;// 审核次数
    private Long contenttypeId; /// 内容类型id  2图文 5图集  6视频 7内容化
    private String thumbUrl; // 缩略图地址
    private String thumbIds;//  thumbIds 视频id
    private Double longitude;// 经度
    private Double latitude;// 纬度

    private String location; // 位置
    private Timestamp releaseTime; // 发布时间
    private String linkUrl; // 外部链接地址
    private String attachmentIds; // 附件id串
    /**
     * 是否显示地图 0不显示 1 显示
     */
    private Integer hiddenMap;

    private String tagIds; // 标签id字符串
    private String keywordIds; // 关键字id串
    private String originUrl;
    private CmsOriginDto origin;// 来源
    private Long srcCategoryId;// 原始分类id
    private Integer userId;// 系统原始录入人 责任编辑
    private String author;// 原文作者
    private List<Map> atlasContent;

    private Long snsTopicId;// 评论主题id
    private String attachmentUrls;

    private String thumIds;//  thumbIds 视频id
    private String vedioUrl;
    private String videoLength;

    private boolean collectionFlag; // 新闻收藏状态 true为已收藏
    private boolean followFlag; //新闻点赞状态 true为已点赞
    private Integer topicCounts; // 新闻爆料数
    private Integer newsPostCounts; // 新闻评论数
    private Integer newsFollowCounts; // 新闻点赞数
    private Integer newsCollectCounts;//收藏数

    private String createTimeStr; // 创建时间字符串
    private String rangeNowTimeStr; // 距离当前时间字符串
    private String releaseTimeStr; // 发布时间字符串
    private CmsCategoryDto cmsCategoryDto; // 新闻内容分类vo


    private List<SnsPostDto> snsPostVoList; // 新闻评论列表list SnsPostVo
    private List<FileInfoDto> fileInfoList; // 文件列表list FileInfo
    // start  zhaoke
    private List<CmsObjectDto> fileImgVoList; // (图片列表list|图集) FileImgVo
    // end zhaoke
    private List<CmsContentDto> cmsContentList; // 相关新闻列表list CmsContent
    private List<SnsTopicDto> snsTopicList; // 新闻爆料列表list SnsTopicVo
    private Integer newPostFollowCounts; //新闻评论点赞数

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getLft() {
        return lft;
    }

    public void setLft(Integer lft) {
        this.lft = lft;
    }

    public Integer getRgt() {
        return rgt;
    }

    public void setRgt(Integer rgt) {
        this.rgt = rgt;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Short getDisplay() {
        return display;
    }

    public void setDisplay(Short display) {
        this.display = display;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutLink() {
        return outLink;
    }

    public void setOutLink(String outLink) {
        this.outLink = outLink;
    }

    public Short getTypeId() {
        return typeId;
    }

    public void setTypeId(Short typeId) {
        this.typeId = typeId;
    }

    public String getAllIDCheck() {
        return allIDCheck;
    }

    public void setAllIDCheck(String allIDCheck) {
        this.allIDCheck = allIDCheck;
    }

    @Override
    public Integer getSiteId() {
        return siteId;
    }

    @Override
    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @Override
    public Short getStatus() {
        return status;
    }

    @Override
    public void setStatus(Short status) {
        this.status = status;
    }

    @Override
    public Timestamp getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public Timestamp getLastModifyTime() {
        return lastModifyTime;
    }

    @Override
    public void setLastModifyTime(Timestamp lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getAuditHisRecord() {
        return auditHisRecord;
    }

    public void setAuditHisRecord(Integer auditHisRecord) {
        this.auditHisRecord = auditHisRecord;
    }

    public Long getContenttypeId() {
        return contenttypeId;
    }

    public void setContenttypeId(Long contenttypeId) {
        this.contenttypeId = contenttypeId;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getThumbIds() {
        return thumbIds;
    }

    public void setThumbIds(String thumbIds) {
        this.thumbIds = thumbIds;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Timestamp getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Timestamp releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(String attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public Integer getHiddenMap() {
        return hiddenMap;
    }

    public void setHiddenMap(Integer hiddenMap) {
        this.hiddenMap = hiddenMap;
    }

    public String getTagIds() {
        return tagIds;
    }

    public void setTagIds(String tagIds) {
        this.tagIds = tagIds;
    }

    public String getKeywordIds() {
        return keywordIds;
    }

    public void setKeywordIds(String keywordIds) {
        this.keywordIds = keywordIds;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public CmsOriginDto getOrigin() {
        return origin;
    }

    public void setOrigin(CmsOriginDto origin) {
        this.origin = origin;
    }

    public Long getSrcCategoryId() {
        return srcCategoryId;
    }

    public void setSrcCategoryId(Long srcCategoryId) {
        this.srcCategoryId = srcCategoryId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Map> getAtlasContent() {
        return atlasContent;
    }

    public void setAtlasContent(List<Map> atlasContent) {
        this.atlasContent = atlasContent;
    }

    public Long getSnsTopicId() {
        return snsTopicId;
    }

    public void setSnsTopicId(Long snsTopicId) {
        this.snsTopicId = snsTopicId;
    }

    public String getAttachmentUrls() {
        return attachmentUrls;
    }

    public void setAttachmentUrls(String attachmentUrls) {
        this.attachmentUrls = attachmentUrls;
    }

    public String getThumIds() {
        return thumIds;
    }

    public void setThumIds(String thumIds) {
        this.thumIds = thumIds;
    }

    public String getVedioUrl() {
        return vedioUrl;
    }

    public void setVedioUrl(String vedioUrl) {
        this.vedioUrl = vedioUrl;
    }

    public String getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(String videoLength) {
        this.videoLength = videoLength;
    }

    public boolean isCollectionFlag() {
        return collectionFlag;
    }

    public void setCollectionFlag(boolean collectionFlag) {
        this.collectionFlag = collectionFlag;
    }

    public boolean isFollowFlag() {
        return followFlag;
    }

    public void setFollowFlag(boolean followFlag) {
        this.followFlag = followFlag;
    }

    public Integer getTopicCounts() {
        return topicCounts;
    }

    public void setTopicCounts(Integer topicCounts) {
        this.topicCounts = topicCounts;
    }

    public Integer getNewsPostCounts() {
        return newsPostCounts;
    }

    public void setNewsPostCounts(Integer newsPostCounts) {
        this.newsPostCounts = newsPostCounts;
    }

    public Integer getNewsFollowCounts() {
        return newsFollowCounts;
    }

    public void setNewsFollowCounts(Integer newsFollowCounts) {
        this.newsFollowCounts = newsFollowCounts;
    }

    public Integer getNewsCollectCounts() {
        return newsCollectCounts;
    }

    public void setNewsCollectCounts(Integer newsCollectCounts) {
        this.newsCollectCounts = newsCollectCounts;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getRangeNowTimeStr() {
        return rangeNowTimeStr;
    }

    public void setRangeNowTimeStr(String rangeNowTimeStr) {
        this.rangeNowTimeStr = rangeNowTimeStr;
    }

    public String getReleaseTimeStr() {
        return releaseTimeStr;
    }

    public void setReleaseTimeStr(String releaseTimeStr) {
        this.releaseTimeStr = releaseTimeStr;
    }

    public CmsCategoryDto getCmsCategoryDto() {
        return cmsCategoryDto;
    }

    public void setCmsCategoryDto(CmsCategoryDto cmsCategoryDto) {
        this.cmsCategoryDto = cmsCategoryDto;
    }

    public List<SnsPostDto> getSnsPostVoList() {
        return snsPostVoList;
    }

    public void setSnsPostVoList(List<SnsPostDto> snsPostVoList) {
        this.snsPostVoList = snsPostVoList;
    }

    public List<FileInfoDto> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(List<FileInfoDto> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public List<CmsObjectDto> getFileImgVoList() {
        return fileImgVoList;
    }

    public void setFileImgVoList(List<CmsObjectDto> fileImgVoList) {
        this.fileImgVoList = fileImgVoList;
    }

    public List<CmsContentDto> getCmsContentList() {
        return cmsContentList;
    }

    public void setCmsContentList(List<CmsContentDto> cmsContentList) {
        this.cmsContentList = cmsContentList;
    }

    public List<SnsTopicDto> getSnsTopicList() {
        return snsTopicList;
    }

    public void setSnsTopicList(List<SnsTopicDto> snsTopicList) {
        this.snsTopicList = snsTopicList;
    }

    public Integer getNewPostFollowCounts() {
        return newPostFollowCounts;
    }

    public void setNewPostFollowCounts(Integer newPostFollowCounts) {
        this.newPostFollowCounts = newPostFollowCounts;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
