/*
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
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

import java.util.List;

/**
 * Executes a search against a directory context using the given filter
 * arguments plus other static search parameters (in the form of instance
 * variables) known at deploy time.
 * 
 * <p>
 * Can also be seen as a generalization of
 * <code>org.acegisecurity.ldap.LdapUserSearch</code>.
 * </p>
 * 
 * @author mlowery
 * @see javax.naming.directory.DirContext.search()
 */
public interface LdapSearch {

  /**
   * Executes a search against a directory context using the given filter
   * arguments.
   * 
   * @param filterArgs
   *            the filter arguments
   * @return the result set as a list
   */
  List search(Object[] filterArgs);
}
