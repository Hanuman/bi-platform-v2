/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services.solution;

import java.io.File;
import java.io.InputStream;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class ActionSequenceFileInfo implements IFileInfoGenerator {

	ILogger logger;
	
	public void setLogger( ILogger logger ) {
		this.logger = logger;
	}
	
	public ContentType getContentType() {
		return ContentType.DOM4JDOC;
	}
	
	public IFileInfo getFileInfo( String solution, String path, String filename, InputStream in ) {
		return null;
	}
	
	public IFileInfo getFileInfo( String solution, String path, String filename, Document actionSequenceDocument ) {
	    if (actionSequenceDocument == null) {
	      return null;
	    }

	    IActionSequence actionSequence = SequenceDefinition.ActionSequenceFactory(actionSequenceDocument, filename, path,
	    		solution, logger, PentahoSystem.getApplicationContext(), Logger.getLogLevel() );
	    if (actionSequence == null) {
	      Logger.error(getClass().toString(), Messages.getErrorString("SolutionRepository.ERROR_0016_FAILED_TO_CREATE_ACTION_SEQUENCE",  //$NON-NLS-1$
	    		  solution + File.separator + path + File.separator + filename));
	      return null;
	    }

	    IFileInfo info = new FileInfo();
	    info.setAuthor(actionSequence.getAuthor());
	    info.setDescription(actionSequence.getDescription());
	    info.setDisplayType(actionSequence.getResultType());
	    info.setIcon(actionSequence.getIcon());
	    info.setTitle(actionSequence.getTitle());
		return info;
	}
	
	public IFileInfo getFileInfo( String solution, String path, String filename, byte bytes[] ) {
		return null;
	}
	
	public IFileInfo getFileInfo( String solution, String path, String filename, String str ) {
		return null;
	}

}
