/*
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.services.security.userrole.ldap.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Adds the roles from <code>extraRoles</code> to the roles input into this transformer. The roles are added as strings
 * so a subsequent transformer must convert them to <code>GrantedAuthority</code> instances.
 *
 * <p>
 * Transformer input: <code>String</code> instance, <code>Collection</code>
 * of <code>String</code> instances, or array of <code>String</code>
 * instances.
 * </p>
 * <p>
 * Transformer output:
 * <code>Collection</code> of <code>String</code> instances, or
 * array of <code>String</code> instances.
 * </p>
 *
 * @author mlowery
 */
public class ExtraRoles implements Transformer, InitializingBean {

  // ~ Instance fields =======================================================

  private Set extraRoles;

  // ~ Methods ===============================================================

  public Object transform(final Object obj) {
    Object transformed;
    Set authSet = new HashSet();
    if (obj instanceof String) {
      authSet.add(obj);
    } else if (obj instanceof Collection) {
      authSet.addAll((Collection) obj);
    } else if (obj instanceof Object[]) {
      authSet.addAll(Arrays.asList((Object[]) obj));
    }
    authSet.addAll(extraRoles);
    if (obj instanceof Object[]) {
      transformed = authSet.toArray();
    } else {
      transformed = authSet;
    }
    return transformed;
  }

  public Set getExtraRoles() {
    return extraRoles;
  }

  public void setExtraRoles(final Set extraRoles) {
    this.extraRoles = extraRoles;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(extraRoles);
  }
}
