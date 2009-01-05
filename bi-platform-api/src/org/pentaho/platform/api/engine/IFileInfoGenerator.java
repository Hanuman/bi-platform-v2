/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008-2009 Pentaho Corporation.  All rights reserved.
 *
 */
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
