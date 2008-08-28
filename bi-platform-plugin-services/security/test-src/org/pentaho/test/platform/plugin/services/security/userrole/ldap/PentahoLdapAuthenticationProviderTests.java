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
