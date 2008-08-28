package org.pentaho.test.platform.web;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.request.HttpWebServiceRequestHandler;
import org.pentaho.test.platform.engine.core.BaseTest;


public class SoapUtilTest extends BaseTest {

public void testSoapUtil() {
      startTest();

      SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
      parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
      UserSession session = new UserSession("Joe", Locale.US, true, parameterProvider);    //$NON-NLS-1$

      OutputStream contentStream = new ByteArrayOutputStream();
      SimpleOutputHandler outputHandler = new SimpleOutputHandler(contentStream, false);
      OutputStream outputStream = getOutputStream( "SoapTest", ".txt" ); //$NON-NLS-1$ //$NON-NLS-2$

      HttpWebServiceRequestHandler requestHandler = new HttpWebServiceRequestHandler(session, null, outputHandler, parameterProvider, null);


      IRuntimeContext runtime = null;
      IRuntimeContext runtime1 = null;
      try {      
        outputStream.write(SoapHelper.getSoapHeader().getBytes(LocaleHelper.getSystemEncoding()));

        runtime = run("test", "rules", "JavaScriptResultSetTest.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SoapHelper.openSoapResponse();
        outputStream.write(SoapHelper.getSoapHeader().getBytes(LocaleHelper.getSystemEncoding()));        
        SoapHelper.generateSoapResponse(runtime, outputStream, outputHandler, contentStream, requestHandler.getMessages());
        runtime1 = run("test", "dashboard", "departments.rule.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SoapHelper.generateSoapResponse(runtime1, outputStream, outputHandler, contentStream, requestHandler.getMessages());        
        SoapHelper.generateSoapError(new StringBuffer(), requestHandler.getMessages());
        SoapHelper.generateSoapResponse(runtime, outputHandler, contentStream, new StringBuffer(), requestHandler.getMessages());
        outputStream.write(SoapHelper.getSoapFooter().getBytes(LocaleHelper.getSystemEncoding()));
        SoapHelper.closeSoapResponse();
        
        
      } catch(Exception e) {
        e.printStackTrace();
      } finally {
          if (runtime != null) {
              runtime.dispose();
          }
      }
      assertTrue(true); 
      finishTest();
  }

 
    public static void main(String[] args) {
      SoapUtilTest test = new SoapUtilTest();
        test.setUp();
        try {
          test.testSoapUtil();
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }
    }

}
