package org.pentaho.test.platform.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.web.servlet.ViewAction;
import org.pentaho.test.platform.engine.core.BaseTestCase;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.ViewAction</code>.
 * 
 * @author mlowery
 */
public class ViewActionServletTest extends BaseTestCase {
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

	public void testDoGet() throws ServletException, IOException {
	  setUp();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setupAddParameter("solution", "samples"); //$NON-NLS-1$//$NON-NLS-2$
		request.setupAddParameter("path", "getting-started"); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter("action", "HelloWorld.xaction"); //$NON-NLS-1$ //$NON-NLS-2$
    MockServletContext context = new MockServletContext();
    context.setServletContextName("pentaho");
    MockServletConfig config = new MockServletConfig();
    config.setServletContext(context);
    config.setServletName("viewAction");
    request.setContextPath("pentaho");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ViewAction servlet = new ViewAction();
    servlet.init(config);
		servlet.service(request, response);
		
		// System.out.println(response.getOutputStreamContent());
	}

}
