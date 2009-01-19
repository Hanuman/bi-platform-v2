/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings({"all"})
public class PojoComponentTest extends BaseTest {
	  private static final String SOLUTION_PATH = "test-src/solution";
	  public String getSolutionPath() {
	       return SOLUTION_PATH;  
	  }

	  public static boolean doneCalled = false;
	  public static boolean executeCalled = false;
    public static boolean validateCalled = false;
    public static boolean setSessionCalled = false;
    public static boolean setRuntimeContextCalled = false;
    public static boolean setLoggerCalled = false;
    public static boolean setResourceInputStreamCalled = false;
    public static boolean setActionSequenceResourceCalled = false;
	  
	  public void testSimplePojoInput() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo1a.xaction" );
	            PojoComponentTest.doneCalled = false;
	            PojoComponentTest.setSessionCalled = false;
              PojoComponentTest.setRuntimeContextCalled = false;
	            PojoComponentTest.setLoggerCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test1a.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	          IActionParameter param = runtimeContext.getOutputParameter( "output1" );
	            assertNotNull( "RuntimeContext is null", runtimeContext );
              assertTrue( "done() was not called", PojoComponentTest.doneCalled );
              assertTrue( "setSession() was not called", PojoComponentTest.setSessionCalled );
              assertTrue( "setRuntimeContext() was not called", PojoComponentTest.setRuntimeContextCalled );
              assertTrue( "setLogger() was not called", PojoComponentTest.setLoggerCalled );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testSimplePojoSettings() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo1b.xaction" );
	            PojoComponentTest.doneCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "pojo1b.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	          IActionParameter param = runtimeContext.getOutputParameter( "output1" );
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertTrue( "done() was not called", PojoComponentTest.doneCalled );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testSimplestCase() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo4.xaction" );
	            PojoComponentTest.doneCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "pojo4.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	          IActionParameter param = runtimeContext.getOutputParameter( "output1" );
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertNotNull( "param is null", param );
	            assertEquals( "abcdeabcde", param.getValue().toString() );
	            assertEquals( "done() was called", false, PojoComponentTest.doneCalled );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testRuntimeInputsAndOutputs() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo5.xaction" );
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "pojo5.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	          IActionParameter param = runtimeContext.getOutputParameter( "output1" );
	            assertNotNull( "param is null", param );
	            assertEquals( "hello", param.getValue().toString() );
	          param = runtimeContext.getOutputParameter( "output2" );
	            assertNotNull( "param is null", param );
	            assertEquals( "world", param.getValue().toString() );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testMissingClassSetting() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad1.xaction" );
	            PojoComponentTest.doneCalled = false;
	            PojoComponentTest.executeCalled = false;
	            PojoComponentTest.validateCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "execute was called", false, PojoComponentTest.executeCalled );
	            assertEquals( "validate was called", false, PojoComponentTest.validateCalled );
	            assertEquals( "Action sequence validation succeeded", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testBadClassSetting() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad2.xaction" );
	            PojoComponentTest.doneCalled = false;
	            PojoComponentTest.executeCalled = false;
	            PojoComponentTest.validateCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "execute was called", false, PojoComponentTest.executeCalled );
	            assertEquals( "validate was called", false, PojoComponentTest.validateCalled );
	            assertEquals( "Action sequence validation succeeded", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testBadValidate() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad3.xaction" );
	            PojoComponentTest.doneCalled = false;
	            PojoComponentTest.executeCalled = false;
	            PojoComponentTest.validateCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "execute was called", false, PojoComponentTest.executeCalled );
	            assertEquals( "validate was not called", true, PojoComponentTest.validateCalled );
	            assertEquals( "Action sequence execution succeeded", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testBadInput() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad4.xaction" );
	            PojoComponentTest.doneCalled = false;
	            PojoComponentTest.executeCalled = false;
	            PojoComponentTest.validateCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "Action sequence succeeded", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testBadOutput() {
	      startTest();
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad5.xaction" );
	            PojoComponentTest.doneCalled = false;
	            PojoComponentTest.executeCalled = false;
	            PojoComponentTest.validateCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "Action sequence succeeded", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }
	        finishTest();
	  }

	  public void testStreamingPojo() {
	      String instanceId = null;
	      ByteArrayOutputStream out = new ByteArrayOutputStream();
	        SimpleOutputHandler outputHandler = new SimpleOutputHandler(out, false);
	        outputHandler.setOutputPreference(IOutputHandler.OUTPUT_TYPE_DEFAULT);

	        startTest();
	        IPentahoSession session = new StandaloneSession("system");
	        ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
	        if (outputHandler != null) {
	          outputHandler.setSession(session);
	        }
	        try {
	            String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo2.xaction" );
	            PojoComponentTest.setActionSequenceResourceCalled = false;
	            IRuntimeContext runtimeContext = solutionEngine.execute( 
	                xactionStr, "test1.xaction", "empty action sequence test", false, true, null, false, new HashMap(), outputHandler, null, new SimpleUrlFactory(""), new ArrayList()); //$NON-NLS-1$ //$NON-NLS-2$
	          IActionParameter param = runtimeContext.getOutputParameter( "outputstream" );
	            assertNotNull( "RuntimeContext is null", runtimeContext );
	            assertEquals( "Action sequence execution failed", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS );
	            assertTrue( "setResource was not called", PojoComponentTest.setResourceInputStreamCalled );
	            assertTrue( "setResource was not called", PojoComponentTest.setActionSequenceResourceCalled );
	        } catch (Exception e) {
	          // we should not get here
	          e.printStackTrace();
	          assertTrue( e.getMessage(), false );
	        }

	      String output = new String( out.toByteArray() );
	      assertEquals( "outputstream", "abcdeabcde", output );
	      finishTest();
	  }
	  
	}

