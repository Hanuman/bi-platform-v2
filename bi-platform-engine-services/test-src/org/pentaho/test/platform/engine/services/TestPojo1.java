package org.pentaho.test.platform.engine.services;


import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISimplePojoComponent;

public class TestPojo1 implements ISimplePojoComponent {

	protected String input1;
	protected String output1;
	
	public boolean execute() throws Exception {

		// this will generate a null pointer if input1 is null
		output1 = input1+input1;
		
		return true;
	}
	
	public void setLogger( Log log ) {
    PojoComponentTest.setLoggerCalled = true;
	}
	
	public void setSession( IPentahoSession session ) {
	  PojoComponentTest.setSessionCalled = true;
	}
	
	public void setRuntimeContext( IRuntimeContext context) {
    PojoComponentTest.setRuntimeContextCalled = true;
	}
	
	public String getOutput1() {
		return output1;
	}
	
	public void setInput1( String input1 ) {
		this.input1 = input1;
	}
	
	public boolean validate() throws Exception {
		return true;
	}

	public boolean done() {
		PojoComponentTest.doneCalled = true;
		return true;
	}
	
}
