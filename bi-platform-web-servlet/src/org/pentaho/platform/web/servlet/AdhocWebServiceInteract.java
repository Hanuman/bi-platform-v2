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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
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
//		    System.out.println( "interactiveOutput 1" );
	        Document reportXml = DocumentHelper.parseText( reportStr );
//		    System.out.println( "interactiveOutput 2" );
	        Document reportDoc = AdhocWebServiceInteractXml.convertXml(reportXml);
//		    System.out.println( "interactiveOutput 3" );
//		    System.out.println( reportDoc.asXML() );

	        StringBuffer sb = XmlHelper.transformXml( "iwaqr-report.xsl" , "adhoc", reportDoc.asXML(), new HashMap<String,String>(), new SolutionURIResolver(session)); //$NON-NLS-1$ //$NON-NLS-2$
//		    System.out.println( "interactiveOutput 4" );

//		    System.out.println( sb.toString() );

	        out.write( sb.toString().getBytes() );
//		    System.out.println( "interactiveOutput 5" );

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
