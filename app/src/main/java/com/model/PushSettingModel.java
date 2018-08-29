/**
 * Copyright 2018 bejson.com
 */
package com.model;

import stcam.stcamproject.Application.STApplication;
import stcam.stcamproject.R;

/**
 * Auto-generated: 2018-06-24 15:34:27
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class PushSettingModel
{

  private int PushActive;
  private int PushInterval;
  private int PushIntervalLevel;
  private int PIRSensitive;

  public void setPushActive(int PushActive)
  {
    this.PushActive = PushActive;
  }

  public int getPushActive()
  {
    return PushActive;
  }

  public void setPushInterval(int PushInterval)
  {
    this.PushInterval = PushInterval;
  }

  public int getPushInterval()
  {
    return PushInterval;
  }

  public void setPIRSensitive(int PIRSensitive)
  {
    this.PIRSensitive = PIRSensitive;
  }

  public int getPIRSensitive()
  {
    return PIRSensitive;
  }

  public String getPushActiveDesc()
  {
    if (PushActive == 1)
    {
      return STApplication.getInstance().getString(R.string.action_open);
    }
    else
    {
      return STApplication.getInstance().getString(R.string.action_close);
    }
  }

  public String getPushIntervalDesc()
  {
    if (PushInterval < 60)
    {
      return PushInterval + STApplication.getInstance().getString(R.string.string_second);
    }
    else
    {
      return PushInterval / 60 + STApplication.getInstance().getString(R.string.string_miniute);
    }
  }

  public String getPIRSensitiveDesc()
  {
    if (PIRSensitive == 0)
    {
      return STApplication.getInstance().getString(R.string.action_level_low);
    }
    else if (PIRSensitive == 1)
    {
      return STApplication.getInstance().getString(R.string.action_level_middle);
    }
    else if (PIRSensitive == 2)
    {
      return STApplication.getInstance().getString(R.string.action_level_high);
    }
    return "";
  }

  public int getPushIntervalLevel()
  {
    if (PushInterval <= 10)
    {
      return 0;
    }
    else if (PushInterval <= 30)
    {
      return 1;
    }
    else if (PushInterval <= 60)
    {
      return 2;
    }
    else if (PushInterval <= 300)
    {
      return 3;
    }
    else
    {
      return 4;
    }
  }

  public void setPushIntervalLevel(int pushIntervalLevel)
  {
    this.PushIntervalLevel = pushIntervalLevel;
    switch (pushIntervalLevel)
    {
      case 0:
        PushInterval = 10;
        break;
      case 1:
        PushInterval = 30;
        break;
      case 2:
        PushInterval = 60;
        break;
      case 3:
        PushInterval = 300;
        break;
      case 4:
        PushInterval = 600;
        break;
      default:
        break;
    }
  }


}