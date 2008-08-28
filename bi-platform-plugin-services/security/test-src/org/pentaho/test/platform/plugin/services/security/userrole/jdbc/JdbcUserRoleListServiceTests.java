package org.pentaho.test.platform.plugin.services.security.userrole.jdbc;


import java.util.HashSet;

import junit.framework.TestCase;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.PopulatedDatabase;
import org.acegisecurity.userdetails.jdbc.JdbcDaoImpl;
import org.pentaho.platform.plugin.services.security.userrole.jdbc.JdbcUserRoleListService;

public class JdbcUserRoleListServiceTests extends TestCase {

	public void testGetAllUsernames() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllUsernamesQuery("SELECT DISTINCT(USERNAME) FROM USERS ORDER BY USERNAME"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		String[] allUsers = dao.getAllUsernames();
		assertTrue("User List should not be empty", allUsers.length > 0); //$NON-NLS-1$
		for (int i = 0; i < allUsers.length; i++) {
			System.out.println("User: " + allUsers[i]); //$NON-NLS-1$
		}
	}

	public void testGetAllAuthorities() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllAuthoritiesQuery("SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		GrantedAuthority[] auths = dao.getAllAuthorities();
		assertTrue("Authorities list should not be empty", auths.length > 0); //$NON-NLS-1$
		for (int i = 0; i < auths.length; i++) {
			System.out.println("Authority: " + auths[i].getAuthority()); //$NON-NLS-1$
		}
	}

	public void testGetAllAuthoritiesWithRolePrefix() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllAuthoritiesQuery("SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1"); //$NON-NLS-1$
		dao.setRolePrefix("ARBITRARY_PREFIX_"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		GrantedAuthority[] auths = dao.getAllAuthorities();
		assertTrue("Authorities list should not be empty", auths.length > 0); //$NON-NLS-1$
		for (int i = 0; i < auths.length; i++) {
			System.out
					.println("Authority with prefix: " + auths[i].getAuthority()); //$NON-NLS-1$
		}
	}

	public void testGetAllUsernamesInRole() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao
				.setAllUsernamesInRoleQuery("SELECT DISTINCT(USERNAME) AS USERNAME FROM AUTHORITIES WHERE AUTHORITY = ? ORDER BY 1"); //$NON-NLS-1$
		dao.afterPropertiesSet();
		String[] allUsers = dao.getUsernamesInRole(new GrantedAuthorityImpl(
				"ROLE_TELLER")); //$NON-NLS-1$
		assertTrue("User List should not be empty", allUsers.length > 0); //$NON-NLS-1$
		for (int i = 0; i < allUsers.length; i++) {
			System.out.println("ROLE_TELLER User: " + allUsers[i]); //$NON-NLS-1$
		}
	}

	public void testGetRolesForUser() throws Exception {
		JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
		dao.setUserDetailsService(makePopulatedJdbcDao()); 
		dao.afterPropertiesSet();
		GrantedAuthority[] roles = dao.getAuthoritiesForUser("marissa"); //$NON-NLS-1$
		HashSet authorities = new HashSet(2);
		authorities.add(roles[0].getAuthority());
		authorities.add(roles[1].getAuthority());
		assertTrue(authorities.contains("ROLE_TELLER")); //$NON-NLS-1$
		assertTrue(authorities.contains("ROLE_SUPERVISOR")); //$NON-NLS-1$

	}

	protected JdbcUserRoleListService makePopulatedJdbcUserRoleListService()
			throws Exception {
		JdbcUserRoleListService dao = new JdbcUserRoleListService(makePopulatedJdbcDao());
		dao.setDataSource(PopulatedDatabase.getDataSource());
		return dao;
	}

	private JdbcDaoImpl makePopulatedJdbcDao() throws Exception {
		JdbcDaoImpl dao = new JdbcDaoImpl();
		dao.setDataSource(PopulatedDatabase.getDataSource());
		dao.afterPropertiesSet();
		return dao;
	}

}
