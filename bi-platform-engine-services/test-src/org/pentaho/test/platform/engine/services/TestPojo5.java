package org.pentaho.test.platform.engine.services;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IAcceptsRuntimeInputs;
import org.pentaho.platform.api.engine.IProducesRuntimeOutputs;

public class TestPojo5 implements IAcceptsRuntimeInputs, IProducesRuntimeOutputs {

	private String input1;
	private String input2;
	private Map<String,Object> outputs = new HashMap<String,Object>();
	
	public boolean execute() {
		outputs.put("output1", input1 );
		outputs.put("output2", input2 );
		return true;
	}
	
	public void setInputs( Map<String,Object> inputs ) {
		input1 = (String) inputs.get( "INPUT1" );
		input2 = (String) inputs.get( "INPUT2" );
	}
	
	public Map<String,Object> getOutputs() {
		return outputs;
	}
	
}
