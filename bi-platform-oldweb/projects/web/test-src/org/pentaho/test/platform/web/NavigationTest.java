package org.pentaho.test.platform.web;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.dom4j.Document;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.uifoundation.component.xml.NavigationComponent;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.test.platform.engine.core.BaseTest;

public class NavigationTest extends BaseTest {
  private static final String SOLUTION_PATH = "projects/web/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

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
    public void testRootLevel() {
        startTest();
        getSolutionDir("NavigationTest.testRootLevel", null, null); //$NON-NLS-1$
        compare( "NavigationTest.testRootLevel", ".xml" ); //$NON-NLS-1$ //$NON-NLS-2$
        finishTest();
    }

    public void testSolutionRootLevel() {
        startTest();
        getSolutionDir("NavigationTest.testSolutionRootLevel", "samples", null); //$NON-NLS-1$ //$NON-NLS-2$
        compare( "NavigationTest.testSolutionRootLevel", ".xml" ); //$NON-NLS-1$ //$NON-NLS-2$
        finishTest();
    }

    public void testSolutionFolder() {
        startTest();
        getSolutionDir("NavigationTest.testSolutionFolder", "samples", "charts"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        compare( "NavigationTest.testSolutionFolder", ".xml" ); //$NON-NLS-1$ //$NON-NLS-2$
        finishTest();
    }

    public void getSolutionDir(String testName, String solution, String path) {
        String hrefUrl = ""; //$NON-NLS-1$
        String onClick = ""; //$NON-NLS-1$
        String solutionParameterName = solution;
        String pathParameterName = path;
        String options = ""; //$NON-NLS-1$
        SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
        ArrayList messages = new ArrayList();

        NavigationComponent component = new NavigationComponent();
        component.setHrefUrl(hrefUrl);
        component.setOnClick(onClick);
        component.setSolutionParamName(solutionParameterName);
        component.setPathParamName(pathParameterName);
        component.setAllowNavigation(new Boolean(true));
        component.setOptions(options);
        component.setUrlFactory(urlFactory);
        component.setMessages(messages);
        component.setLoggingLevel(getLoggingLevel());
        
        OutputStream outputStream = getOutputStream(testName, ".xml"); //$NON-NLS-1$

        SimpleParameterProvider requestParameters = new SimpleParameterProvider();
        SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

        if (solution != null) {
            requestParameters.setParameter("solution", solution); //$NON-NLS-1$
        }
        if (path != null) {
            requestParameters.setParameter("path", path); //$NON-NLS-1$
        }

        component.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters); 
        component.setParameterProvider(HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters); 
        StandaloneSession session = new StandaloneSession("BaseTest.DEBUG_JUNIT_SESSION"); //$NON-NLS-1$

        try {
            component.validate(session, null);
            Document doc = component.getXmlContent();
            if( doc != null ) {
                outputStream.write(doc.asXML().getBytes(LocaleHelper.getSystemEncoding()));
            }
        } catch (IOException e) {
            error(e.getLocalizedMessage(), e);
        }
    }

    public static void main(String[] args) {
        NavigationTest test = new NavigationTest();
        test.setUp();
        try {
            test.testRootLevel();
            test.testSolutionRootLevel();
            test.testSolutionFolder();
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }
    }
    

}
