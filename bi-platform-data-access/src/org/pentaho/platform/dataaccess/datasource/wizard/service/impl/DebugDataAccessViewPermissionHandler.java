package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;

public class DebugDataAccessViewPermissionHandler implements IDataAccessViewPermissionHandler {

  public List<String> getPermittedRoleList(IPentahoSession session) {
    List<String> roleList = new ArrayList<String>();
    roleList.add("Admin");
    return roleList;
  }

  public List<String> getPermittedUserList(IPentahoSession session) {
    List<String> userList = new ArrayList<String>();
    userList.add("joe");
    return userList;
  }

  public int getDefaultAcls(IPentahoSession session) {
    return 31;
  }
}
