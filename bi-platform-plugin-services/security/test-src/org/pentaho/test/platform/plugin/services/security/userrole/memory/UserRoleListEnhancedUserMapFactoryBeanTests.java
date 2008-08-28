package org.pentaho.test.platform.plugin.services.security.userrole.memory;


import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMap;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMapFactoryBean;
import org.springframework.util.Assert;

public class UserRoleListEnhancedUserMapFactoryBeanTests extends
		AbstractUserMapFactoryBeanTest {

	public void testGetObject() throws Exception {
		UserRoleListEnhancedUserMapFactoryBean bean = new UserRoleListEnhancedUserMapFactoryBean();
		bean.setUserMap(userMapText);
		UserRoleListEnhancedUserMap map = (UserRoleListEnhancedUserMap) bean
				.getObject();
		assertNotNull(map.getUser("joe")); //$NON-NLS-1$
    // Next assert is unnecessary by interface contract
		// assertTrue(map.getUser("joe") instanceof UserDetails); //$NON-NLS-1$
		assertTrue(isRolePresent(map.getAllAuthorities(), "ROLE_CEO")); //$NON-NLS-1$
		assertTrue(isUserPresent(
				map.getUserNamesInRole(new GrantedAuthorityImpl("ROLE_CEO")), "joe")); //$NON-NLS-1$//$NON-NLS-2$
		// System.out.println(StringUtils.arrayToCommaDelimitedString(map.getAllUsers()));
		assertTrue(isUserPresent(map.getAllUsers(), "suzy")); //$NON-NLS-1$
		// System.out.println(map.getUser("joe"));
	}

	protected boolean isRolePresent(final GrantedAuthority[] roles,
			final String role) {
		Assert.hasLength(role);
		for (int i = 0; i < roles.length; i++) {
			if (null != roles[i] && roles[i].getAuthority().equals(role)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isUserPresent(final String[] users, final String user) {
		Assert.hasLength(user);
		for (int i = 0; i < users.length; i++) {
			if (user.equals(users[i])) {
				return true;
			}
		}
		return false;
	}

}
