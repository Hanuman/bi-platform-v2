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
package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator;

/**
 * Slightly modified version of <code>DefaultLdapAuthoritiesPopulator</code>
 * that correctly consults the <code>searchSubtree</code> property before
 * executing the query.
 *
 * @deprecated Use org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator instead. See PPP-318.
 * @author mlowery
 */
@Deprecated
public class FixedDefaultLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

  public FixedDefaultLdapAuthoritiesPopulator(final InitialDirContextFactory initialDirContextFactory,
      final String groupSearchBase) {
    super(initialDirContextFactory, groupSearchBase);
  }

}