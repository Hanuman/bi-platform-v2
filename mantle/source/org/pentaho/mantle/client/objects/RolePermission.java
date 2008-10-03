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

public class RolePermission implements Serializable {
  public String name;
  public int mask;
  
  public RolePermission() {
  }
  
  public RolePermission(String name, int mask) {
    this.name = name;
    this.mask = mask;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMask() {
    return mask;
  }

  public void setMask(int mask) {
    this.mask = mask;
  }
  
  public String toString() {
    return "RolePermission[name=" + name + ", mask=" + mask + "]";
  }
}
