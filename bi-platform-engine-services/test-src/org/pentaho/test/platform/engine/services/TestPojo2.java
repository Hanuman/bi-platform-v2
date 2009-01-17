package org.pentaho.test.platform.engine.services;


import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IStreamingPojo;

@SuppressWarnings({"all"})
public class TestPojo2 implements IStreamingPojo {

	protected OutputStream outputStream;
	protected String input1;
	
	public boolean execute() throws Exception {

		// this will generate a null pointer if input1 is null
		String output = input1+input1;

		// this will generate an exception is outputStream is null
		outputStream.write( output.getBytes() );
		outputStream.close();
		
		return true;
	}
	
	public void setInput1( String input1 ) {
		this.input1 = input1;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public String getMimeType( ){
		return "text/text";
	}

	public boolean validate() throws Exception {
		return true;
	}

  public void setResource1( InputStream stream ) {
    PojoComponentTest.setResourceInputStreamCalled = true;
  }
  
	public void setResource2( IActionSequenceResource resource ) {
    PojoComponentTest.setActionSequenceResourceCalled = true;
	}
	
}
