package org.pentaho.test.platform.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.web.servlet.ViewAction;
import org.pentaho.test.platform.engine.core.BaseTest;

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
public class ViewActionServletTest extends BaseTest {
  private static final String SOLUTION_PATH = "projects/servlet/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
	
	public void setUp() {
	  System.setProperty(PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, getSolutionPath() + "/system/" + SystemSettings.PENTAHOSETTINGSFILENAME);
		StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(
				getSolutionPath(), ""); //$NON-NLS-1$
		PentahoSystem.init(applicationContext, getRequiredListeners());
	}

	 public String getSolutionPath() {
	    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
	    if(file.exists()) {
	      System.out.println("File exist returning " + SOLUTION_PATH);
	      return SOLUTION_PATH;  
	    } else {
	      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);      
	      return ALT_SOLUTION_PATH;
	    }
	    
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
