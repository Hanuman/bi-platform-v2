package org.pentaho.test.platform.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.servlet.GetImage;
import org.pentaho.test.platform.engine.core.GenericPentahoTest;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.GetImage</code>.
 * 
 * @author mlowery
 */
public class GetImageTest extends GenericPentahoTest {
	private static final String SOLUTION_PATH = "test-src/solution";
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

	public void testGetImage() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
    request.setupAddParameter("image", "picture.png"); //$NON-NLS-1$ //$NON-NLS-2$
    MockServletConfig config = new MockServletConfig();
    MockServletContext context = new MockServletContext();
    context.setServletContextName("pentaho");
    config.setServletContext(context);
    config.setServletName("getImage");
    request.setContextPath("pentaho");
    session.setupServletContext(context);
    MockHttpServletResponse response = new MockHttpServletResponse();
    GetImage servlet = new GetImage();
    servlet.init(config);
		servlet.service(request, response);
    
		// System.out.println(response.getOutputStreamContent());
	}


    @Override
	public String getSolutionPath() {
		return SOLUTION_PATH;
	}
}
