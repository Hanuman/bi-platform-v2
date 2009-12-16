package org.pentaho.test.platform.plugin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
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
    catalogService.setDataSourcesConfig("file:" + //$NON-NLS-1$
        PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/test-datasources.xml")); //$NON-NLS-1$
    
    // JNDI
    System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("org.osjava.sj.root", "test-src/solution/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
  @Test
  public void testReadRolesInSchema() {
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);
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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);
    
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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);
    
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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);
    
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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);
    
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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);
    
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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createSimpleBob(session);

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
    StandaloneSession session;
    session = new StandaloneSession();
    UserRoleMapperTest.createJoe(session);

    IConnectionUserRoleMapper mapper = new MondrianOneToOneUserRoleListMapper();
    try {
      String[] roles = mapper.mapConnectionRoles(session, "SteelWheelsRoles");
      Assert.assertNull(roles);
      
    } catch (PentahoAccessControlException e) {
      Assert.fail(e.getMessage());
    }
  }

  
  public static void createSuzy(StandaloneSession session) {
    session.setAuthenticated("suzy"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[3];
    auths[0] = new GrantedAuthorityImpl("ROLE_CTO"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("ROLE_IS"); //$NON-NLS-1$
    auths[2] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("suzy", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static void createJoe(StandaloneSession session) {
    session.setAuthenticated("joe"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[3];
    auths[0] = new GrantedAuthorityImpl("ceo"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("Admin"); //$NON-NLS-1$
    auths[2] = new GrantedAuthorityImpl("Authenticated"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("joe", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }
 
  public static void createSimpleBob(StandaloneSession session) {
    session.setAuthenticated("simplebob"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[2];
    auths[0] = new GrantedAuthorityImpl("Role1"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("Role2"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("simplebob", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

}
