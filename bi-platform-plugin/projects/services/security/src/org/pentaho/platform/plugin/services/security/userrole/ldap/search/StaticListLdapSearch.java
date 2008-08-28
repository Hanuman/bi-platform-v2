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
package org.pentaho.platform.plugin.services.security.userrole.ldap.search;

import java.util.Collections;
import java.util.List;

/**
 * Returns the list configured via the <code>staticList</code> property.  (Makes no connection to a directory.)
 * One way to use this class would be to use it along with <code>UnionizingLdapSearch</code> to return a list of 
 * additional roles or usernames.  This is useful when you want to use the Admin Permissions interface to assign
 * ACLs but the role or username that you wish to use is not actually present in the directory (e.g. 
 * <code>Anonymous</code>).
 * 
 * @author mlowery
 */
public class StaticListLdapSearch implements LdapSearch {

  private List staticList = Collections.EMPTY_LIST;

  public List search(final Object[] ignored) {
    return staticList;
  }

  public void setStaticList(final List staticList) {
    if (null == staticList) {
      this.staticList = Collections.EMPTY_LIST;
    } else {
      this.staticList = staticList;
    }
  }

  public List getStaticList() {
    return staticList;
  }

}
