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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Iterates over <code>LdapSearch</code> instances in <code>searches</code>
 * and unions the results.
 * 
 * @author mlowery
 */
public class UnionizingLdapSearch implements LdapSearch, InitializingBean {
  // ~ Static fields/initializers ============================================

  // private static final Log logger = LogFactory.getLog(UnionizingLdapSearch.class);

  // ~ Instance fields =======================================================

  private Set searches;

  // ~ Constructors ==========================================================

  public UnionizingLdapSearch() {
    super();
  }

  public UnionizingLdapSearch(final Set searches) {
    this.searches = searches;
  }

  // ~ Methods ===============================================================

  public List search(final Object[] filterArgs) {
    Set results = new HashSet();
    Iterator iter = searches.iterator();
    while (iter.hasNext()) {
      results.addAll(((LdapSearch) iter.next()).search(filterArgs));
    }
    return new ArrayList(results);
  }

  public void setSearches(final Set searches) {
    this.searches = searches;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notEmpty(searches);
  }

}
