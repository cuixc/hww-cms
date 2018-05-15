package com.hww.cms.webservice.test;

import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.hww.cms.CmsWebServiceApplication;

import java.io.Serializable;
//import com.hww.cms.webservice.controller.CmsColumnController;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = CmsWebServiceApplication.class)
//@WebAppConfiguration
public class CmsCategoryQueryTest {

	/*@Autowired
	private CmsColumnController cmsColumnController;

	@Test
	public void test() {
//		cmsColumnController.query();
		cmsColumnController.editColumn(9, 1);
		//cmsColumnController.myColumn(9);
	}*/

	public static void main(String[] args) {

		NodeSocial nodeSocial=new NodeSocial();
		nodeSocial.setContentName("success");
		nodeSocial.setContentTitle("fail");
		System.out.println(JSON.toJSONString(nodeSocial));
		System.out.println(JSON.parse(JSON.toJSONString(nodeSocial)));

		String html="<p>xxxxxxx</p>";
		html= Jsoup.parse(html).body().html();
		System.out.println(html);



	}

	static class NodeSocial implements Serializable{

		private String name;
		private String title;

		public String getContentName() {
			return name;
		}

		public void setContentName(String name) {
			this.name = name;
		}

		public String getContentTitle() {
			return title;
		}

		public void setContentTitle(String title) {
			this.title = title;
		}

		public String getContentTitle1() {
			return title;
		}

		public void setContentTitle1(String title) {
			this.title = title;
		}

	}

	static class NodeContent implements  Serializable{

		private String lh;
		private String rr;

		public String getContentName() {
			return lh;
		}

		public void setContentName(String name) {
			this.lh = name;
		}

		public String getContentTitle() {
			return rr;
		}

		public void setContentTitle(String title) {
			this.rr = title;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("NodeContent{");
			sb.append("lh='").append(lh).append('\'');
			sb.append(", rr='").append(rr).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

}
