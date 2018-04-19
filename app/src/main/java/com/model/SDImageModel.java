package com.model;

public class SDImageModel{
	public String sdImage;
	public boolean checked;
	public SDImageModel()
	{
		checked = false;
	}
	public String getSdImageUrl(DevModel model){
		return "http://"+model.IPUID+":"+model.WebPort+""+sdImage;
	}
	public String getSdImageName(){
		String[] array = sdImage.split("/");
		return array[array.length-1];
	}
	public boolean isSDImage(){
		return sdImage.contains("jpg");
	}
}