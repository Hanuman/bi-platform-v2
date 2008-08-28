package org.pentaho.test.platform.plugin.services.security.userrole.ldap;


import java.util.Hashtable;

import junit.framework.TestCase;

import org.acegisecurity.ldap.DefaultInitialDirContextFactory;
import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.ldap.LdapUserSearch;
import org.acegisecurity.ldap.search.FilterBasedLdapUserSearch;
import org.apache.directory.server.core.jndi.CoreContextFactory;

/**
 * Abstract LDAP testcase. (Based on
 * <code>org.acegisecurity.providers.ldap.AbstractLdapServerTestCase</code>
 * Located in Acegi Security tests).
 * 
 * <p>
 * This test can run against (1) an external LDAP server or (2) an embedded LDAP
 * server. Simply uncomment the constants below that correspond to the option
 * that you want (and comment out the other constants).
 * </p>
 * 
 * @author mlowery
 */
public abstract class AbstractLdapServerTestCase extends TestCase {
	// ~ Static fields/initializers
	// =========================================================================

	// Option 1 of 2: External server config
	// private static final String ROOT_DN = "ou=system"; //$NON-NLS-1$
	//
	// private static final String MANAGER_USER = "uid=admin," + ROOT_DN;
	// //$NON-NLS-1$
	//
	// private static final String MANAGER_PASSWORD = "secret"; //$NON-NLS-1$
	//
	// private static final String PROVIDER_URL =
	// "ldap://localhost:10389/" + ROOT_DN; //$NON-NLS-1$
	// private static final String CONTEXT_FACTORY =
	// "com.sun.jndi.ldap.LdapCtxFactory"; //$NON-NLS-1$
	// private static final Hashtable EXTRA_ENV = new Hashtable();

	// Option 2 of 2: Embedded (non-networked) server config
	protected static final String ROOT_DN = "dc=acegisecurity,dc=org"; //$NON-NLS-1$

	private static final String MANAGER_USER = "cn=manager," + ROOT_DN; //$NON-NLS-1$

	private static final String MANAGER_PASSWORD = "acegisecurity"; //$NON-NLS-1$

	private static final PentahoLdapTestServer SERVER = new PentahoLdapTestServer(
			ROOT_DN);

	private static final String PROVIDER_URL = ROOT_DN;

	private static final String CONTEXT_FACTORY = CoreContextFactory.class
			.getName();

	private static final Hashtable EXTRA_ENV = SERVER.getConfiguration()
			.toJndiEnvironment();

	// ~ Instance fields
	// =========================================================================

	private DefaultInitialDirContextFactory idf;

	// ~ Constructors
	// =========================================================================

	protected AbstractLdapServerTestCase() {
		super();
	}

	protected AbstractLdapServerTestCase(final String string) {
		super(string);
	}

	// ~ Methods
	// =========================================================================

	protected DefaultInitialDirContextFactory getInitialCtxFactory() {
		return idf;
	}

	/**
	 * For subclasses.
	 */
	protected void onSetUp() {
	}

	public final void setUp() {
		idf = new DefaultInitialDirContextFactory(PROVIDER_URL);
		idf.setInitialContextFactory(CONTEXT_FACTORY);
		idf.setExtraEnvVars(EXTRA_ENV);
		idf.setManagerDn(MANAGER_USER);
		idf.setManagerPassword(MANAGER_PASSWORD);
		onSetUp();
	}

	/**
	 * Returns an <code>LdapUserSearch</code> instance based on whether the
	 * LDAP server is embedded or external. This is a workaround for a bug in
	 * ApacheDS.
	 */
	protected LdapUserSearch getUserSearch(final String searchBase,
			final String searchFilter,
			final InitialDirContextFactory initialDirContextFactory) {
		LdapUserSearch userSearch = null;
		// if we're running the embedded apache, return the workaround class
		if (CONTEXT_FACTORY.equals(CoreContextFactory.class.getName())) {
			userSearch = new ApacheDirectoryServerUserSearch(searchBase,
					searchFilter, initialDirContextFactory);
		} else {
			userSearch = new FilterBasedLdapUserSearch(searchBase,
					searchFilter, initialDirContextFactory);
		}
		return userSearch;

	}

}