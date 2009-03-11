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


import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.ldap.LdapCallback;
import org.acegisecurity.ldap.LdapTemplate;
import org.acegisecurity.ldap.LdapUserSearch;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsImpl;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.util.Assert;

/**
 * In talking with a member of the Acegi Security team, the Acegi Security team
 * believes that the embedded Apache Directory Server is incorrectly
 * implemented. So in order to run an embedded ApacheDS instance for testing
 * purposes, this class was created as a workaround for behavior of ApacheDS.
 * 
 * <p>
 * Some links:
 * </p>
 * <ul>
 * <li><a href="https://issues.apache.org/jira/browse/DIRSERVER-169">Apache
 * Directory Server JIRA case</a></li>
 * <li><a
 * href="http://www.webservertalk.com/archive384-2005-12-1326956.html">Another
 * link on the issue</a></li>
 * </ul>
 * 
 * @author mlowery
 */
public class ApacheDirectoryServerUserSearch implements LdapUserSearch {
	// ~ Static fields/initializers
	// =====================================================================================

	private static final Log logger = LogFactory
			.getLog(ApacheDirectoryServerUserSearch.class);

	// ~ Instance fields
	// ================================================================================================

	private InitialDirContextFactory initialDirContextFactory;

	private LdapUserDetailsMapper userDetailsMapper = new LdapUserDetailsMapper();

	/**
	 * The LDAP SearchControls object used for the search. Shared between
	 * searches so shouldn't be modified once the bean has been configured.
	 */
	private SearchControls searchControls = new SearchControls();

	/**
	 * Context name to search in, relative to the root DN of the configured
	 * InitialDirContextFactory.
	 */
	private String searchBase = ""; //$NON-NLS-1$

	/**
	 * The filter expression used in the user search. This is an LDAP search
	 * filter (as defined in 'RFC 2254') with optional arguments. See the
	 * documentation for the <tt>search</tt> methods in {@link
	 * javax.naming.directory.DirContext DirContext} for more information.
	 * <p>
	 * In this case, the username is the only parameter.
	 * </p>
	 * Possible examples are:
	 * <ul>
	 * <li>(uid={0}) - this would search for a username match on the uid
	 * attribute.</li>
	 * </ul>
	 * TODO: more examples.
	 */
	private String searchFilter;

	// ~ Constructors
	// ===================================================================================================

	public ApacheDirectoryServerUserSearch(String searchBase,
			String searchFilter,
			InitialDirContextFactory initialDirContextFactory) {
		Assert.notNull(initialDirContextFactory,
				"initialDirContextFactory must not be null"); //$NON-NLS-1$
		Assert.notNull(searchFilter, "searchFilter must not be null."); //$NON-NLS-1$
		Assert.notNull(searchBase,
				"searchBase must not be null (an empty string is acceptable)."); //$NON-NLS-1$

		this.searchFilter = searchFilter;
		this.initialDirContextFactory = initialDirContextFactory;
		this.searchBase = searchBase;

		if (searchBase.length() == 0) {
			logger
					.info("SearchBase not set. Searches will be performed from the root: " //$NON-NLS-1$
							+ initialDirContextFactory.getRootDn());
		}
	}

	// ~ Methods
	// ========================================================================================================

	/**
	 * Return the LdapUserDetails containing the user's information
	 * 
	 * @param username
	 *            the username to search for.
	 * 
	 * @return An LdapUserDetails object containing the details of the located
	 *         user's directory entry
	 * 
	 * @throws UsernameNotFoundException
	 *             if no matching entry is found.
	 */
	public LdapUserDetails searchForUser(final String username) {
		DirContext ctx = initialDirContextFactory.newInitialDirContext();

		if (logger.isDebugEnabled()) {
			logger.debug("Searching for user '" + username + "', in context " //$NON-NLS-1$ //$NON-NLS-2$
					+ ctx + ", with user search " + this.toString()); //$NON-NLS-1$
		}

		LdapTemplate template = new LdapTemplate(initialDirContextFactory);

		template.setSearchControls(searchControls);

		try {
			LdapUserDetailsImpl.Essence user = (LdapUserDetailsImpl.Essence) template
					.execute(new LdapCallback() {
						public Object doInDirContext(DirContext dirCtx)
								throws NamingException {
							NamingEnumeration results = dirCtx.search(searchBase,
									searchFilter, new String[] { username },
									searchControls);

							if (!results.hasMore()) {
								throw new IncorrectResultSizeDataAccessException(
										1, 0);
							}

							SearchResult searchResult = (SearchResult) results
									.next();

							if (results.hasMore()) {
								// We don't know how many results but set to 2
								// which is
								// good enough
								throw new IncorrectResultSizeDataAccessException(
										1, 2);
							}

							return userDetailsMapper.mapAttributes(searchResult
									.getName(), searchResult.getAttributes());
						}
					});

			user.setUsername(username);

			return user.createUserDetails();
		} catch (IncorrectResultSizeDataAccessException notFound) {
			if (notFound.getActualSize() == 0) {
				throw new UsernameNotFoundException("User " + username //$NON-NLS-1$
						+ " not found in directory."); //$NON-NLS-1$
			}
			// Search should never return multiple results if properly
			// configured, so just rethrow
			throw notFound;
		}
	}

	/**
	 * Sets the corresponding property on the {@link SearchControls} instance
	 * used in the search.
	 * 
	 * @param deref
	 *            the derefLinkFlag value as defined in SearchControls..
	 */
	public void setDerefLinkFlag(boolean deref) {
		searchControls.setDerefLinkFlag(deref);
	}

	/**
	 * If true then searches the entire subtree as identified by context, if
	 * false (the default) then only searches the level identified by the
	 * context.
	 * 
	 * @param searchSubtree
	 *            true the underlying search controls should be set to
	 *            SearchControls.SUBTREE_SCOPE rather than
	 *            SearchControls.ONELEVEL_SCOPE.
	 */
	public void setSearchSubtree(boolean searchSubtree) {
		searchControls
				.setSearchScope(searchSubtree ? SearchControls.SUBTREE_SCOPE
						: SearchControls.ONELEVEL_SCOPE);
	}

	/**
	 * The time to wait before the search fails; the default is zero, meaning
	 * forever.
	 * 
	 * @param searchTimeLimit
	 *            the time limit for the search (in milliseconds).
	 */
	public void setSearchTimeLimit(int searchTimeLimit) {
		searchControls.setTimeLimit(searchTimeLimit);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[ searchFilter: '").append(searchFilter).append("', "); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("searchBase: '").append(searchBase).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
		sb
				.append(", scope: ") //$NON-NLS-1$
				.append(
						(searchControls.getSearchScope() == SearchControls.SUBTREE_SCOPE) ? "subtree" //$NON-NLS-1$
								: "single-level, "); //$NON-NLS-1$
		sb.append("searchTimeLimit: ").append(searchControls.getTimeLimit()); //$NON-NLS-1$
		sb.append("derefLinkFlag: ").append(searchControls.getDerefLinkFlag()) //$NON-NLS-1$
				.append(" ]"); //$NON-NLS-1$

		return sb.toString();
	}
}
