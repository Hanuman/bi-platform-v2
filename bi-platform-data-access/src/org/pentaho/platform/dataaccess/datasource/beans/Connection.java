/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class Connection extends XulEventSourceAdapter implements IConnection{

  private String name;
  private String driverClass;
  private String username;
  private String password;
  private String url;

  public Connection(){
    
  }

  public Connection(IConnection connection){
    setName(connection.getName());
    setDriverClass(connection.getDriverClass());
    setPassword(connection.getPassword());
    setUrl(connection.getUrl());
    setUsername(connection.getUsername());
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public String getDriverClass() {
    return driverClass;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

}
