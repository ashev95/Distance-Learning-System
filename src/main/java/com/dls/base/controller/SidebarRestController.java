package com.dls.base.controller;

import com.dls.base.ui.sidebar.extra.TreeItem;
import com.dls.base.ui.sidebar.extra.TreeRoot;
import com.dls.base.utils.AccessUtils;
import com.dls.base.utils.Utils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

@RestController
@RequestMapping("view/sidebar")
public class SidebarRestController
{

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	Utils utils;

	@Autowired
	AccessUtils accessUtils;

	private void checkRoleRecursive(ArrayList <TreeItem> treeItemArrayList){
		//
		Iterator <TreeItem> i = treeItemArrayList.iterator();
		while (i.hasNext()) {
			TreeItem treeItem = i.next();
			boolean isEnd = false;
			if (treeItem.children != null){
				isEnd = (treeItem.children.size() == 0);
			}else{
				isEnd = true;
			}
			if (isEnd){
				if (!accessUtils.hasAtLeastRole(treeItem.roles)){
					i.remove();
				}
			}else{
				checkRoleRecursive(treeItem.children);
			}
		}
		//
	}

	/*
	private int getMaxLevelRecursive(ArrayList <TreeItem> treeItemArrayList){
		if (treeItemArrayList == null){
			return 1;
		}
		if (treeItemArrayList.size() == 0){
			return 1;
		}
		int max = 0;
		for (TreeItem treeItem : treeItemArrayList){
			int cur = getMaxLevelRecursive(treeItem.children);
			if (max < cur){
				max = cur;
			}
		}
		return max + 1;
	}

	private int getMaxLevel(TreeRoot treeRoot){
		return (getMaxLevelRecursive(treeRoot.items) - 1);
	}
	 */

	private void filterChilds(TreeItem current, Iterator currentIterator){
		if (current.children == null && current.uid != null){
			if (!accessUtils.hasAtLeastRole(current.roles)){
				currentIterator.remove();
			}
		}else{
			Iterator it = current.children.iterator();
			while (it.hasNext())
			{
				TreeItem treeItem = (TreeItem)it.next();
				filterChilds(treeItem, it);
			}
			if (current.children.size() == 0){
				currentIterator.remove();
			}
		}
	}

	@GetMapping(value = "/extra", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
	public String getExtra() {
		TreeRoot treeRoot = null;
		try {
			XmlMapper xmlMapper = new XmlMapper();
			final Resource fileResource = resourceLoader.getResource("/WEB-INF/sidebar_config/extra.xml");
			File file = fileResource.getFile();
			String xml = utils.inputStreamToString(new FileInputStream(file));
			treeRoot = xmlMapper.readValue(xml, TreeRoot.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// get all target childs


		Iterator it = treeRoot.items.iterator();
		while (it.hasNext())
		{
			TreeItem treeItem = (TreeItem)it.next();
			filterChilds(treeItem, it);
		}

		/*
		int maxLevel = getMaxLevel(treeRoot);

		for (int increment = 0; increment < (maxLevel - 1); increment++){
			checkRoleRecursive(treeRoot.items);
		}

		Iterator <TreeItem> i = treeRoot.items.iterator();
		while (i.hasNext()) {
			TreeItem treeItem = i.next();
			if (treeItem.children == null) {
				i.remove();
			} else if (treeItem.children.size() == 0) {
				i.remove();
			} else if (!accessUtils.hasAtLeastRole(treeItem.roles)) {
				i.remove();
			}
		}
		 */

		String json = null;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			json = mapper.writeValueAsString(treeRoot);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return json;
	}
}