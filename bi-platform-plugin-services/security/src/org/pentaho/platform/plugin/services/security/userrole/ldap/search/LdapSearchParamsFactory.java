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

/**
 * A factory for creating <code>LdapSearchParams</code> instances.
 * 
 * @author mlowery
 * 
 */
public interface LdapSearchParamsFactory {

  /**
   * Create a parameters object with the given arguments. The assumption is
   * that the filter arguments will be the only parameter not known until
   * runtime.
   * 
   * @param filterArgs
   *            arguments that will be merged with the <code>filter</code>
   *            property of an <code>LdapSearchParams</code> instance
   * @return a new parameters object
   */
  LdapSearchParams createParams(Object[] filterArgs);

}
