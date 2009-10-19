package org.pentaho.mantle.client.usersettings;

import java.util.List;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

public interface IUserSettingsListener {
  public void onFetchUserSettings(List<IUserSetting> settings);
}
