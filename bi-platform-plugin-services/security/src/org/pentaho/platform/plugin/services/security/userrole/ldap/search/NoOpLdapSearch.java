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
 * Immediately returns an empty list without connecting to a server.
 *
 * <p>This is useful when you do not wish to implement one of the searches of
 * <code>DefaultLdapUserRoleLIstService</code>--most often the all usernames search.</p>
 * @author mlowery
 */
public class NoOpLdapSearch implements LdapSearch {

  public List search(final Object[] filterArgs) {
    return Collections.EMPTY_LIST;
  }

}
