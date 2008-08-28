package org.pentaho.platform.plugin.action.builtin;

import java.io.OutputStream;

public interface IStreamingPojo {

	public void setOutputStream( OutputStream outputStream );
	
	public String getMimeType( );
	
}
