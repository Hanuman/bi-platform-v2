package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.pentaho.commons.connection.SimpleStreamSource;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;

public class GenericServlet extends ServletBase {

	private static final long serialVersionUID = 6713118348911206464L;
	
	private static final Log logger = LogFactory.getLog(GenericServlet.class);
	
	  @Override
	  public Log getLogger() {
	    return GenericServlet.logger;
	  }

	  @Override
	  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		  doGet( request, response );
	  }

	  @Override
	  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
	    
	    PentahoSystem.systemEntryPoint();

	    try {
	      InputStream in = request.getInputStream();
	    	String servletPath = request.getServletPath();
	    	String key = request.getPathInfo();
	    	String contentGeneratorId = ""; //$NON-NLS-1$
	    	String urlPath = ""; //$NON-NLS-1$
	    	SimpleParameterProvider pathParams = new SimpleParameterProvider();
	    	if( key == null ) {
	    		contentGeneratorId = servletPath.substring(1);
	    		urlPath = contentGeneratorId;
	    	} else {
	    		String path = key.substring( 1 );
	    		int slashPos = path.indexOf( '/' );
	    		if( slashPos != -1 ) {
	    			pathParams.setParameter( "path" , key.substring( slashPos+1 ) ); //$NON-NLS-1$
			    	contentGeneratorId = path.substring( 0, slashPos );
	    		} else {
			    	contentGeneratorId = path;
	    		}
		    	urlPath = "content/"+contentGeneratorId; //$NON-NLS-1$
	    	}
        pathParams.setParameter( "query" , request.getQueryString() ); //$NON-NLS-1$
        pathParams.setParameter( "contentType", request.getContentType() ); //$NON-NLS-1$
        pathParams.setParameter( "inputstream" , in ); //$NON-NLS-1$
        pathParams.setParameter( "httpresponse", response ); //$NON-NLS-1$
        pathParams.setParameter( "httprequest", request ); //$NON-NLS-1$
	    	if( PentahoSystem.debug ) debug( "GenericServlet contentGeneratorId="+contentGeneratorId ); //$NON-NLS-1$
	    	if( PentahoSystem.debug ) debug( "GenericServlet urlPath="+urlPath ); //$NON-NLS-1$
	    	IPentahoSession session = getPentahoSession( request );
	    	IPluginSettings pluginSettings = PentahoSystem.get( IPluginSettings.class, session );
		    if( pluginSettings == null ) {
		    	OutputStream out = response.getOutputStream();
		    	String message = "Could not get system object: PluginSettings";
		    	error( message );
		    	out.write( message.getBytes() );
		    	return;
		    }

		    // TODO make doing the HTTP headers configurable per content generator
        SimpleParameterProvider headerParams = new SimpleParameterProvider();
		    Enumeration names = request.getHeaderNames();
		    while( names.hasMoreElements() ) {
		      String name = (String) names.nextElement();
		      String value = request.getHeader( name );
		      headerParams.setParameter(name, value);
		    }
		    
		    IContentGenerator contentGenerator = pluginSettings.getContentGenerator(contentGeneratorId, session);
	    	if( contentGenerator == null ) {
		    	OutputStream out = response.getOutputStream();
	    		String message = "Could not get content generator for type: "+contentGeneratorId;
		    	error( message );
	    		out.write( message.getBytes() );
	    		return;
	    	}

	    	// set the classloader of the current thread to the class loader of 
	    	// the plugin so that it can load its libraries
	      Thread.currentThread().setContextClassLoader( contentGenerator.getClass().getClassLoader() );

//		      String proxyClass = PentahoSystem.getSystemSetting( module+"/plugin.xml" , "plugin/content-generators/"+contentGeneratorId, "content generator not found");
	    	IParameterProvider requestParameters = new HttpRequestParameterProvider( request );
	    	// see if this is an upload
	    	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	    	if( isMultipart ) {
	    		requestParameters = new SimpleParameterProvider();
	    		// Create a factory for disk-based file items
	    		FileItemFactory factory = new DiskFileItemFactory();

	    		// Create a new file upload handler
	    		ServletFileUpload upload = new ServletFileUpload(factory);

	    		// Parse the request
	    		List<?> /* FileItem */ items = upload.parseRequest(request);
	    		Iterator<?> iter = items.iterator();
	    		while (iter.hasNext()) {
	    			FileItem item = (FileItem) iter.next();

	    			if (item.isFormField()) {
	    				((SimpleParameterProvider)requestParameters).setParameter( item.getFieldName() , item.getString() );
	    			} else {
	    				String name = item.getName();
	    				((SimpleParameterProvider)requestParameters).setParameter( name , item.getInputStream() );
	    			}
	    		}	
	    	}

//	    	IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );
//	    	IContentGenerator contentGenerator = getContentGenerator( proxyClass, userSession );
	    	
	    	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

	        IMimeTypeListener listener = new HttpMimeTypeListener(request, response);
	    	
	    	OutputStream out = response.getOutputStream();
	    	HttpOutputHandler outputHandler = new HttpOutputHandler( response, out, true );
	        outputHandler.setMimeTypeListener(listener);
	    	
	    	IParameterProvider sessionParameters = new HttpSessionParameterProvider( session );
	    	
	    	
	    	Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
	    	parameterProviders.put( IParameterProvider.SCOPE_REQUEST , requestParameters );
        parameterProviders.put( IParameterProvider.SCOPE_SESSION , sessionParameters );
        parameterProviders.put( "headers" , headerParams ); //$NON-NLS-1$
	    	parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
	        SimpleUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl()
	                + urlPath+"?"); //$NON-NLS-1$
	    	List<String> messages = new ArrayList<String>();
	    	contentGenerator.setOutputHandler(outputHandler);
	    	contentGenerator.setMessagesList(messages);
	    	contentGenerator.setParameterProviders(parameterProviders);
	    	contentGenerator.setSession(session);
	    	contentGenerator.setUrlFactory(urlFactory);
	    	String contentType = request.getContentType();
//	    	SimpleStreamSource input = new SimpleStreamSource( "input", contentType, in, null ); //$NON-NLS-1$
//        contentGenerator.setInput(input);
	    	contentGenerator.createContent();
	    	if (PentahoSystem.debug) debug( "Generic Servlet content generate successfully" ); //$NON-NLS-1$

	    } catch ( Exception e ) {
	    	error( "Errors trying to generate content: "+request.getQueryString(), e );
	    } finally {
	      // reset the classloader of the current thread
	      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
	      PentahoSystem.systemExitPoint();
	    }
	  }
}
