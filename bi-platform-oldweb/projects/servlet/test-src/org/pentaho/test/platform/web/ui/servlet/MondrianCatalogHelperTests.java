package org.pentaho.test.platform.web.ui.servlet;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

public class MondrianCatalogHelperTests extends AbstractMondrianCatalogTestBase {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(MondrianCatalogHelperTests.class);

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public MondrianCatalogHelperTests() {
    super();
  }

  // ~ Methods =========================================================================================================

//  @Test
//  public void testInitWithMissingProperties() throws Exception {
//    MondrianCatalogHelper helper1 = new MondrianCatalogHelper();
//    try {
//      helper1.afterPropertiesSet();
//      fail("didn't throw expected exception");
//    } catch (Exception e) {
//      if (logger.isErrorEnabled()) {
//        logger.error("an exception occurred", e);
//      }
//    }
//  }

  @Test
  public void testInit() throws Exception {
    MondrianCatalogHelper helper2 = new MondrianCatalogHelper();
    helper2.setDataSourcesConfig("classpath:/org/pentaho/test/platform/web/ui/servlet/test-datasources.xml"); //$NON-NLS-1$
//    helper2.afterPropertiesSet();
  }

  //  @Test
  //  public void testAddCatalog() throws Exception {
  //    MondrianCatalogHelper helper2 = new MondrianCatalogHelper();
  //    Resource res = new ClassPathResource("/org/pentaho/ui/servlet/test-datasources.xml");
  //    File src = res.getFile();
  //    File tmp = File.createTempFile("datasources", ".xml");
  //    FileUtils.copyFile(src, tmp);
  //    System.out.println("using temp file: " + tmp.getAbsolutePath());
  //    helper2.setDataSourcesConfig("file:" + tmp.getAbsolutePath()); //$NON-NLS-1$
  //    //    helper2.setSolutionRepository(new DummySolutionRepository());
  //    helper2.afterPropertiesSet();
  //    MondrianDataSource mds = new MondrianDataSource("matds", "here is a ds", "df", "kldf", "dljdf", "dflkjdf", "dfs",
  //        null);
  //
  //    helper2.addCatalog(new MondrianCatalog("mat", "desc", "lkdf", mds, null), null);
  //  }

  @Test
  public void testListCatalogs() throws Exception {
    MondrianCatalogHelper helper = new MondrianCatalogHelper();
    helper.setDataSourcesConfig("classpath:/org/pentaho/test/platform/web/ui/servlet/test-datasources.xml"); //$NON-NLS-1$
//    helper.afterPropertiesSet();
    List<MondrianCatalog> cats = helper.listCatalogs(pentahoSession, false);
    System.out.println(cats);
    assertTrue(cats.size() == 3);
  }

  @Test
  public void testGetCatalog() throws Exception {
    MondrianCatalogHelper helper = new MondrianCatalogHelper();
    helper.setDataSourcesConfig("classpath:/org/pentaho/test/platform/web/ui/servlet/test-datasources.xml"); //$NON-NLS-1$
    // set to false since we're in test mode and there's only one mondrian.xml in the default solution repo
    helper.setUseSchemaNameAsCatalogName(false);
//    helper.afterPropertiesSet();
    MondrianCatalog cat = helper.getCatalog("SteelWheels3", pentahoSession); //$NON-NLS-1$
    System.out.println(cat);
    assertNotNull(cat);
  }

  @Test
  public void testJndiOnly() throws Exception {
    MondrianCatalogHelper helper = new MondrianCatalogHelper();
    helper.setDataSourcesConfig("classpath:/org/pentaho/test/platform/web/ui/servlet/test-datasources.xml"); //$NON-NLS-1$
//    helper.afterPropertiesSet();
    List<MondrianCatalog> cats = helper.listCatalogs(pentahoSession, true);
    System.out.println(cats);
    assertTrue(cats.size() == 1);
  }

  @Test
  public void testAddCatalogOverwrite() throws Exception {
    MondrianCatalogHelper helper = new MondrianCatalogHelper();
    helper.setDataSourcesConfig("file:" + destFile.getAbsolutePath()); //$NON-NLS-1$
    System.out.println("********************* " + destFile.getAbsolutePath());
    // set to false since we're in test mode and there's only one mondrian.xml in the default solution repo
    helper.setUseSchemaNameAsCatalogName(false);
//    helper.afterPropertiesSet();

    MondrianSchema schema = new MondrianSchema("SteelWheels3", null);
    MondrianDataSource ds = new MondrianDataSource("SteelWheels3", "", "", "Provider=mondrian;DataSource=SampleData;",
        "", "", "", null);

    MondrianCatalog cat = new MondrianCatalog("SteelWheels3", null,
        "solution:samples/steel-wheels/analysis/steelwheels.mondrian.xml", ds, schema);

    helper.addCatalog(cat, true, pentahoSession);
    List<MondrianCatalog> cats = helper.listCatalogs(pentahoSession, false);
    assertTrue("expected size=3, actual size=" + cats.size(), cats.size() == 3);
  }

  @Test
  public void testAddCatalogNoOverwrite() throws Exception {
    MondrianCatalogHelper helper = new MondrianCatalogHelper();
    helper.setDataSourcesConfig("file:" + destFile.getAbsolutePath()); //$NON-NLS-1$
    // set to false since we're in test mode and there's only one mondrian.xml in the default solution repo
    helper.setUseSchemaNameAsCatalogName(false);
//    helper.afterPropertiesSet();

    MondrianSchema schema = new MondrianSchema("SteelWheels3", null);
    MondrianDataSource ds = new MondrianDataSource("SteelWheels3", "", "", "Provider=mondrian;DataSource=SampleData;",
        "", "", "", null);

    MondrianCatalog cat = new MondrianCatalog("SteelWheels3", null,
        "solution:samples/steel-wheels/analysis/steelwheels.mondrian.xml", ds, schema);

    try {
      helper.addCatalog(cat, false, pentahoSession);
      fail("expected exception");
    } catch (MondrianCatalogServiceException e) {
      // expected (a good thing)
    }
    List<MondrianCatalog> cats = helper.listCatalogs(pentahoSession, false);
    assertTrue("expected size=3, actual size=" + cats.size(), cats.size() == 3);
  }
}