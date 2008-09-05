package org.pentaho.test.platform.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.servlet.GetResource;
import org.pentaho.test.platform.engine.core.BaseTestCase;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.GetMondrianModel</code>.
 * 
 * @author mlowery
 */
public class GetResourceTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";


  public String getSolutionPath() {
      return SOLUTION_PATH;  
  }
	public void setUp() {
		StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(
				getSolutionPath(), ""); //$NON-NLS-1$
		PentahoSystem.init(applicationContext, getRequiredListeners());
	}

	protected Map getRequiredListeners() {
		HashMap listeners = new HashMap();
		listeners.put("globalObjects", "globalObjects"); //$NON-NLS-1$ //$NON-NLS-2$
		return listeners;
	}

	public void testGetResouce() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
    request.setupAddParameter("resource", "adhoc/picklist.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockServletContext context = new MockServletContext();
		context.setServletContextName("pentaho");
		MockServletConfig config = new MockServletConfig();
		config.setServletContext(context);
		config.setServletName("getResource");
		request.setContextPath("pentaho");
    GetResource servlet = new GetResource();
    servlet.init(config);
    servlet.service(request, response);
	}
}
