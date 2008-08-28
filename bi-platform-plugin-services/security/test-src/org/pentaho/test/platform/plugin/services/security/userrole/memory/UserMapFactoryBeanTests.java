package org.pentaho.test.platform.plugin.services.security.userrole.memory;


import org.acegisecurity.userdetails.memory.UserMap;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserMapFactoryBean;

public class UserMapFactoryBeanTests extends AbstractUserMapFactoryBeanTest {

	public void testGetObject() throws Exception {
		UserMapFactoryBean bean = new UserMapFactoryBean();
		bean.setUserMap(userMapText);
		UserMap map = (UserMap) bean.getObject();
		assertNotNull(map.getUser("joe")); //$NON-NLS-1$
		assertNotNull(map.getUser("tiffany")); //$NON-NLS-1$
    // Next assert is unnecessary as by contract, the getUser returns a UserDetails
		// assertTrue(map.getUser("joe") instanceof UserDetails); //$NON-NLS-1$
		// System.out.println(map.getUser("joe"));
	}
}
