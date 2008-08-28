package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.MultiContentItem;
import org.pentaho.platform.engine.core.output.MultiOutputStream;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.ExceptionOutputStream;

public class MultiOutputTest extends BaseTest {


	public static final String SOLUTION_PATH = "projects/services/test-src/solution";
	  private static final String ALT_SOLUTION_PATH = "test-src/solution";
	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
	  final String SYSTEM_FOLDER = "/system";
	  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

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
		  
	public void testMultiOutput() {
		
	    startTest();
        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
        try {
            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "services/MultiOutputTest.xaction" );
            PojoComponentTest.doneCalled = false;
            IRuntimeContext runtimeContext = solutionEngine.execute( 
            		xactionStr, "test1a.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
        	// we should not get here
        	e.printStackTrace();
        	assertTrue( e.getMessage(), false );
        }
        finishTest();

	}
	
	public void testMultiContentItem() {

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		SimpleContentItem item1 = new SimpleContentItem( out1 );
		SimpleContentItem item2 = new SimpleContentItem( out2 );
		
		MultiContentItem multiContent = new MultiContentItem();
		multiContent.addContentItem( item1 );
		multiContent.addContentItem( item2 );

		byte in[] = "abcd".getBytes();
		String outStr1 = "";
		String outStr2 = "";
		
		try {
			OutputStream multi = multiContent.getOutputStream( "" );
			multi.write( 'a' );
			multi.write( in, 1, 2 );
			multi.write( in );
			multi.close();
		} catch (IOException e) {
			// we should not get here
			assertEquals( "IOException", null, e );
		}
		outStr1 = new String( out1.toByteArray() );
		outStr2 = new String( out2.toByteArray() );
		
		assertEquals( "Output stream 1", "abcabcd", outStr1 );
		assertEquals( "Output stream 2", "abcabcd", outStr2 );
		
	}
	
	public void testMultiStream() {
		
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		ByteArrayOutputStream outs[] = new ByteArrayOutputStream[] { out1, out2 };
		
		MultiOutputStream multi = new MultiOutputStream( outs );
		byte in[] = "abcd".getBytes();
		String outStr1 = "";
		String outStr2 = "";
		
		try {
			multi.write( 'a' );
			multi.write( in, 1, 2 );
			multi.write( in );
			multi.close();
		} catch (IOException e) {
			// we should not get here
			assertEquals( "IOException", null, e );
		}
		outStr1 = new String( out1.toByteArray() );
		outStr2 = new String( out2.toByteArray() );
		
		assertEquals( "Output stream 1", "abcabcd", outStr1 );
		assertEquals( "Output stream 2", "abcabcd", outStr2 );
	}
	
	public void testMultiStreamErrors() {
		
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		ExceptionOutputStream out2 = new ExceptionOutputStream();
		ByteArrayOutputStream out3 = new ByteArrayOutputStream();
		
		OutputStream outs[] = new OutputStream[] { out1, out2, out3 };
		
		MultiOutputStream multi = new MultiOutputStream( outs );
		byte in[] = "abcd".getBytes();
		String outStr1 = "";
		String outStr2 = "";
		
		try {
			multi.write( 'a' );
		} catch (IOException e) {
			// we expect to get here
			assertEquals( "IOException", "Test Exception", e.getMessage() );
		}
		try {
			multi.write( in, 1, 2 );
		} catch (IOException e) {
			// we expect to get here
			assertEquals( "IOException", "Test Exception", e.getMessage() );
		}
		try {
			multi.write( in );
		} catch (IOException e) {
			// we expect to get here
			assertEquals( "IOException", "Test Exception", e.getMessage() );
		}
		try {
			multi.close();
		} catch (IOException e) {
			// we expect to get here
			assertEquals( "IOException", "Test Exception", e.getMessage() );
		}

		outStr1 = new String( out1.toByteArray() );
		outStr2 = new String( out3.toByteArray() );
		
		assertEquals( "Output stream 1", "abcabcd", outStr1 );
		assertEquals( "Output stream 2", "abcabcd", outStr2 );
	}
	
}
