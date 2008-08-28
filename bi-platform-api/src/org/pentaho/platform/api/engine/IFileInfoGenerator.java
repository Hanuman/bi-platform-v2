package org.pentaho.platform.api.engine;

import java.io.InputStream;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.ILogger;

public interface IFileInfoGenerator {

	public void setLogger( ILogger logger );
	
	public enum ContentType { INPUTSTREAM, DOM4JDOC, BYTES, STRING };
	
	public ContentType getContentType();
	
	public IFileInfo getFileInfo( String solution, String path, String filename, InputStream in );
	
	public IFileInfo getFileInfo( String solution, String path, String filename, Document in );
	
	public IFileInfo getFileInfo( String solution, String path, String filename, byte bytes[] );
	
	public IFileInfo getFileInfo( String solution, String path, String filename, String str );
	
}
