package org.pentaho.mantle.client.usersettings;

import java.util.HashMap;
import java.util.List;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

public interface IMantleSettingsListener {
  public void onFetchMantleSettings(HashMap<String,String> settings);
}
