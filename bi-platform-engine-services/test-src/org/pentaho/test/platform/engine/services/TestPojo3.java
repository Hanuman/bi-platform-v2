package org.pentaho.test.platform.engine.services;


import org.pentaho.platform.api.engine.ISimplePojoComponent;

public class TestPojo3 implements ISimplePojoComponent {

	public boolean execute() throws Exception {

		// we should not get here
		PojoComponentTest.executeCalled = true;
		
		return true;
	}
	
	public boolean validate() throws Exception {
		PojoComponentTest.validateCalled = true;
		return false;
	}

}
