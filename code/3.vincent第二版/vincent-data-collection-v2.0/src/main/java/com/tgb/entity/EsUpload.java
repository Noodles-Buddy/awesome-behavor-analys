package com.tgb.entity;

//用于将前端分析拿到的数据上传到es当中
public class EsUpload {
	private String id;
	private String args;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getArgs() {
		return args;
	}
	public void setArgs(String args) {
		this.args = args;
	}

}
