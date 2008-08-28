package org.pentaho.test.platform.web;


import org.pentaho.test.platform.engine.core.BaseTest;


public class UIUtilTest extends BaseTest {
/*
  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "mondrian", "mondrian" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }
  
  public void testProcessTemplate() {
    startTest();
    UIUtil utility = new UIUtil(); 
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    String title = "TemplateTitle"; //$NON-NLS-1$
    String inputTemplate = ""; //$NON-NLS-1$ //TODO what is a template. how does it get used in the composite application
    String template = utility.processTemplate(inputTemplate, title, session);
    System.out.println("Output Template : " +template); //$NON-NLS-1$
    info("Expected: Successful processing of a template using the input template"); //$NON-NLS-1$

    assertTrue(true);

    finishTest();    
  }
  
  public void testProcessTemplate2() {
    startTest();
    UIUtil utility = new UIUtil(); 
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    String title = "TemplateTitle"; //$NON-NLS-1$
    
    String inputTemplate = ""; //$NON-NLS-1$ //TODO what is a template. how does it get used in the composite application
    String content = " "; //$NON-NLS-1$ //TODO what is a content used for 
    String template = utility.processTemplate(inputTemplate, title, content,session);
    
    System.out.println("Output Template : " +template); //$NON-NLS-1$
    info("Expected: Successful processing of a template using the input template and a content"); //$NON-NLS-1$
    utility.getTemplate("My Template", session); //$NON-NLS-1$

    
    assertTrue(true);

    finishTest();    
  }

  public void testFormatSuccessMessage() {
    startTest();
 
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "chart_type", "line"); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream("Chart_Line", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run("test", "charts", "ChartComponent_ChartTypes.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    UIUtil.formatSuccessMessage("text/html", context, new StringBuffer(), true); //$NON-NLS-1$
    
    assertTrue(true);

    finishTest();    
  }  
  public static void main(String[] args) {
    UIUtilTest test = new UIUtilTest();
    try {
      test.setUp();
      test.testProcessTemplate();
      test.testFormatSuccessMessage();
      test.testProcessTemplate2();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }*/
}
