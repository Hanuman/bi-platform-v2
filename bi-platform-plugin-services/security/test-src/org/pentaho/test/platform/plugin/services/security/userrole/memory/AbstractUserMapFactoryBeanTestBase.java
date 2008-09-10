package org.pentaho.test.platform.plugin.services.security.userrole.memory;


import junit.framework.TestCase;


/**
 * Superclass of UserMap factory bean tests.
 * 
 * @author mlowery
 */
public class AbstractUserMapFactoryBeanTestBase extends TestCase {
	protected String userMapText;

	protected void setUp() throws Exception {
		super.setUp();
		StringBuffer buf = new StringBuffer();
		buf
				.append("joe=password,ROLE_ADMIN,ROLE_CEO,ROLE_AUTHENTICATED\n") //$NON-NLS-1$
				.append("suzy=password,ROLE_CTO,ROLE_IS,ROLE_AUTHENTICATED\n") //$NON-NLS-1$
				.append("pat=password,ROLE_DEV,ROLE_AUTHENTICATED\n") //$NON-NLS-1$
				.append(
						"tiffany=password,ROLE_AUTHENTICATED\n") //$NON-NLS-1$
				.append("admin=secret,ROLE_ADMIN,ROLE_AUTHENTICATED\n"); //$NON-NLS-1$
		userMapText = buf.toString();
	}
}
