package org.pentaho.test.platform.web.ui;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.engine.core.BaseTest;

public class BaseUITest extends BaseTest {

	protected static HttpConnection connection;
	protected static HttpClient httpClient;
	protected static String baseUrl;
	protected static HttpConnectionManager connectionManager;
	protected static HttpConnectionManagerParams connectionParams;

    public void setUp() {
    	super.setUp();
    		if( baseUrl == null && PentahoSystem.getInitializedOK() ) {
    			baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    		}
    }
    
    protected HttpClient getClient() {
    		if( connectionManager == null ) {
    			connectionManager = new SimpleHttpConnectionManager();
    		}
		if( httpClient == null && connectionManager != null ) {
			connectionParams = connectionManager.getParams();
			connectionParams.setConnectionTimeout( 5000 );
			connectionParams.setSoTimeout( 10000 );
	        httpClient  = new HttpClient( connectionManager );
		}
		return httpClient;
    }
    
    protected void newSession() {
    		httpClient = null;
    }
    
    public void tearDown() {
    	super.tearDown();
    }

    protected void runUrl( String url, String testName ) {
    		
    		OutputStream stream = getOutputStream( testName, ".html" ); //$NON-NLS-1$
    	
    		try {
    			if( stream != null ) {
        			submitGet( url, stream ); 
    			}
    		} finally {
    			try {
        			stream.close();
    			} catch (Exception e) {
    			}
    		}
    		
    		// compare content with golden...
    		compare( testName, ".html" ); //$NON-NLS-1$
    }

    
    protected void submitGet( String url, OutputStream tmpFile ) {

    		HttpClient localHttpClient = getClient();

    		try {

    	        GetMethod call = new GetMethod( baseUrl + url );
    	        
    			int status = localHttpClient.executeMethod( call );
    			if( status == 200 ) {
    				InputStream response = call.getResponseBodyAsStream();
    				try {
    					if( tmpFile != null ) {
    						byte buffer[] = new byte[2048];
    						int size = response.read( buffer );
    						while( size > 0 ) {
    							tmpFile.write( buffer, 0, size );
    							size = response.read( buffer );
    						}
    					}
    				} catch (Exception e) {
    					// we can ignore this because the content comparison will fail
    				}
    			}
    		} catch (Throwable e) {
    			PrintStream print = new PrintStream( tmpFile );
    			e.printStackTrace( print );
    		}

    	}
    
}
