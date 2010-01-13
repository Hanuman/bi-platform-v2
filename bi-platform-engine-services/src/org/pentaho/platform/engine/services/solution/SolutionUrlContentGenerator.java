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

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.web.MimeHelper;

public class SolutionUrlContentGenerator extends BaseContentGenerator {

  private static final long serialVersionUID = 8445693289282403228L;

  public static final int TYPE_UNKNOWN = 0;
  
  public static final int TYPE_STATIC = 1;
  
  public static final int TYPE_PLUGIN = 2;
  
  @Override
  public void createContent() throws Exception {
    OutputStream out = null;
    if( outputHandler == null ) {
      error( Messages.getInstance().getErrorString("SimpleContentGenerator.ERROR_0001_NO_OUTPUT_HANDLER") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getInstance().getString("SimpleContentGenerator.ERROR_0001_NO_OUTPUT_HANDLER") );  //$NON-NLS-1$
    }

    IParameterProvider params = parameterProviders.get( "path" ); //$NON-NLS-1$
    
    String urlPath = params.getStringParameter("path", null); //$NON-NLS-1$

    ActionInfo pathInfo = ActionInfo.parseActionString(urlPath);
    
    if( pathInfo == null ) {
      // there is no path so we don't know what to return
      error( Messages.getInstance().getErrorString("SolutionURLContentGenerator.ERROR_0001_NO_FILEPATH") ); //$NON-NLS-1$
      return;
    }
    
    if( PentahoSystem.debug ) debug( "SolutionResourceContentGenerator urlPath="+urlPath); //$NON-NLS-1$
    int type = TYPE_UNKNOWN;

    // work out what this thing is
    String filename = pathInfo.getActionName();
    String extension = ""; //$NON-NLS-1$
    int index = filename.lastIndexOf('.');
    if (index != -1) {
      extension = filename.substring(index+1);
    }
    
    // is this a plugin file type?
    if( type == TYPE_UNKNOWN ) {
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, userSession );
      if( pluginManager != null ) {
        IContentGenerator contentGenerator = pluginManager.getContentGeneratorForType(extension, userSession);
        if( contentGenerator != null ) {
          // set up the path parameters
          IParameterProvider requestParams = parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
          if( requestParams instanceof SimpleParameterProvider ) {
            ((SimpleParameterProvider) requestParams).setParameter("solution", pathInfo.getSolutionName()); //$NON-NLS-1$
            ((SimpleParameterProvider) requestParams).setParameter("path", pathInfo.getPath()); //$NON-NLS-1$
            ((SimpleParameterProvider) requestParams).setParameter("name", pathInfo.getActionName()); //$NON-NLS-1$
            ((SimpleParameterProvider) requestParams).setParameter("action", pathInfo.getActionName()); //$NON-NLS-1$
          }
          // delegate over to the content generator for this file type
          contentGenerator.setCallbacks( callbacks );
          contentGenerator.setInstanceId( instanceId );
          contentGenerator.setItemName( itemName );
          contentGenerator.setLoggingLevel( loggingLevel );
          contentGenerator.setMessagesList( messages );
          contentGenerator.setOutputHandler( outputHandler );
          contentGenerator.setParameterProviders( parameterProviders );
          contentGenerator.setSession( userSession );
          contentGenerator.setUrlFactory( urlFactory );
          contentGenerator.createContent();
          return;
        }
      }
    }

    // get the mime-type
    String mimeType = MimeHelper.getMimeTypeFromFileName(filename);
    if( mimeType != null && mimeType.equals( MimeHelper.MIMETYPE_XACTION ) ) {
      mimeType = null;
    }
    
    // is this a static file type?
    if( urlPath.contains( "/web/" ) && mimeType != null ) { //$NON-NLS-1$
      // this is a static file type
      type = TYPE_STATIC;
    }

    if( type == TYPE_UNKNOWN ) {
      // should not handle this file type
      warn( Messages.getInstance().getErrorString("SolutionURLContentGenerator.ERROR_0002_CANNOT_HANDLE_TYPE", urlPath ) ); //$NON-NLS-1$
      return;
    }
    IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", "", instanceId, mimeType ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    if( contentItem == null ) {
      error( Messages.getInstance().getErrorString("SimpleContentGenerator.ERROR_0002_NO_CONTENT_ITEM") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getInstance().getString("SimpleContentGenerator.ERROR_0002_NO_CONTENT_ITEM") );  //$NON-NLS-1$
    }
    
    contentItem.setMimeType( mimeType );
    
    out = contentItem.getOutputStream( itemName );
    if( out == null ) {
      error( Messages.getInstance().getErrorString("SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getInstance().getString("SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM") );  //$NON-NLS-1$
    }
    
    // TODO support cache control settings
    
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, userSession);
    InputStream in = repo.getResourceInputStream(urlPath, false, ISolutionRepository.ACTION_EXECUTE);
    if( in == null ) {
      error( Messages.getInstance().getErrorString("SolutionURLContentGenerator.ERROR_0003_RESOURCE_NOT_FOUND", urlPath ) ); //$NON-NLS-1$
      return;
    }
    
    try {
      byte buffer[] = new byte[4096];
      int n = in.read(buffer);
      while( n != -1 ) {
        out.write(buffer, 0, n);
        n = in.read(buffer);
      }
    } finally {
      out.close();
    }
    
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(SolutionUrlContentGenerator.class);
  }


}
