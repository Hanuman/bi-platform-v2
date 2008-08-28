package org.pentaho.test.platform.plugin.services.security.userrole.ldap;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.Control;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.pentaho.platform.plugin.services.security.userrole.ldap.UnionizingLdapAuthoritiesPopulator;

/**
 * Tests the <code>UnionizingLdapAuthoritiesPopulator</code> class.
 * 
 * @author mlowery
 */
public class UnionizingLdapAuthoritiesPopulatorTests extends
		AbstractLdapServerTestCase {

	public void testGetGrantedAuthorities() throws Exception {
		DefaultLdapAuthoritiesPopulator wrappedPop;
		wrappedPop = new DefaultLdapAuthoritiesPopulator(
				getInitialCtxFactory(), "ou=roles"); //$NON-NLS-1$
		wrappedPop.setRolePrefix("ROLE_"); //$NON-NLS-1$
		wrappedPop.setGroupSearchFilter("(roleOccupant={0})"); //$NON-NLS-1$

		DefaultLdapAuthoritiesPopulator wrappedPop2;
		wrappedPop2 = new DefaultLdapAuthoritiesPopulator(
				getInitialCtxFactory(), "ou=groups"); //$NON-NLS-1$
		wrappedPop2.setRolePrefix("ROLE_"); //$NON-NLS-1$
		wrappedPop2.setGroupSearchFilter("(uniqueMember={0})"); //$NON-NLS-1$

		Set populators = new HashSet();
		populators.add(wrappedPop);
		populators.add(wrappedPop2);
		UnionizingLdapAuthoritiesPopulator unionizer = new UnionizingLdapAuthoritiesPopulator();
		unionizer.setPopulators(populators);

		unionizer.afterPropertiesSet();
		
		GrantedAuthority[] auths = unionizer
				.getGrantedAuthorities(buildLdapUserDetails());

		assertTrue(null != auths && auths.length > 0);

		List authsList = Arrays.asList(auths);
		assertTrue(authsList.contains(new GrantedAuthorityImpl("ROLE_CTO"))); //$NON-NLS-1$
		assertTrue(authsList
				.contains(new GrantedAuthorityImpl("ROLE_MARKETING"))); //$NON-NLS-1$

		System.out.println(authsList);
	}

	protected LdapUserDetails buildLdapUserDetails() {
		return new LdapUserDetails() {

			private static final long serialVersionUID = 1L;

			public Attributes getAttributes() {
				return new BasicAttributes();
			}

			public Control[] getControls() {
				return new Control[0];
			}

			public String getDn() {
				return "uid=suzy,ou=users," + ROOT_DN; //$NON-NLS-1$
			}

			public GrantedAuthority[] getAuthorities() {
				return new GrantedAuthority[0];
			}

			public String getPassword() {
				return "password"; //$NON-NLS-1$
			}

			public String getUsername() {
				return "suzy"; //$NON-NLS-1$
			}

			public boolean isAccountNonExpired() {
				return true;
			}

			public boolean isAccountNonLocked() {
				return true;
			}

			public boolean isCredentialsNonExpired() {
				return true;
			}

			public boolean isEnabled() {
				return true;
			}

		};
	}

}
