package org.pentaho.platform.api.engine;

import java.io.OutputStream;

public interface IStreamingPojoComponent extends ISimplePojoComponent {

	public void setOutputStream( OutputStream outputStream );
	
	public String getMimeType( );
	
}
