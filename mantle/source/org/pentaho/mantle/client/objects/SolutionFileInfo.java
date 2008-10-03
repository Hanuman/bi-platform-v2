/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class SolutionFileInfo implements Serializable {
  public String solution;
  public String path;
  public String name;
  public Date lastModifiedDate;
  public long size;
  public byte[] data;
  public String type = "html";  //$NON-NLS-1$
  public boolean isDirectory = false;
  public boolean isSubscribable = false;
  public boolean supportsAccessControls = true;

  public List<UserPermission> userPermissions;
  public List<RolePermission> rolePermissions;

  public SolutionFileInfo() {
  }
  
  public String getSolution() {
    return solution;
  }

  public void setSolution(String solution) {
    this.solution = solution;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public boolean isSubscribable() {
    return isSubscribable;
  }

  public void setSubscribable(boolean isSubscribable) {
    this.isSubscribable = isSubscribable;
  }

  public boolean isSupportsAccessControls() {
    return supportsAccessControls;
  }

  public void setSupportsAccessControls(boolean supportsAccessControls) {
    this.supportsAccessControls = supportsAccessControls;
  }

  public List<UserPermission> getUserPermissions() {
    return userPermissions;
  }

  public void setUserPermissions(List<UserPermission> userPermissions) {
    this.userPermissions = userPermissions;
  }

  public List<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(List<RolePermission> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  

}
