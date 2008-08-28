package org.pentaho.platform.repository.usersettings;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;

public class UserSettingDAO {
  // only the org.pentaho.platform.repository.usersettings package can execute these methods!
  // we do not want to generically expose these methods
  static List<IUserSetting> getUserSettings(Session session, String username) {
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.usersettings.pojo.UserSetting.getUserSettings").setParameter("username", username)
        .setCacheable(true);
    return qry.list();
  }

  static IUserSetting getUserSetting(Session session, String username, String settingName) {
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.usersettings.pojo.UserSetting.getUserSetting").setParameter("username", username)
        .setParameter("settingName", settingName).setCacheable(true);
    List list = qry.list();
    if (list != null && list.size() > 0) {
      return (IUserSetting)list.get(0);
    }
    return null;
  }

  static void setUserSetting(Session session, String username, String settingName, String settingValue) {
    // make sure any old values are removed
    removeUserSetting(session, username, settingName);
    IUserSetting setting = new UserSetting();
    setting.setUsername(username);
    setting.setSettingName(settingName);
    setting.setSettingValue(settingValue);
    session.saveOrUpdate(setting);
  }

  // remove all settings for a given settingName
  static void removeUserSetting(Session session, String username, String settingName) {
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.usersettings.pojo.UserSetting.getUserSetting").setParameter("username", username)
        .setParameter("settingName", settingName).setCacheable(true);
    List settings = qry.list();
    for (int i = 0; i < settings.size(); i++) {
      IUserSetting setting = (IUserSetting) settings.get(i);
      session.delete(setting);
    }
  }

}
