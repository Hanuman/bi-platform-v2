package org.pentaho.platform.repository.usersettings;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;

public class UserSettingService implements IUserSettingService {

  public static final String GLOBAL_SETTING = "_GLOBAL";
  IPentahoSession            session        = null;

  public UserSettingService() {
  }

  public void init(IPentahoSession session) {
    this.session = session;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // GENERIC/ADMIN METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  // delete all settings for a given user
  public void deleteUserSettings() {
    Session hibsession = HibernateUtil.getSession();
    Transaction tx = hibsession.beginTransaction();
    List<IUserSetting> settings = UserSettingDAO.getUserSettings(hibsession, session.getName());
    if (settings != null) {
      for (IUserSetting setting : settings) {
        hibsession.delete(setting);
      }
    }
    tx.commit();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // USER SETTINGS METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  public List<IUserSetting> getUserSettings() {
    // get the global settings and the user settings
    // merge unseen global settings into the user settings list
    Session hibsession = HibernateUtil.getSession();
    List<IUserSetting> userSettings = UserSettingDAO.getUserSettings(hibsession, session.getName());
    List<IUserSetting> globalSettings = UserSettingDAO.getUserSettings(hibsession, GLOBAL_SETTING);
    // merge the two lists (add unseen global settings)
    for (IUserSetting globalSetting : globalSettings) {
      if (!userSettings.contains(globalSetting)) {
        userSettings.add(globalSetting);
      }
    }
    return userSettings;
  }

  public IUserSetting getUserSetting(String settingName, String defaultValue) {
    // if the user does not have the setting, check if a global setting exists
    Session hibsession = HibernateUtil.getSession();
    IUserSetting userSetting = UserSettingDAO.getUserSetting(hibsession, session.getName(), settingName);
    if (userSetting == null) {
      userSetting = getGlobalUserSetting(settingName, defaultValue);
    }
    return userSetting;
  }

  public void setUserSetting(String settingName, String settingValue) {
    Session hibsession = HibernateUtil.getSession();
    Transaction tx = hibsession.beginTransaction();
    UserSettingDAO.setUserSetting(hibsession, session.getName(), settingName, settingValue);
    tx.commit();
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // GLOBAL USER SETTINGS METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  public IUserSetting getGlobalUserSetting(String settingName, String defaultValue) {
    Session hibsession = HibernateUtil.getSession();
    IUserSetting userSetting = UserSettingDAO.getUserSetting(hibsession, GLOBAL_SETTING, settingName);
    if (userSetting == null && defaultValue != null) {
      // pass out default value
      userSetting = new UserSetting();
      userSetting.setUsername(session.getName());
      userSetting.setSettingName(settingName);
      userSetting.setSettingValue(defaultValue);
    }
    return userSetting;
  }

  public List<IUserSetting> getGlobalUserSettings() {
    Session hibsession = HibernateUtil.getSession();
    return UserSettingDAO.getUserSettings(hibsession, GLOBAL_SETTING);
  }

  public void setGlobalUserSetting(String settingName, String settingValue) {
    if (SecurityHelper.isPentahoAdministrator(session)) {
      Session hibsession = HibernateUtil.getSession();
      Transaction tx = hibsession.beginTransaction();
      UserSettingDAO.setUserSetting(hibsession, GLOBAL_SETTING, settingName, settingValue);
      tx.commit();
    } else {
      throw new UnsupportedOperationException("Insufficient privileges to set a global user setting.");
    }
  }

}
