package org.pentaho.test.platform.plugin;


import java.io.File;
import java.io.OutputStream;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTestCase;

public class EmailTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public EmailTest() {
	  super( SOLUTION_PATH );
  }
  
  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }
  }

    public void testEmailLoop() {
        SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
        OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_only_email-loop", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
        SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
        IRuntimeContext context = run(SOLUTION_PATH+ "/test/email/" , "text_only_email-loop.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    }

    public void testEmailOnly() {
        SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
        OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_only_email", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
        SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);        
        IRuntimeContext context = run(SOLUTION_PATH+ "/test/email/" , "text_only_email.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    }

    public void testEmailLoopProp() {
        SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
        OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_only_email-loop_property", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
        SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);        
        IRuntimeContext context = run(SOLUTION_PATH+ "/test/email/" , "text_only_email-loop_property.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    }

    public void testEmailHtml() {
        SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
        OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_html_attach_email", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
        SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);        
        IRuntimeContext context = run(SOLUTION_PATH+ "/test/email/" , "text_html_attach_email.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    }

    public static void main(String[] args) {
        EmailTest test = new EmailTest();
        try {
            test.testEmailLoop();
            test.testEmailOnly();
            test.testEmailLoopProp();
            test.testEmailHtml();
        } finally {
        }
    }
}
