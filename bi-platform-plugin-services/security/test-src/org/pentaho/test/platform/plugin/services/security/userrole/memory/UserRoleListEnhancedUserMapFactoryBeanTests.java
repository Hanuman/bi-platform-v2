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
package org.pentaho.test.platform.plugin.services.security.userrole.memory;


import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMap;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMapFactoryBean;
import org.springframework.util.Assert;

public class UserRoleListEnhancedUserMapFactoryBeanTests extends
		AbstractUserMapFactoryBeanTestBase {

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
