package org.pentaho.test.platform.engine.services;


import org.pentaho.platform.api.engine.ISimplePojoComponent;

public class TestPojo1 implements ISimplePojoComponent {

	protected String input1;
	protected String output1;
	
	public boolean execute() throws Exception {

		// this will generate a null pointer if input1 is null
		output1 = input1+input1;
		
		return true;
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
