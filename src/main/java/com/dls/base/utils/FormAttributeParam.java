package com.dls.base.utils;

import java.util.HashMap;

class FormAttributeParam {

	private HashMap<String, String> skipList = new HashMap<String, String>();
    private HashMap<String, String> exList = new HashMap<String, String>();

		public FormAttributeParam toSkip(String key){
			skipList.put(key, "");
			return this;
		}

		public FormAttributeParam toEx(String key){
			exList.put(key, "");
			return this;
		}

		public boolean hasSkip(String key){
			return (this.skipList.containsKey(key));
		}

		public boolean hasEx(String key){
			return (this.exList.containsKey(key));
		}

	}