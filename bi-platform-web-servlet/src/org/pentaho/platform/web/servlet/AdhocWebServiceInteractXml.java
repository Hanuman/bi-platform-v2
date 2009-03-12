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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

public class AdhocWebServiceInteractXml {

	public static Document convertXml( final Document reportXml ) {
		
        // get the list of headers
        List<String> headerList = new ArrayList<String>();
        List<?> nodes = reportXml.selectNodes( "/report/groupheader/@name" );
        // find all the unique group header names

        Iterator<?> it = nodes.iterator();
        Attribute attr;
        String name;
        while( it.hasNext() ) {
        	// we only need to go until we get the first duplicate
        	attr = (Attribute) it.next();
        	name = attr.getText();
        	if( !"dummy".equals( name ) ) {
        		if( !headerList.contains( name ) ) {
        			headerList.add( name );
                	System.out.println(name);
        		} else {
        			break;
        		}
        	}
        }
        
        String headerNames[] = new String[ headerList.size() ];
        String headerValues[] = new String[ headerList.size() ];
        Element headerNodes[] = new Element[ headerList.size() ];
        String columnHeaders[] = new String[0];
        Element columnHeaderNodes[] = new Element[0];
        headerList.toArray( headerNames );
        for( int idx=0; idx<headerValues.length; idx++ ) {
        	headerValues[ idx ] = "";
        }

      Document reportDoc = DocumentHelper.createDocument();
      Element reportNode = DocumentHelper.createElement("report");
      reportDoc.setRootElement( reportNode );

      // process the top-level nodes
      nodes = reportXml.selectNodes( "/report/*" );
        
      Node node;
      // go thru all the nodes
      it = nodes.iterator();
      while( it.hasNext() ) {
      	node = (Node) it.next();
      	name = node.getName();
      	if( "groupheader".equals( name ) ) {
            // process the group headers
      		// get the group header name
      		String headerName = node.selectSingleNode( "@name" ).getText();
      		if( !"dummy".equals( headerName ) ) {
      			// find the header index
      			String headerValue = node.selectSingleNode( "element[1]" ).getText();
          		int headerIdx=-1;
          		for( int idx=0; idx<headerNames.length; idx++ ) {
          			if( headerNames[idx].equals( headerName ) ) {
          				headerIdx = idx;
          				break;
          			}
          		}
          		if( !headerValues[headerIdx].equals( headerValue ) ) {
          			// this is a new header value
          			headerValues[headerIdx] = headerValue;
          			// find the parent node
          			Element parentNode;
          			if( headerIdx == 0 ) {
          				parentNode = reportNode;
          			} else {
          				parentNode = headerNodes[headerIdx-1];
          			}
          			
          			// create a group header node for this
                    Element headerNode = DocumentHelper.createElement( "groupheader" );
          			parentNode.add( headerNode );
          			headerNodes[headerIdx] = headerNode;

          			// create the name attribute
                    attr = DocumentHelper.createAttribute( headerNode, "name", headerName);
                    headerNode.add( attr );
                    
                    // create the value node
                    Element elementNode = DocumentHelper.createElement( "element" );
          			headerNode.add( elementNode );
                    attr = DocumentHelper.createAttribute( elementNode, "name", headerName);
                    elementNode.add( attr );
                    elementNode.setText( headerValue );
                    
          		}
      		}
      		
            // see if there are any column headers
      		List<?> elements = node.selectNodes( "element" );
      		if( elements.size() == 0 ) {
      			elements = node.selectNodes( "band/element" );
      		}
      		if( elements.size() > 1 ) {
      			// there are column headers here, get them and store them for the next set of rows
      			columnHeaders = new String[elements.size()-1];
      			columnHeaderNodes = new Element[elements.size()-1];
      			for( int idx=1; idx<elements.size(); idx++ ) {
      				columnHeaders[idx-1] = ((Element)elements.get(idx)).getText();
      			}
      		}
      	}
      	else if( "items".equals( name ) ) {
            // process items (rows)
      		// get the parent node, this should always be the last one on the list
      		Element parentNode;
      		if( headerNodes.length == 0 ) {
      			parentNode = reportNode;
      		} else {
          		parentNode = headerNodes[headerNodes.length-1];
      		}
            // create the items node
            Element itemsNode = DocumentHelper.createElement( "items" );
  			parentNode.add( itemsNode );
            // create the headers node
            Element headersNode = DocumentHelper.createElement( "headers" );
  			itemsNode.add( headersNode );
            // create the rows node
            Element itemBandsNode = DocumentHelper.createElement( "itembands" );
  			itemsNode.add( itemBandsNode );
  			for( int idx=0; idx<columnHeaders.length; idx++ ) {
                Element headerNode = DocumentHelper.createElement( "header" );
                headerNode.setText( columnHeaders[idx] );
      			headersNode.add( headerNode );
      			columnHeaderNodes[ idx ] = headerNode;
  				
  			}
  			// now copy the item bands over
      		List<?> itembands = node.selectNodes( "itemband" );
      		Iterator<?> bands = itembands.iterator();
      		boolean first = true;
      		while( bands.hasNext() ) {
      			Element itemband = (Element) bands.next();
                Element itemBandNode = DocumentHelper.createElement( "itemband" );
                itemBandsNode.add( itemBandNode );
          		List<?> elementList = itemband.selectNodes( "element" );
          		Iterator<?> elements = elementList.iterator();
          		int idx = 0;
          		while( elements.hasNext() ) {
          			Element element = (Element) elements.next();
                    Element elementNode = DocumentHelper.createElement( "element" );
                    itemBandNode.add( elementNode );
                    elementNode.setText( element.getText() );
                    name = element.selectSingleNode("@name").getText();
                    if( name.endsWith( "Element" ) ) {
                    	name = name.substring(0, name.length()-"Element".length() );
                    }
                    attr = DocumentHelper.createAttribute( elementNode, "name", name );
                    elementNode.add( attr );
          			if( first ) {
          				// copy the item name over to the column header
                        attr = DocumentHelper.createAttribute( columnHeaderNodes[idx], "name", name );
                        columnHeaderNodes[idx].add( attr );
          			}
          			idx++;
          		}
          		first = false;
      			
      		}
  			
      	}
          
      }

      return reportDoc;
		
	}
	
}
