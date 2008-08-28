package org.pentaho.test.platform.web.ui.servlet;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.web.doubles.PentahoSessionDouble;

/**
 * Superclass of tests for IMondrianCatalogService and MondrianCatalogPublisher instances.
 * 
 * @author mlowery
 */
public abstract class AbstractMondrianCatalogTestBase extends BaseTest {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(AbstractMondrianCatalogTestBase.class);

  // ~ Instance fields =================================================================================================

  protected IPentahoSession pentahoSession;

  protected static final String DEFAULT_CONTENT_TEMPLATE = "-----1234\r\n" + "Content-Disposition: form-data; " //$NON-NLS-1$//$NON-NLS-2$
      + "name=\"file\"; " + "filename=\"{0}\"\r\n" //$NON-NLS-1$ //$NON-NLS-2$
      + "Content-Type: text/xml\r\n\r\n{1}\n\r\n" + "-----1234--\r\n"; //$NON-NLS-1$ //$NON-NLS-2$

  protected File destFile;

  protected static final String DEFAULT_FILENAME = "foo11.mondrian.xml"; //$NON-NLS-1$

  protected static final String DEFAULT_FILE_CONTENT = "<?xml version=\"1.0\"?><Schema name=\"Foo\" />"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================

  public AbstractMondrianCatalogTestBase() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Override
  public void setUp() {
    super.setUp();
    setUpTempFile();
    setUpPentahoSession();
  }

  /**
   * Makes a copy of the test-datasources.xml so the test can write to it and muck it up.
   */
  protected void setUpTempFile() {
    InputStream src = this.getClass().getResourceAsStream("/org/pentaho/test/platform/web/ui/servlet/test-datasources.xml");
    OutputStream dest = null;
    try {
      destFile = File.createTempFile("test-datasources", ".xml");
      dest = new FileOutputStream(destFile);
      IOUtils.copy(src, dest);
    } catch (FileNotFoundException e) {
      if (logger.isErrorEnabled()) {
        logger.error("an exception occurred", e);
      }
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("an exception occurred", e);
      }
    }
    IOUtils.closeQuietly(src);
    IOUtils.closeQuietly(dest);
  }

  /**
   * Creates a dummy IPentahoSession to work with.
   */
  protected void setUpPentahoSession() {
    final String USERNAME = "joe"; //$NON-NLS-1$
    pentahoSession = new PentahoSessionDouble(USERNAME);
    pentahoSession.setAuthenticated(USERNAME);
    GrantedAuthority[] roles = new GrantedAuthority[2];
    roles[0] = new GrantedAuthorityImpl("Authenticated"); //$NON-NLS-1$
    roles[1] = new GrantedAuthorityImpl("Admin"); //$NON-NLS-1$
    Authentication auth = new TestingAuthenticationToken(USERNAME, "password", roles); //$NON-NLS-1$
    auth.setAuthenticated(true);
    pentahoSession.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
  }

  @Override
  public void tearDown() {
    super.tearDown();
    pentahoSession = null;
    if (null != destFile) {
      FileUtils.deleteQuietly(destFile);
    }
  }

}