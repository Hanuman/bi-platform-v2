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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.InputStream;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.ILogger;

public class FileInfoGenerator implements IFileInfoGenerator {

	public ContentType getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			InputStream in) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			Document in) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			String str) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLogger(ILogger logger) {
		// TODO Auto-generated method stub

	}

}
