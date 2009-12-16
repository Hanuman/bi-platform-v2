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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 * 
 * @author mbatchelor and gmoran
 *
*/
package org.pentaho.test.platform.plugin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianLookupMapUserRoleListMapper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianOneToOneUserRoleListMapper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianUserSessionUserRoleListMapper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

@SuppressWarnings("nls")
public class UserRoleMapperTest {

  private MicroPlatform microPlatform;
  
  @Before
  public void init0() {
    microPlatform = new MicroPlatform("test-src/solution");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL);
    microPlatform.define("connection-SQL", SQLConnection.class);
    microPlatform.define("connection-MDX", MDXConnection.class);
    microPlatform.define(IDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
    try {
      microPlatform.start();
    } catch (PlatformInitializationException ex) {
      Assert.fail();
    }

    MondrianCatalogHelper catalogService = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    catalogService.setDataSourcesConfig("file:" + 
        PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/test-datasources.xml")); 
    
    // JNDI
    System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
    System.setProperty("org.osjava.sj.root", "test-src/solution/system/simple-jndi");
    System.setProperty("org.osjava.sj.delimiter", "/");
    
  }
  
  @Test
  public void testReadRolesInSchema() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");
    MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);;
    Assert.assertNotNull(helper);
    MondrianCatalog mc = helper.getCatalog("SteelWheelsRoles", session);
    Assert.assertNotNull(mc);
    MondrianSchema ms = mc.getSchema();
    Assert.assertNotNull(ms);
    String[] roleNames = ms.getRoleNames();
    Assert.assertNotNull(roleNames);
    Assert.assertEquals(2, roleNames.length);
    Assert.assertEquals("Role1", roleNames[0]);
    Assert.assertEquals("Role2", roleNames[1]);
  }
  
  @Test
  public void testReadRolesInPlatform() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");
    
    Authentication auth = SecurityHelper.getAuthentication(session, false);
    Assert.assertNotNull(auth);
    GrantedAuthority[] gAuths = auth.getAuthorities();
    Assert.assertNotNull(gAuths);
    Assert.assertEquals(3, gAuths.length);
    Assert.assertEquals("ceo", gAuths[0].getAuthority());
    Assert.assertEquals("Admin", gAuths[1].getAuthority());
    Assert.assertEquals("Authenticated", gAuths[2].getAuthority());

  }

  @Test
  public void testMondrianUserSessionUserRoleListMapper() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");
    
    session.setAttribute("rolesAttribute", new Object[]{"mondrianRole1", "mondrianRole2", "mondrianRole3"});
    PentahoSessionHolder.setSession(session);
    
    MondrianUserSessionUserRoleListMapper mapper = new MondrianUserSessionUserRoleListMapper();
    mapper.setSessionProperty("rolesAttribute");

    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNotNull(roles);
      Assert.assertEquals(3, roles.length);
      Assert.assertEquals("mondrianRole1", roles[0]);
      Assert.assertEquals("mondrianRole2", roles[1]);
      Assert.assertEquals("mondrianRole3", roles[2]);
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testNoMatchMondrianUserSessionUserRoleListMapper() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");
    
    PentahoSessionHolder.setSession(session);
    
    MondrianUserSessionUserRoleListMapper mapper = new MondrianUserSessionUserRoleListMapper();
    mapper.setSessionProperty("rolesAttribute");

    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNull(roles);
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testLookupMapUserRoleListMapper() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");
    
    Map<String, String> lookup = new HashMap<String, String>();
    lookup.put("ceo", "Role1");
    lookup.put("Not Pentaho","Role2");
    lookup.put("Not Mondrian or Pentaho","Role3");
    
    MondrianLookupMapUserRoleListMapper mapper = new MondrianLookupMapUserRoleListMapper();
    mapper.setLookupMap(lookup);

    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNotNull(roles);
      Assert.assertEquals(1, roles.length);
      Assert.assertEquals("Role1", roles[0]);
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testNoMatchLookupMapUserRoleListMapper() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");
    
    Map<String, String> lookup = new HashMap<String, String>();
    lookup.put("No Match", "Role1");
    lookup.put("No Match Here Either","Role2");
    
    MondrianLookupMapUserRoleListMapper mapper = new MondrianLookupMapUserRoleListMapper();
    mapper.setLookupMap(lookup);

    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNull(roles);
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testMondrianOneToOneUserRoleListMapper() {
    IPentahoSession session = createSession("simplebob", "Role1", "Role2");

    IConnectionUserRoleMapper mapper = new MondrianOneToOneUserRoleListMapper();
    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNotNull(roles);
      Assert.assertEquals(2, roles.length);
      Assert.assertEquals("Role1", roles[0]);
      Assert.assertEquals("Role2", roles[1]);
      
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testNoMatchMondrianOneToOneUserRoleListMapper() {
    IPentahoSession session = this.createSession("joe", "ceo", "Admin", "Authenticated");

    IConnectionUserRoleMapper mapper = new MondrianOneToOneUserRoleListMapper();
    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNull(roles);
      
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  public IPentahoSession createSession(String uname, String... authorities) {
    StandaloneSession session = new StandaloneSession();
    session.setAuthenticated(uname); 
    
    GrantedAuthority[] auths = new GrantedAuthority[authorities.length];
    for (int i=0; i<authorities.length; i++) {
      auths[i] = new GrantedAuthorityImpl(authorities[i]);
    }

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(uname, "none", auths
    );
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
    return session;
  }

}
