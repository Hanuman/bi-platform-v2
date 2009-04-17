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

import java.io.OutputStream;
import java.security.InvalidParameterException;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.messages.Messages;

public abstract class SimpleContentGenerator extends BaseContentGenerator {

  @Override
  public void createContent() throws Exception {
    OutputStream out = null;
    if( outputHandler == null ) {
      error( Messages.getErrorString("SimpleContentGenerator.ERROR_0001_NO_OUTPUT_HANDLER") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getString("SimpleContentGenerator.ERROR_0001_NO_OUTPUT_HANDLER") );  //$NON-NLS-1$
    }

    IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", "", instanceId, getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    if( contentItem == null ) {
      error( Messages.getErrorString("SimpleContentGenerator.ERROR_0002_NO_CONTENT_ITEM") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getString("SimpleContentGenerator.ERROR_0002_NO_CONTENT_ITEM") );  //$NON-NLS-1$
    }
    
    contentItem.setMimeType( getMimeType() );
    
    out = contentItem.getOutputStream( itemName );
    if( out == null ) {
      error( Messages.getErrorString("SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getString("SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM") );  //$NON-NLS-1$
    }
    
    createContent( out );
  }

  public abstract void createContent( OutputStream out ) throws Exception;

  public abstract String getMimeType();
}
