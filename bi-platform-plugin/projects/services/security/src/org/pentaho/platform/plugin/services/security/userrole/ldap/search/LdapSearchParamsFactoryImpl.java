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

import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class LdapSearchParamsFactoryImpl implements LdapSearchParamsFactory, InitializingBean {

  // ~ Static fields/initializers ============================================

  // private static final Log logger = LogFactory.getLog(LdapSearchParamsFactoryImpl.class);

  // ~ Instance fields =======================================================

  private String base;

  private String filter;

  private SearchControls searchControls;

  // ~ Constructors ==========================================================

  public LdapSearchParamsFactoryImpl() {
    super();
  }

  // ~ Methods ===============================================================

  public LdapSearchParamsFactoryImpl(final String base, final String filter) {
    this(base, filter, new SearchControls());
  }

  public LdapSearchParamsFactoryImpl(final String base, final String filter, final SearchControls searchControls) {
    this.base = base;
    this.filter = filter;
    this.searchControls = searchControls;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(base);
    Assert.hasLength(filter);
  }

  /**
   * Private so it cannot be instantiated except by this class.
   */
  private class LdapSearchParamsImpl implements LdapSearchParams {
    private String implBase;

    private String implFilter;

    private Object[] filterArgs;

    private SearchControls implSearchControls;

    private LdapSearchParamsImpl(final String base, final String filter, final Object[] filterArgs,
        final SearchControls searchControls) {
      this.implBase = base;
      this.implFilter = filter;
      this.filterArgs = filterArgs;
      this.implSearchControls = searchControls;
    }

    public String getBase() {
      return implBase;
    }

    public String getFilter() {
      return implFilter;
    }

    public Object[] getFilterArgs() {
      return filterArgs;
    }

    public SearchControls getSearchControls() {
      return implSearchControls;
    }

  }

  public LdapSearchParams createParams(final Object[] filterArgs) {
    return new LdapSearchParamsImpl(base, filter, filterArgs, searchControls);
  }

}
