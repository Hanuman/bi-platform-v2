package org.pentaho.test.platform.plugin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianAbstractPlatformUserRoleMapper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianLookupMapUserRoleListMapper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
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
    microPlatform.define("connection-SQL", SQLConnection.class);
    microPlatform.define("connection-MDX", MDXConnection.class);
    microPlatform.define(IDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
    try {
      microPlatform.start();
    } catch (PlatformInitializationException ex) {
      Assert.fail();
    }
    
    // Datasources
    MondrianCatalogHelper helper = MondrianCatalogHelper.getInstance();
//    helper.setDataSourcesConfig("file:test/analysis/test-datasources.xml"); //$NON-NLS-1$
    helper.setDataSourcesConfig("file:" + //$NON-NLS-1$
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
    MondrianCatalogHelper helper = MondrianCatalogHelper.getInstance();
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
  public void testLookupMapUserRoleListMapper() {
    IConnectionUserRoleMapper mapper = new MondrianLookupMapUserRoleListMapper();
    
  }
  
//  public String[] resolveRoles(MondrianAbstractPlatformUserRoleMapper mapper, int flag) {
//    mapper.
//  }

  public class TestPlatformLookupMapUserRoleMapper extends MondrianLookupMapUserRoleListMapper {

    protected String[] mapRoles(String[] mondrianRoles, String[] platformRoles, String mondrianCatalog) {
      return super.mapRoles(mondrianRoles, platformRoles, mondrianCatalog);
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
    auth.setAuthenticated(true);
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
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }
 
}
