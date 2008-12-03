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
    
    out = contentItem.getOutputStream( null );
    if( out == null ) {
      error( Messages.getErrorString("SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM") ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getString("SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM") );  //$NON-NLS-1$
    }
    
    createContent( out );
  }

  public abstract void createContent( OutputStream out ) throws Exception;

  public abstract String getMimeType();
}
