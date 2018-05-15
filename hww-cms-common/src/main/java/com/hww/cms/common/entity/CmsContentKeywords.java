package com.hww.cms.common.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hww.base.common.entity.AbsBaseEntity;

@Entity
@Table(name = "cms_content_keywords")

public class CmsContentKeywords extends AbsBaseEntity<Long>{
	
	private static final long serialVersionUID = 1L;
	
	private Long keywordId;
	private String keywordContent;
	private Short status;
	private Timestamp createTime;
	private Timestamp lastModifyTime;	
	
	@Id
    @GeneratedValue(generator="snowFlake")
    @GenericGenerator(name="snowFlake", strategy="com.hww.framework.common.idgen.SnowFlakeIdGenerator")
	@Column(name = "keyword_id")
	public Long getKeywordId() {
		return keywordId;
	}
	public void setKeywordId(Long keywordId) {
		this.keywordId = keywordId;
	}
	
	@Column(name = "keyword_content")
	public String getKeywordContent() {
		return keywordContent;
	}
	public void setKeywordContent(String keywordContent) {
		this.keywordContent = keywordContent;
	}
	
	@Column(name = "status")
	public Short getStatus() {
		return status;
	}
	
	public void setStatus(Short status) {
		this.status = status;
	}
	
	@Column(name = "create_time")
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	@Column(name = "last_modify_t")
	public Timestamp getLastModifyTime() {
		return lastModifyTime;
	}
	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
	
	@Override
	@Transient
	public Long getId() {
		return keywordId;
	}
	
}
