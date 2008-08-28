package org.pentaho.platform.web.servlet;

import java.io.OutputStream;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.util.xml.XmlHelper;

public class AdhocWebServiceInteract {

	public static void interactiveOutput( final String reportStr, final OutputStream out, final IPentahoSession session ) {
		
		try {
		    System.out.println( "interactiveOutput 1" );
	        Document reportXml = DocumentHelper.parseText( reportStr );
		    System.out.println( "interactiveOutput 2" );
	        Document reportDoc = AdhocWebServiceInteractXml.convertXml(reportXml);
		    System.out.println( "interactiveOutput 3" );
		    System.out.println( reportDoc.asXML() );

	        StringBuffer sb = XmlHelper.transformXml( "iwaqr-report.xsl" , "adhoc", reportDoc.asXML(), new HashMap<String,String>(), new SolutionURIResolver(session));
		    System.out.println( "interactiveOutput 4" );

		    System.out.println( sb.toString() );

	        out.write( sb.toString().getBytes() );
		    System.out.println( "interactiveOutput 5" );

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
