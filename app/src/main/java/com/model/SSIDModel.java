package com.model;

import java.io.UnsupportedEncodingException;

/**
 * Created by gyl1 on 4/1/17.
 */

public class SSIDModel
{
  public String SSID;
  public int Siganl;

  public String getMSSID()
  {
    //其内容必须为iso8859-1编码),可能会通过将中文字符按照字节方式来编码的 ==ansi???
    try
    {
      return new String(SSID.getBytes("utf8"),"utf8");
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
      return null;
    }
  }

}
