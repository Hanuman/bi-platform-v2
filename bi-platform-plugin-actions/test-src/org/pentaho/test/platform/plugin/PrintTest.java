package org.pentaho.test.platform.plugin;


import java.io.File;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

public class PrintTest extends BaseTest {
  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";
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
    public void testPrinting1() {
        startTest();
        IRuntimeContext context = run("test", "printing", "PrintTest1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
        finishTest();
    }

  public void testPrinting_NoReportOutput() {
      startTest();
      IRuntimeContext context = run("test", "printing", "PrintTest_NoReportOutput.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
      finishTest();
  }

  public void testPrinting_NoPrinterName() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_NoPrinterName.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }  
  
  public void testPrinting_NoPrintFile() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_NoPrintFile.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }
  
  public void testPrinting_PrintFileAsInput() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_PrintFileAsInput.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testPrinting_FakeFileName() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_FakeFileName.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }  
    public static void main(String[] args) {
        PrintTest test = new PrintTest();
        test.setUp();
        try {
            test.testPrinting1();
            test.testPrinting_NoPrinterName();
            test.testPrinting_NoPrintFile();
            test.testPrinting_NoReportOutput();
            test.testPrinting_PrintFileAsInput();
            test.testPrinting_FakeFileName();
            
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }

    }
}
