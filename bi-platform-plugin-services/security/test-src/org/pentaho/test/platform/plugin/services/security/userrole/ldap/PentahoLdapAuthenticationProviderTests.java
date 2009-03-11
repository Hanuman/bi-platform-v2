/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin.services.security.userrole.ldap;


import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.ldap.LdapUserSearch;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.ldap.LdapAuthenticationProvider;
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator;
import org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator;
import org.pentaho.platform.plugin.services.security.userrole.ldap.RolePreprocessingMapper;

/**
 * Tests for <code>RolePreprocessingMapper</code>. Also serves as an example
 * of how to configure an <code>LdapAuthenticationProvider</code>.
 * 
 * @author mlowery
 */
public class PentahoLdapAuthenticationProviderTests extends
		AbstractLdapServerTestCase {

	public void testAuthenticate() throws Exception {
		BindAuthenticator authenticator = new BindAuthenticator(
				getInitialCtxFactory());
		LdapUserSearch userSearch = getUserSearch(
				"ou=users", "(cn={0})", getInitialCtxFactory()); //$NON-NLS-1$ //$NON-NLS-2$

		authenticator.setUserSearch(userSearch);

		RolePreprocessingMapper mapper = new RolePreprocessingMapper();
		mapper.setTokenName("cn"); //$NON-NLS-1$
		mapper.setRoleAttributes(new String[] { "uniqueMember" }); //$NON-NLS-1$

		authenticator.setUserDetailsMapper(mapper);

		DefaultLdapAuthoritiesPopulator populator;
		populator = new DefaultLdapAuthoritiesPopulator(getInitialCtxFactory(),
				"ou=roles"); //$NON-NLS-1$
		populator.setRolePrefix("ROLE_"); //$NON-NLS-1$

		LdapAuthenticationProvider ldapProvider = new LdapAuthenticationProvider(
				authenticator, populator);
		Authentication auth = ldapProvider
				.authenticate(new UsernamePasswordAuthenticationToken(
						"tiffany", //$NON-NLS-1$
						"password")); //$NON-NLS-1$
		System.out.println(auth);
		assertEquals(2, auth.getAuthorities().length);
		GrantedAuthority[] actual = auth.getAuthorities();
		GrantedAuthority expected = new GrantedAuthorityImpl("ROLE_DEV"); //$NON-NLS-1$
		assertTrue(actual[0].equals(expected) || actual[1].equals(expected));
	}

}
