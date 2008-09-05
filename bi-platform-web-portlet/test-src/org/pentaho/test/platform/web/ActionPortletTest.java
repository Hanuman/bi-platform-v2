package org.pentaho.test.platform.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.web.portal.ActionPortlet;
import org.pentaho.platform.web.portal.PentahoPortletSession;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import org.springframework.mock.web.portlet.MockActionRequest;
import org.springframework.mock.web.portlet.MockActionResponse;
import org.springframework.mock.web.portlet.MockPortletConfig;
import org.springframework.mock.web.portlet.MockPortletContext;
import org.springframework.mock.web.portlet.MockPortletSession;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;

public class ActionPortletTest extends BaseTestCase {


  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
      return SOLUTION_PATH;
  }
  public void setUp() {
    System.setProperty(PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, getSolutionPath() + "/system/" + SystemSettings.PENTAHOSETTINGSFILENAME);
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(
        getSolutionPath(), ""); //$NON-NLS-1$
    PentahoSystem.init(applicationContext, getRequiredListeners());
  }

  protected Map getRequiredListeners() {
    HashMap listeners = new HashMap();
    listeners.put("globalObjects", "globalObjects"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testDoPortletView() throws ServletException, IOException {
    setUp();
    MockPortletContext portletContext = new MockPortletContext();
    MockPortletConfig portletConfig = new MockPortletConfig(portletContext, "ActionPortlet") ;
    MockPortletSession session = new MockPortletSession(portletContext);
    MockRenderRequest renderRequest = new MockRenderRequest(portletContext);
    MockRenderResponse renderResponse = new MockRenderResponse();
    MockPortletContext context = new MockPortletContext();
    context.setPortletContextName("pentaho");
    MockPortletConfig config = new MockPortletConfig(context);
    
    renderRequest.setSession(session);
    renderRequest.addParameter("solution", "samples"); //$NON-NLS-1$//$NON-NLS-2$
    renderRequest.addParameter("path", "getting-started"); //$NON-NLS-1$ //$NON-NLS-2$
    renderRequest.addParameter("action", "HelloWorld.xaction"); //$NON-NLS-1$ //$NON-NLS-2$

    ActionPortlet portlet = new ActionPortlet();
    PentahoPortletSession portletSession = new PentahoPortletSession("Joe", session, Locale.US );
    try {
      portlet.init(config);
      portlet.doPortletView((RenderRequest) renderRequest, (RenderResponse)renderResponse, portletSession);
    } catch(Exception e) {
      assertTrue("Should not have thrown the excepton", false);
    }
    assertTrue(true);
  }

  public void testDoProcessPortletAction() throws ServletException, IOException {
    setUp();
    MockActionRequest actionRequest = new MockActionRequest();
    MockActionResponse actionResponse = new MockActionResponse();
    MockPortletContext context = new MockPortletContext();
    context.setPortletContextName("pentaho");
    MockPortletConfig config = new MockPortletConfig(context);
    ActionPortlet portlet = new ActionPortlet();
    try {
       portlet.init(config);
       portlet.processAction(actionRequest, actionResponse);
    } catch(Exception e) {
      assertTrue("Should not have thrown the excepton", false);
    }
    assertTrue(true);
  }

  public static void main(String[] args) {
    ActionPortletTest test = new ActionPortletTest();
      test.setUp();
      try {
          test.testDoPortletView();
          test.testDoProcessPortletAction();
      } catch(Exception e) {
        e.printStackTrace();
      } finally {
          BaseTest.shutdown();
      }
  }
    
}