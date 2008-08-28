package org.pentaho.test.platform.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.servlet.UIServlet;
import org.pentaho.test.platform.engine.core.GenericPentahoTest;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.UIServlet</code>.
 * 
 * @author mlowery
 */
public class UIServletTest extends GenericPentahoTest {

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

	public void testDoGet() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setupAddParameter("solution", "samples"); //$NON-NLS-1$//$NON-NLS-2$
		request.setupAddParameter("path", "steel-wheels/reports"); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter("action", "Inventory List.xaction"); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter("details", "all"); //$NON-NLS-1$ //$NON-NLS-2$

		MockHttpServletResponse response = new MockHttpServletResponse();
    UIServlet servlet = new UIServlet();
		servlet.service(request, response);
		
		// System.out.println(response.getOutputStreamContent());
	}
}
