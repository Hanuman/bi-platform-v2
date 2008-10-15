package org.pentaho.platform.uifoundation.contentgen;

import java.io.OutputStream;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.uifoundation.messages.Messages;

public abstract class BaseXmlContentGenerator extends BaseContentGenerator {

	private static final long serialVersionUID = 2272261269875005948L;

	protected String baseUrl;
	
	protected IParameterProvider requestParameters;
	
	protected IParameterProvider sessionParameters;
	
	protected abstract String getContent() throws Exception;
	
	@Override
	public void createContent() throws Exception {

		baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

	    requestParameters = this.parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
	    sessionParameters = this.parameterProviders.get( IParameterProvider.SCOPE_SESSION );

		String content = getContent();

		if( content == null ) {
			StringBuffer buffer = new StringBuffer();
			PentahoSystem.getMessageFormatter(userSession).formatErrorMessage( "text/html", Messages.getErrorString( "UI.ERROR_0001_CONTENT_ERROR" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
			content = buffer.toString();
		}

		String intro = "";
		String footer = "";
		IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
		if( templater != null ) {
			String sections[] = templater.breakTemplate( "template.html", "", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
			if( sections != null && sections.length > 0 ) {
				intro = sections[0];
			}
			if( sections != null && sections.length > 1 ) {
				footer = sections[1];
			}
		} else {
			intro = Messages.getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
		}
        IContentItem contentItem = outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null, "text/html" );//$NON-NLS-1$
        OutputStream outputStream = contentItem.getOutputStream(null);
        outputStream.write( intro.getBytes() );
        outputStream.write( content.getBytes() );
        outputStream.write( footer.getBytes() );
	}


}
