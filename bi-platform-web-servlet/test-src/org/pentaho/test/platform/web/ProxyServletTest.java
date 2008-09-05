package org.pentaho.test.platform.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.servlet.ProxyServlet;
import org.pentaho.test.platform.engine.core.BaseTestCase;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.ProxyServlet</code>.
 * 
 * @author mlowery
 */
public class ProxyServletTest extends BaseTestCase {
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

	public void testService() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setupAddParameter("ProxyURL", "http://www.pentaho.org"); //$NON-NLS-1$//$NON-NLS-2$

		MockHttpServletResponse response = new MockHttpServletResponse();
    ProxyServlet servlet = new ProxyServlet();
    servlet.service(request, response);
	}

}
