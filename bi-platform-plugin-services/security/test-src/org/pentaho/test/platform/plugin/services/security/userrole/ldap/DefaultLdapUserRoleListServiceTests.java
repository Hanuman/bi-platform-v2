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


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.ldap.LdapUserSearch;
import org.acegisecurity.providers.ldap.populator.DefaultLdapAuthoritiesPopulator;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.security.DefaultGrantedAuthorityComparator;
import org.pentaho.platform.engine.security.DefaultUsernameComparator;
import org.pentaho.platform.plugin.services.security.userrole.ldap.DefaultLdapUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.ldap.LdapUserDetailsService;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.GenericLdapSearch;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearchParamsFactory;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearchParamsFactoryImpl;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.UnionizingLdapSearch;
import org.pentaho.platform.plugin.services.security.userrole.ldap.transform.GrantedAuthorityToString;
import org.pentaho.platform.plugin.services.security.userrole.ldap.transform.SearchResultToAttrValueList;
import org.pentaho.platform.plugin.services.security.userrole.ldap.transform.StringToGrantedAuthority;

/**
 * Tests for the <code>DefaultLdapUserRoleListService</code> class. The ways
 * in which an LDAP schema can be layed out are numerous. See the comment for
 * each method to get an idea of how the schema is layed out in each example.
 * 
 * @author mlowery
 */
public class DefaultLdapUserRoleListServiceTests extends AbstractLdapServerTestCase {

  private static final Log logger = LogFactory.getLog(DefaultLdapUserRoleListServiceTests.class);

  /**
   * Get the roles of user <code>suzy</code> by extracting the
   * <code>cn</code> token from the <code>uniqueMember</code> attribute of
   * the object that matches base of <code>ou=users</code> and filter of
   * <code>(uid={0})</code>.
   * 
   * <p>
   * Note that the UserDetailsService used by Acegi Security is re-used here.
   * </p>
   * @throws Exception 
   */
/*  public void testGetAuthoritiesForUser1() throws Exception {
    LdapUserSearch userSearch = getUserSearch("ou=users", "(uid={0})", //$NON-NLS-1$//$NON-NLS-2$
        getInitialCtxFactory());

    LdapUserDetailsService service = new LdapUserDetailsService();
    service.setPopulator(new NoOpLdapAuthoritiesPopulator());
    service.setUserSearch(userSearch);

    RolePreprocessingMapper mapper = new RolePreprocessingMapper();
    mapper.setRoleAttributes(new String[] { "uniqueMember" }); //$NON-NLS-1$
    mapper.setTokenName("cn"); //$NON-NLS-1$
    service.setUserDetailsMapper(mapper);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    service.afterPropertiesSet();

    userRoleListService.setUserDetailsService(service);

    List res = Arrays.asList(userRoleListService.getAuthoritiesForUser("suzy")); //$NON-NLS-1$

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_IS"))); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAuthoritiesForUser1(): " + res); //$NON-NLS-1$
    }

  }*/

  /**
   * Get the roles of user <code>suzy</code> by returning the
   * <code>cn</code> attribute of each object that matches base of
   * <code>ou=roles</code> and filter of <code>(roleOccupant={0})</code>.
   * 
   * <p>
   * Note that the UserDetailsService used by Acegi Security is re-used here.
   * </p>
   */
  public void testGetAuthoritiesForUser2() {
    DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(getInitialCtxFactory(), "ou=roles"); //$NON-NLS-1$
    populator.setGroupRoleAttribute("cn"); //$NON-NLS-1$
    populator.setGroupSearchFilter("(roleOccupant={0})"); //$NON-NLS-1$

    LdapUserSearch userSearch = getUserSearch("ou=users", "(uid={0})", //$NON-NLS-1$//$NON-NLS-2$
        getInitialCtxFactory());

    LdapUserDetailsService service = new LdapUserDetailsService();
    service.setPopulator(populator);
    service.setUserSearch(userSearch);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUserDetailsService(service);

    List res = Arrays.asList(userRoleListService.getAuthoritiesForUser("suzy")); //$NON-NLS-1$

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_IS"))); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAuthoritiesForUser2(): " + res); //$NON-NLS-1$
    }

  }

  /**
   * Same as above except sorted.
   */
  public void testGetAuthoritiesForUser2Sorted() {
    DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(getInitialCtxFactory(), "ou=roles"); //$NON-NLS-1$
    populator.setGroupRoleAttribute("cn"); //$NON-NLS-1$
    populator.setGroupSearchFilter("(roleOccupant={0})"); //$NON-NLS-1$

    LdapUserSearch userSearch = getUserSearch("ou=users", "(uid={0})", //$NON-NLS-1$//$NON-NLS-2$
        getInitialCtxFactory());

    LdapUserDetailsService service = new LdapUserDetailsService();
    service.setPopulator(populator);
    service.setUserSearch(userSearch);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUserDetailsService(service);
    userRoleListService.setGrantedAuthorityComparator(new DefaultGrantedAuthorityComparator());

    List res = Arrays.asList(userRoleListService.getAuthoritiesForUser("suzy")); //$NON-NLS-1$

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_IS"))); //$NON-NLS-1$

    assertTrue(res.indexOf(new GrantedAuthorityImpl("ROLE_CTO")) < res.indexOf(new GrantedAuthorityImpl("ROLE_IS")));

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAuthoritiesForUser2Sorted(): " + res); //$NON-NLS-1$
    }

  }

  /**
   * Search for all users starting at <code>ou=groups</code>, looking for
   * objects with <code>objectClass=groupOfUniqueNames</code>, and
   * extracting the <code>uid</code> token of the <code>uniqueMember</code>
   * attribute.
   */
  public void testGetAllUserNames1() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "uniqueMember" }); //$NON-NLS-1$

    LdapSearchParamsFactoryImpl paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=groups", "(objectClass=groupOfUniqueNames)", con1); //$NON-NLS-1$//$NON-NLS-2$
    paramFactory.afterPropertiesSet();

    Transformer transformer1 = new SearchResultToAttrValueList("uniqueMember", "uid"); //$NON-NLS-1$ //$NON-NLS-2$

    GenericLdapSearch allUsernamesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1);
    allUsernamesSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllUsernamesSearch(allUsernamesSearch);

    List res = Arrays.asList(userRoleListService.getAllUsernames());

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("joe")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllUserNames1(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Same as above except sorted.
   */
  public void testGetAllUserNames1Sorted() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "uniqueMember" }); //$NON-NLS-1$

    LdapSearchParamsFactoryImpl paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=groups", "(objectClass=groupOfUniqueNames)", con1); //$NON-NLS-1$//$NON-NLS-2$
    paramFactory.afterPropertiesSet();

    Transformer transformer1 = new SearchResultToAttrValueList("uniqueMember", "uid"); //$NON-NLS-1$ //$NON-NLS-2$

    GenericLdapSearch allUsernamesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1);
    allUsernamesSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllUsernamesSearch(allUsernamesSearch);
    userRoleListService.setUsernameComparator(new DefaultUsernameComparator());

    List res = Arrays.asList(userRoleListService.getAllUsernames());

    assertTrue(res.indexOf("pat") < res.indexOf("tiffany"));

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllUserNames1Sorted(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=users</code>, looking for
   * objects with <code>objectClass=person</code>, and returning the
   * <code>uniqueMember</code> attribute.
   */
  public void testGetAllUserNames2() {
    SearchControls con2 = new SearchControls();
    con2.setReturningAttributes(new String[] { "uid" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl("ou=users", "(objectClass=person)", con2); //$NON-NLS-1$ //$NON-NLS-2$

    Transformer transformer2 = new SearchResultToAttrValueList("uid"); //$NON-NLS-1$

    LdapSearch allUsernamesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory, transformer2);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllUsernamesSearch(allUsernamesSearch);

    List res = Arrays.asList(userRoleListService.getAllUsernames());

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("joe")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllUserNames2(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=roles</code>, looking for
   * objects with <code>objectClass=organizationalRole</code>, and
   * extracting the <code>uid</code> token of the <code>roleOccupant</code>
   * attribute.
   */
  public void testGetAllUserNames3() {
    SearchControls con3 = new SearchControls();
    con3.setReturningAttributes(new String[] { "roleOccupant" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl(
        "ou=roles", "(objectClass=organizationalRole)", con3); //$NON-NLS-1$ //$NON-NLS-2$

    Transformer transformer3 = new SearchResultToAttrValueList("roleOccupant", "uid"); //$NON-NLS-1$ //$NON-NLS-2$

    LdapSearch allUsernamesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory, transformer3);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllUsernamesSearch(allUsernamesSearch);

    List res = Arrays.asList(userRoleListService.getAllUsernames());

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$
    assertTrue(res.contains("joe")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllUserNames3(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=users</code>, looking for
   * objects with <code>businessCategory=cn={0}*</code>, and returning the
   * <code>uid</code> attribute. This search implies that the schema is
   * setup such that a user's roles come from one of the user's attributes.
   */
  public void testGetUsernamesInRole1() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "uid" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=users", "(businessCategory=cn={0}*)", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList("uid"); //$NON-NLS-1$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1,
        transformer2);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUsernamesInRoleSearch(usernamesInRoleSearch);

    List res = Arrays.asList(userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_DEV"))); //$NON-NLS-1$

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getUsernamesInRole1(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Same as above except sorted.
   */
  public void testGetUsernamesInRole1Sorted() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "uid" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=users", "(businessCategory=cn={0}*)", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList("uid"); //$NON-NLS-1$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1,
        transformer2);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUsernamesInRoleSearch(usernamesInRoleSearch);
    userRoleListService.setUsernameComparator(new DefaultUsernameComparator());

    List res = Arrays.asList(userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_DEV"))); //$NON-NLS-1$

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$

    assertTrue(res.indexOf("pat") < res.indexOf("tiffany"));

    if (logger.isDebugEnabled()) {
      logger.debug("results of getUsernamesInRole1Sorted(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=roles</code>, looking for
   * objects with <code>(&(objectClass=organizationalRole)(cn={0}))</code>,
   * and extracting the <code>uid</code> token of the
   * <code>roleOccupant</code> attribute. This search implies that the
   * schema is setup such that a user's roles come from that user's DN being
   * present in the <code>roleOccupant</code> attribute of a child object
   * under the <code>ou=roles</code> object.
   */
  public void testGetUsernamesInRole2() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "roleOccupant" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=roles", "(&(objectClass=organizationalRole)(cn={0}))", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList("roleOccupant", "uid"); //$NON-NLS-1$ //$NON-NLS-2$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1,
        transformer2);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUsernamesInRoleSearch(usernamesInRoleSearch);

    List res = Arrays.asList(userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_DEV"))); //$NON-NLS-1$

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getUsernamesInRole2(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=groups</code>, looking for
   * objects with <code>(&(objectClass=groupOfUniqueNames)(cn={0}))</code>,
   * and extracting the <code>uid</code> token of the
   * <code>uniqueMember</code> attribute. This search implies that the
   * schema is setup such that a user's roles come from that user's DN being
   * present in the <code>uniqueMember</code> attribute of a child object
   * under the <code>ou=groups</code> object.
   */
  public void testGetUsernamesInRole3() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "uniqueMember" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=groups", "(&(objectClass=groupOfUniqueNames)(cn={0}))", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList("uniqueMember", "uid"); //$NON-NLS-1$ //$NON-NLS-2$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1,
        transformer2);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUsernamesInRoleSearch(usernamesInRoleSearch);

    List res = Arrays.asList(userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_DEVELOPMENT"))); //$NON-NLS-1$

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getUsernamesInRole3(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=groups</code>, looking for
   * objects with <code>(&(objectClass=groupOfUniqueNames)(cn={0}))</code>,
   * and extracting the <code>uid</code> token of the
   * <code>uniqueMember</code> attribute. This search implies that the
   * schema is setup such that a user's roles come from that user's DN being
   * present in the <code>uniqueMember</code> attribute of a child object
   * under the <code>ou=groups</code> object.
   * @throws Exception 
   */
  public void testGetUsernamesInRole4() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "uniqueMember" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory = new LdapSearchParamsFactoryImpl(
        "ou=groups", "(&(objectClass=groupOfUniqueNames)(cn={0}))", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList("uniqueMember", "uid"); //$NON-NLS-1$ //$NON-NLS-2$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch = new GenericLdapSearch(getInitialCtxFactory(), paramFactory, transformer1,
        transformer2);

    SearchControls con2 = new SearchControls();
    con2.setReturningAttributes(new String[] { "uid" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory2 = new LdapSearchParamsFactoryImpl(
        "ou=users", "(businessCategory=cn={0}*)", con2); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer3 = new SearchResultToAttrValueList("uid"); //$NON-NLS-1$

    GrantedAuthorityToString transformer4 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch2 = new GenericLdapSearch(getInitialCtxFactory(), paramFactory2, transformer3,
        transformer4);

    Set searches = new HashSet();
    searches.add(usernamesInRoleSearch);
    searches.add(usernamesInRoleSearch2);
    UnionizingLdapSearch unionSearch = new UnionizingLdapSearch(searches);
    unionSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setUsernamesInRoleSearch(unionSearch);

    List res = Arrays.asList(userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_DEV"))); //$NON-NLS-1$

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getUsernamesInRole4() with role=ROLE_DEV: " + res); //$NON-NLS-1$
    }

    res = Arrays.asList(userRoleListService.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_DEVELOPMENT"))); //$NON-NLS-1$

    assertTrue(res.contains("pat")); //$NON-NLS-1$
    assertTrue(res.contains("tiffany")); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getUsernamesInRole4() with role=ROLE_DEVELOPMENT: " + res); //$NON-NLS-1$
    }

  }

  /**
   * Search for all roles (aka authorities) starting at <code>ou=roles</code>,
   * looking for objects with <code>objectClass=organizationalRole</code>,
   * and returning the <code>cn</code> attribute.
   */
  public void testGetAllAuthorities1() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "cn" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl(
        "ou=roles", "(objectClass=organizationalRole)", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList("cn"); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer(transformers);

    LdapSearch rolesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory, transformer);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllAuthoritiesSearch(rolesSearch);

    List res = Arrays.asList(userRoleListService.getAllAuthorities());

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_CTO"))); //$NON-NLS-1$
    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_CEO"))); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllAuthorities1(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Same as above except sorted.
   */
  public void testGetAllAuthorities1Sorted() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "cn" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl(
        "ou=roles", "(objectClass=organizationalRole)", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList("cn"); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer(transformers);

    LdapSearch rolesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory, transformer);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllAuthoritiesSearch(rolesSearch);
    userRoleListService.setGrantedAuthorityComparator(new DefaultGrantedAuthorityComparator());

    List res = Arrays.asList(userRoleListService.getAllAuthorities());

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_CTO"))); //$NON-NLS-1$
    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_CEO"))); //$NON-NLS-1$

    assertTrue(res.indexOf(new GrantedAuthorityImpl("ROLE_ADMIN")) < res.indexOf(new GrantedAuthorityImpl("ROLE_DEV")));

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllAuthorities1Sorted(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Search for all roles (aka authorities) starting at <code>ou=groups</code>,
   * looking for objects with <code>objectClass=groupOfUniqueNames</code>,
   * and returning the <code>cn</code> attribute.
   */
  public void testGetAllAuthorities2() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "cn" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl(
        "ou=groups", "(objectClass=groupOfUniqueNames)", con1); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList("cn"); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer(transformers);

    LdapSearch rolesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory, transformer);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllAuthoritiesSearch(rolesSearch);

    List res = Arrays.asList(userRoleListService.getAllAuthorities());

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_SALES"))); //$NON-NLS-1$
    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_MARKETING"))); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllAuthorities2(): " + res); //$NON-NLS-1$
    }
  }

  /**
   * Union the results of two different searches.
   * <ul>
   * <li>Search 1: Search for all roles (aka authorities) starting at
   * <code>ou=groups</code>, looking for objects with
   * <code>objectClass=groupOfUniqueNames</code>, and returning the
   * <code>cn</code> attribute.</li>
   * <li>Search 2: Search for all roles (aka authorities) starting at
   * <code>ou=roles</code>, looking for objects with
   * <code>objectClass=organizationalRole</code>, and returning the
   * <code>cn</code> attribute.</li>
   * </ul>
   */
  public void testGetAllAuthorities3() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes(new String[] { "cn" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl(
        "ou=roles", "(objectClass=organizationalRole)", con1); //$NON-NLS-1$ //$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList("cn"); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer(transformers);

    LdapSearch rolesSearch = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory, transformer);

    SearchControls con2 = new SearchControls();
    con1.setReturningAttributes(new String[] { "cn" }); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory2 = new LdapSearchParamsFactoryImpl(
        "ou=groups", "(objectClass=groupOfUniqueNames)", con2); //$NON-NLS-1$//$NON-NLS-2$

    Transformer oneB = new SearchResultToAttrValueList("cn"); //$NON-NLS-1$
    Transformer twoB = new StringToGrantedAuthority();
    Transformer[] transformers2 = { oneB, twoB };
    Transformer transformer2 = new ChainedTransformer(transformers2);

    LdapSearch rolesSearch2 = new GenericLdapSearch(getInitialCtxFactory(), paramsFactory2, transformer2);

    Set searches = new HashSet();
    searches.add(rolesSearch);
    searches.add(rolesSearch2);
    UnionizingLdapSearch unionSearch = new UnionizingLdapSearch(searches);

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService(getInitialCtxFactory());

    userRoleListService.setAllAuthoritiesSearch(unionSearch);

    List res = Arrays.asList(userRoleListService.getAllAuthorities());

    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_DEVMGR"))); //$NON-NLS-1$
    assertTrue(res.contains(new GrantedAuthorityImpl("ROLE_DEVELOPMENT"))); //$NON-NLS-1$

    if (logger.isDebugEnabled()) {
      logger.debug("results of getAllAuthorities3(): " + res); //$NON-NLS-1$
    }

  }

}
