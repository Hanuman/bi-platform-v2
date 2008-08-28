package org.pentaho.test.platform.plugin.services.security.userrole.ldap;


import java.io.File;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.configuration.StartupConfiguration;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * An embedded LDAP server. (Based on
 * <code>org.acegisecurity.ldap.LdapTestServer</code> located in Acegi
 * Security tests).
 * 
 * @author mlowery
 */
public class PentahoLdapTestServer {
	private DirContext serverContext;

	private StartupConfiguration cfg;

	private File workingDir = new File(System.getProperty("java.io.tmpdir") //$NON-NLS-1$
			+ File.separator + "embedded-apacheds-work"); //$NON-NLS-1$

	private String rootDn;

	public PentahoLdapTestServer(final String rootDn) {
		this.rootDn = rootDn;
		boolean started = startLdapServer();
		// quit now if problem starting
		if (started) {
			createManagerUser();
			initTestData();
		}
	}

	/**
	 * Starts an embedded LDAP server.
	 * <ol>
	 * <li>Read config and env from XML.</li>
	 * <li>Remove working directory.</li>
	 * <li>Get initial directory context to do initTestData with.</li>
	 * </ol>
	 */
	public boolean startLdapServer() {
		Properties env;
		ApplicationContext factory = new ClassPathXmlApplicationContext(
				"org/pentaho/test/platform/plugin/services/security/userrole/ldap/apacheds-context.xml"); //$NON-NLS-1$
		cfg = (StartupConfiguration) factory.getBean("configuration"); //$NON-NLS-1$
		((MutableServerStartupConfiguration) cfg)
				.setWorkingDirectory(workingDir);
		deleteDir(workingDir);

		System.out.println("Working directory is " //$NON-NLS-1$
				+ workingDir.getAbsolutePath());
		env = (Properties) factory.getBean("environment"); //$NON-NLS-1$

		// connecting with out of box manager (PartitionNexus.ADMIN_PRINCIPAL)
		env.setProperty(Context.PROVIDER_URL, rootDn); 
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				CoreContextFactory.class.getName());
		env.putAll(cfg.toJndiEnvironment());

		try {
			serverContext = new InitialDirContext(env);
		} catch (NamingException e) {
			System.err.println("Failed to start Apache DS"); //$NON-NLS-1$
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Loads test users.
	 */
	protected void initTestData() {
		createOu("users"); //$NON-NLS-1$
		createOu("groups"); //$NON-NLS-1$
		createOu("configuration"); //$NON-NLS-1$
		createOu("partitions"); //$NON-NLS-1$
		createOu("services"); //$NON-NLS-1$
		createOu("interceptors"); //$NON-NLS-1$
		createOu("roles"); //$NON-NLS-1$

		createUser(
				"tiffany", "tiffany", "Pentaho", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"password", //$NON-NLS-1$
				new String[] {
						"cn=devmgr,ou=roles" + "," + rootDn, "cn=dev,ou=roles" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		createUser("pat", "pat", "Pentaho", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"password", //$NON-NLS-1$
				new String[] { "cn=dev,ou=roles" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$
		createUser(
				"joe", "joe", "Pentaho", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"password", new String[] { //$NON-NLS-1$
						"cn=ceo,ou=roles" + "," + rootDn, "cn=admin,ou=roles" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		createUser(
				"suzy", "suzy", "Pentaho", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"password", //$NON-NLS-1$
				new String[] {
						"cn=cto,ou=roles" + "," + rootDn, "cn=is,ou=roles" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		createGroupAndRole("Development", "groups", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=pat,ou=users" + "," + rootDn, //$NON-NLS-1$ //$NON-NLS-2$
						"uid=tiffany,ou=users" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("Administrators", "groups", new String[] { //$NON-NLS-1$ //$NON-NLS-2$
				"uid=joe,ou=users" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("Sales", "groups", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=joe,ou=users" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("Marketing", "groups", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=suzy,ou=users" + "," + rootDn }); //$NON-NLS-1$ //$NON-NLS-2$

		createGroupAndRole("devmgr", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=tiffany,ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("cto", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=suzy,ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("ceo", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=joe, ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("is", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=suzy,ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole(
				"dev", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] {
						"uid=pat,ou=users" + "," + rootDn, "uid=tiffany,ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole("admin", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] { "uid=joe,ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$
		createGroupAndRole(
				"authenticated", "roles", //$NON-NLS-1$ //$NON-NLS-2$
				new String[] {
						"uid=joe,ou=users" + "," + rootDn, "uid=tiffany,ou=users" + "," + rootDn, "uid=suzy,ou=users" + "," + rootDn, "uid=pat,ou=users" + "," + rootDn }, true, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"Orlando", "Florida"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Recursively deletes a directory.
	 * 
	 * @param dir
	 *            the directory to delete
	 * @return true if successful
	 */
	protected static boolean deleteDir(File dir) {
		if (!dir.exists()) {
			return true;
		}
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	private void createManagerUser() {
		Attributes user = new BasicAttributes("cn", "manager", true); //$NON-NLS-1$ //$NON-NLS-2$
		user.put("userPassword", "acegisecurity"); //$NON-NLS-1$ //$NON-NLS-2$

		Attribute objectClass = new BasicAttribute("objectClass"); //$NON-NLS-1$
		user.put(objectClass);
		objectClass.add("top"); //$NON-NLS-1$
		objectClass.add("person"); //$NON-NLS-1$
		objectClass.add("organizationalPerson"); //$NON-NLS-1$
		objectClass.add("inetOrgPerson"); //$NON-NLS-1$
		user.put("sn", "Manager"); //$NON-NLS-1$ //$NON-NLS-2$
		user.put("cn", "manager"); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			serverContext.createSubcontext("cn=manager", user); //$NON-NLS-1$
		} catch (NamingException ne) {
			System.err.println("Failed to create manager user."); //$NON-NLS-1$
			ne.printStackTrace();
		}
	}

	protected void createOu(String name) {
		Attributes ou = new BasicAttributes("ou", name); //$NON-NLS-1$
		Attribute objectClass = new BasicAttribute("objectClass"); //$NON-NLS-1$
		objectClass.add("top"); //$NON-NLS-1$
		objectClass.add("organizationalUnit"); //$NON-NLS-1$
		ou.put(objectClass);

		try {
			serverContext.createSubcontext("ou=" + name, ou); //$NON-NLS-1$
		} catch (NameAlreadyBoundException ignore) {
			System.out.println(" ou " + name + " already exists."); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NamingException ne) {
			System.err.println("Failed to create ou."); //$NON-NLS-1$
			ne.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void createUser(String uid, String cn, String sn,
			String password, String[] theMembers) {
		Attributes user = new BasicAttributes("uid", uid); //$NON-NLS-1$
		user.put("cn", cn); //$NON-NLS-1$
		user.put("userPassword", password); //$NON-NLS-1$
		user.put("sn", sn); // added //$NON-NLS-1$
		user.put("mail", new StringBuffer(uid).append(".pentaho@pentaho.org") //$NON-NLS-1$ //$NON-NLS-2$
				.toString()); // added

		Attribute objectClass = new BasicAttribute("objectClass"); //$NON-NLS-1$
		user.put(objectClass);

		if (theMembers.length > 0) {
			Attribute members = new BasicAttribute("uniqueMember"); //$NON-NLS-1$
			Attribute categories = new BasicAttribute("businessCategory"); //$NON-NLS-1$
			user.put(members);
			user.put(categories);
			
			for (int i = 0; i < theMembers.length; i++) {
				members.add(theMembers[i]);
				categories.add(theMembers[i]);
			}
			objectClass.add("groupOfUniqueNames"); //$NON-NLS-1$
		}

		objectClass.add("top"); //$NON-NLS-1$
		objectClass.add("organizationalPerson"); //$NON-NLS-1$
		objectClass.add("inetOrgPerson"); //$NON-NLS-1$
		objectClass.add("person"); //$NON-NLS-1$
		
		try {
			serverContext.createSubcontext("uid=" + uid + ",ou=users", user); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NameAlreadyBoundException ignore) {
			System.out.println(" user " + uid + " already exists."); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NamingException ne) {
			System.err.println("Failed to create  user."); //$NON-NLS-1$
			ne.printStackTrace();
		}
	}

	protected void createGroupAndRole(String cn, String ou, String[] memberDns) {
		createGroupAndRole(cn, ou, memberDns, false, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void createGroupAndRole(String cn, String ou, String[] memberDns,
			boolean isOrganizational, String l, String st) {
		Attributes group = new BasicAttributes("cn", cn); //$NON-NLS-1$

		// check the flag for role
		Attribute members = null;
		if (isOrganizational) {
			group.put("l", l); //$NON-NLS-1$
			group.put("st", st); //$NON-NLS-1$
			members = new BasicAttribute("roleOccupant"); //$NON-NLS-1$
		} else {
			members = new BasicAttribute("uniqueMember"); //$NON-NLS-1$
		}
		Attribute orgUnit = new BasicAttribute("ou", ou); //$NON-NLS-1$

		for (int i = 0; i < memberDns.length; i++) {
			members.add(memberDns[i]);
		}

		Attribute objectClass = new BasicAttribute("objectClass"); //$NON-NLS-1$
		objectClass.add("top"); //$NON-NLS-1$

		// check the flag for role
		if (isOrganizational) {
			objectClass.add("organizationalRole"); //$NON-NLS-1$
		} else {
			objectClass.add("groupOfUniqueNames"); //$NON-NLS-1$
		}
		group.put(objectClass);
		group.put(members);
		group.put(orgUnit);

		try {
			if (isOrganizational) {
				serverContext.createSubcontext("cn=" + cn + ",ou=roles", group); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				serverContext
						.createSubcontext("cn=" + cn + ",ou=groups", group); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (NameAlreadyBoundException ignore) {
			System.out.println(" group " + cn + " already exists."); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NamingException ne) {
			System.err.println("Failed to create group."); //$NON-NLS-1$
			ne.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new PentahoLdapTestServer("dc=acegisecurity,dc=org"); //$NON-NLS-1$
		System.out.println("didn't get an exception!!!"); //$NON-NLS-1$
	}

	public StartupConfiguration getConfiguration() {
		return cfg;
	}

}
