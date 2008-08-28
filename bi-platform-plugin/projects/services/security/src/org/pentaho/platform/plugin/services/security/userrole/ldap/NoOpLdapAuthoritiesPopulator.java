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
package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.LdapDataAccessException;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;

/**
 * For use when authorities are stored in the user object (e.g.
 * <code>objectClass=Person</code>) and therefore retrieved by an
 * <code>LdapEntryMapper</code> instance. This class helps since
 * <code>LdapAuthenticationProvider</code> requires a
 * <code>LdapAuthoritiesPopulator</code> instance.
 * 
 * @author mlowery
 * 
 */
public class NoOpLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

  public GrantedAuthority[] getGrantedAuthorities(final LdapUserDetails userDetails) throws LdapDataAccessException {
    return new GrantedAuthority[0];
  }

}
