package com.hww.cms.webadmin.service.impl;

import com.hww.cms.common.dao.CmsCategoryDao;
import com.hww.cms.common.entity.CmsCategory;
import com.hww.cms.common.entity.CmsContent;
import com.hww.cms.common.entity.CmsContentData;
import com.hww.cms.common.manager.CmsContentDataMng;
import com.hww.cms.webadmin.service.CmsRewriteHtml;
import com.hww.file.api.FileFeignClient;
import com.hww.file.common.dto.FileInfoDto;
import com.hww.sys.common.dto.SysUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

@Service
public class CmsRewriteHtmlImpl implements CmsRewriteHtml {

    private static final Logger log = LoggerFactory.getLogger(CmsRewriteHtmlImpl.class);

    @Value("${contentXmlFilePath}")
    private String xmlPath;
    @Value("${fileServicePrefix}")
    private String fileServicePrefix;

    @Autowired
    private FileFeignClient fileFeignClient;
    @Autowired
    private CmsCategoryDao cmsCategoryDao;

    @Autowired
    private CmsContentDataMng cmsContentDataMng;

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Override
    public void writeToHtml(CmsContent cmsContent, CmsContentData cmsContentData, SysUserDto user, String operation) {
        String path = xmlPath;
        int year = LocalDate.now().getYear();
        path = path + year + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        int isLink = 1;
        //php那边cms设计是原始分类的话，算外链
        if (cmsContent.getCategoryId().equals(cmsContentData.getSrcCategoryId()))
            isLink = 0;
        file = new File(file, cmsContent.getContentId() + "-" + isLink + "-" + cmsContent.getCategoryId() + ".xml");
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element news = document.createElement("news");

            Element op = setChild("op", "操作类型", operation, document);
            news.appendChild(op);
            String statusStr = null;
            if (cmsContent.getStatus() == 1)
                statusStr = "99";
            else {
                statusStr = "-1";
            }
            Element status = setChild("status", "文章状态", statusStr, document);
            news.appendChild(status);

            Element id = setChild("id", "文章id", cmsContent.getContentId() + "", document);
            news.appendChild(id);

            Element roleid = setChild("roleid", "角色ID", user.getDefaultRole() + "", document);
            news.appendChild(roleid);
            Element userid = setChild("userid", "发布人", user.getUserId() + "", document);
            news.appendChild(userid);

            Element releasid = setChild("releasid", "审核人ID", user.getUserId() + "", document);
            news.appendChild(releasid);

            Element catid = setChild("catid", "栏目ID", cmsContent.getCategoryId() + "", document);
            news.appendChild(catid);
            CmsCategory cmsCategory = cmsCategoryDao.getCmsCategoryById(cmsContent.getCategoryId());
            if (cmsCategory != null) {
                Element catName = setChild("catName", "栏目名字", cmsCategory.getCategoryName() + "", document);
                news.appendChild(catName);
            } else {
                log.error("静态化文件异常，category not find,contentId:{},categoryId:{}", cmsContent.getContentId(),
                        cmsContent.getCategoryId());
                return;
            }

            Element title = setChild("title", "标题", cmsContent.getTitle() + "", document);
            news.appendChild(title);

            Element pretitle = setChild("pretitle", "肩标题", "", document);
            news.appendChild(pretitle);

            Element subtitle = setChild("subtitle", "副标题", "", document);
            news.appendChild(subtitle);

            Element shorttitle = setChild("shorttitle", "短标题", cmsContentData.getShortTitle(), document);
            news.appendChild(shorttitle);

            Element keywords = setChild("keywords", "关键字", cmsContentData.getKeywordIds(), document);
            news.appendChild(keywords);

            Element author = setChild("author", "作者", cmsContentData.getAuthor(), document);
            news.appendChild(author);

            Element copyfrom = setChild("copyfrom", "来源", cmsContentData.getOrigin().getOriginName() + "", document);
            news.appendChild(copyfrom);

            Element weight = setChild("weight", "权重", cmsContent.getPriority() + "", document);
            news.appendChild(weight);

			/*
             * Element thumb = setChild("thumb", "缩略图", "" , document);
			 * news.appendChild(thumb);
			 */

            Element thumb = document.createElement("thumb");
            Element name = document.createElement("name");
            name.setTextContent("缩略图");
            thumb.appendChild(name);
            if (cmsContent.getThumbUrl() != null) {
                String[] thumbs = cmsContent.getThumbUrl().split(",");
                for (String url : thumbs) {
                    Element value = document.createElement("value");
                    value.setTextContent(fileServicePrefix + url);
                    thumb.appendChild(value);
                }
            }
            news.appendChild(thumb);

            Element relation = setChild("relation", "相关文章", "", document);
            news.appendChild(relation);

            Element inputtime = setChild("inputtime", "发布时间", cmsContent.getReleaseTime() + "", document);
            news.appendChild(inputtime);
            Element islink = setChild("islink", "跳转链接标记", isLink + "", document);
            news.appendChild(islink);

            Element linkurl = setChild("linkurl", "跳转链接", cmsContent.getLinkUrl() + "", document);
            news.appendChild(linkurl);

            Element template = setChild("template", "模板", "", document);
            news.appendChild(template);

            Element allow_comment = setChild("allow_comment", "允许评论", "", document);
            news.appendChild(allow_comment);

            Element posids = setChild("posids", "推荐位ID", cmsContent.getManuallySortNum() + "", document);
            news.appendChild(posids);

            Element description = setChild("description", "摘要", cmsContent.getSummary() + "", document);
            news.appendChild(description);
            String contentDataStr = cmsContentData.getContent();
            contentDataStr = "<![CDATA[" + contentDataStr + "]]>";
            Element content = setChild("content", "正文", contentDataStr, document);
            news.appendChild(content);

            Element pictureurls = document.createElement("pictureurls");
            name = document.createElement("name");
            name.setTextContent("附件图片");
            pictureurls.appendChild(name);

            if (cmsContent.getContenttypeId() == 5) {
                List<FileInfoDto> fileInfos = fileFeignClient.listFileInfoByIds(cmsContent.getAttachmentIds());
                if (fileInfos != null && fileInfos.size() > 0) {
                    for (FileInfoDto fileInfo : fileInfos) {
                        Element value = document.createElement("value");
                        value.setTextContent(fileServicePrefix + fileInfo.getFileVisitUrl());
                        pictureurls.appendChild(value);
                        Element describle = document.createElement("describle");
                        describle.setTextContent(fileInfo.getFileDesc());
                        pictureurls.appendChild(describle);

                    }
                }

            } else {
                Element value = document.createElement("value");
                value.setTextContent("");
                pictureurls.appendChild(value);
            }
            news.appendChild(pictureurls);

            Element videourl = document.createElement("videourl");
            name = document.createElement("name");
            name.setTextContent("视频地址");
            videourl.appendChild(name);

            if (cmsContent.getContenttypeId() == 6) {
                String videoUrl = fileFeignClient.getUrlByVideoId(cmsContent.getAttachmentIds());
                if (videoUrl != null) {
                    String[] picUrls = videoUrl.split(",");
                    for (String url : picUrls) {
                        Element value = document.createElement("value");
                        value.setTextContent(fileServicePrefix + url);
                        videourl.appendChild(value);

                    }
                }

            } else {
                Element value = document.createElement("value");
                value.setTextContent("");
                videourl.appendChild(value);
            }
            news.appendChild(videourl);

            Element extended = setChild("extended", "扩展字段", "", document);
            news.appendChild(extended);

            Element paginationtype = setChild("paginationtype", "内容分页", "", document);
            news.appendChild(paginationtype);

            document.appendChild(news);
            writeToLocal(document, file);

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            log.error("解析静态化配置异常!{}", e);
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            log.error("上传静态化文件异常:{}", e);
        }

    }

    @Override
    public void deleteHtml(CmsContent cmsContent) {
        String path = xmlPath;
        int year = LocalDate.now().getYear();
        path = path + year + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        CmsContentData cmsContentData = cmsContentDataMng.loadCmsContentDataByContentId(cmsContent.getContentId());
        int isLink = 1;
        //php那边cms设计是原始分类的话，算外链
        if (cmsContent.getCategoryId().equals(cmsContentData.getSrcCategoryId()))
            isLink = 0;
        file = new File(file, cmsContent.getContentId() + "-" + isLink + "-" + cmsContent.getCategoryId() + ".xml");
        DocumentBuilder db = null;
        try {
            db = factory.newDocumentBuilder();
            Document xmldoc = db.parse(file);
            Element root = xmldoc.getDocumentElement();
            NodeList op = root.getElementsByTagName("op");
            NodeList childs = op.item(0).getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node node = childs.item(i);
                if ("value".equals(node.getNodeName())) {
                    node.setTextContent("delete");
                    break;
                }
            }
            writeToLocal(xmldoc, file);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    private Element setChild(String elementName, String nameStr, String valueStr, Document document) {
        Element elment = document.createElement(elementName);

        Element name = document.createElement("name");
        if (elementName.equals("op")) {
            name = document.createElement("ename");
        }
        Element value = document.createElement("value");
        name.setTextContent(nameStr);
        value.setTextContent(valueStr);
        elment.appendChild(name);
        elment.appendChild(value);
        return elment;

    }

    /**
     * 写入硬盘
     *
     * @param document
     * @param file
     * @throws TransformerException
     */
    private void writeToLocal(Document document, File file) throws TransformerException {
        // 创建TransformerFactory对象
        TransformerFactory tff = TransformerFactory.newInstance();

        // 创建Transformer对象
        Transformer tf = tff.newTransformer();

        // 设置输出数据时换行
        tf.setOutputProperty(OutputKeys.INDENT, "yes");

        // 使用Transformer的transform()方法将DOM树转换成XML
        tf.transform(new DOMSource(document), new StreamResult(file));
    }
}
