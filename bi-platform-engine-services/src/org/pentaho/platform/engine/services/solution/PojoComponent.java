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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IStreamingPojoComponent;
import org.pentaho.platform.api.repository.IContentItem;

public class PojoComponent extends ComponentBase {

	private static final long serialVersionUID = 7064470160805918218L;

	protected Object pojo;
	
	Map<String, Method> getMethods = new HashMap<String, Method>();
	Map<String, Method> setMethods = new HashMap<String, Method>();
	Method executeMethod = null;
	Method validateMethod = null;
	Method doneMethod = null;
	Method runtimeInputsMethod = null;
	Method runtimeOutputsMethod = null;

	public Log getLogger() {
	    return LogFactory.getLog(PojoComponent.class);
	}

	@Override
	public void done() {
		if( doneMethod != null && pojo != null ) {
			try {
				doneMethod.invoke( pojo , (Object[]) null );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean executeAction() throws Throwable {

		Set<?> inputNames = getInputNames();
		Element defnNode = (Element) getComponentDefinition();

		Map<String,Object> map = new HashMap<String,Object>();
		// look at the component settings
		List<?> nodes = defnNode.selectNodes( "*" );
		for( int idx=0; idx<nodes.size(); idx++ ) {
			Element node = (Element) nodes.get( idx );
			String name = node.getName().toUpperCase();
			if( !name.equals( "CLASS" ) && !name.equals( "OUTPUTSTREAM" )) {
				String value = node.getText();
				Method method = setMethods.get( name );
				if( method != null ) {
					method.invoke(pojo, new Object[] { value } );
				} 
				else if( runtimeInputsMethod != null ) {
					map.put(name, value);
				} else {
					throw new NoSuchMethodException( "set"+name );
				}
			}
		}
		
		// now process all of the inputs, overriding the component settings
		Iterator<?> it = inputNames.iterator();
		while( it.hasNext() ) {
			String name = (String) it.next();
			Object value = getInputValue( name );
			Method method = setMethods.get( name.toUpperCase() );
			if( method != null ) {
				method.invoke(pojo, new Object[] { value } );
			} 
			else if( runtimeInputsMethod != null ) {
				map.put(name, value);
			} else {
				throw new NoSuchMethodException( "set"+name );
			}
		}

		if( map.size() > 0 && runtimeInputsMethod != null ) {
			// call the generic input setter
			runtimeInputsMethod.invoke( pojo , new Object[] { map } );
		}
		
		if( getOutputNames().contains( "outputstream" ) && setMethods.containsKey( "OUTPUTSTREAM" ) && pojo instanceof IStreamingPojoComponent) {
			// set the output stream
			String mimeType = ((IStreamingPojoComponent) pojo).getMimeType();
			IContentItem contentItem = getOutputContentItem( "outputstream", mimeType );
			OutputStream out = contentItem.getOutputStream( null );
			Method method = setMethods.get( "OUTPUTSTREAM" );
			method.invoke( pojo , new Object[] {out} );
		}
		
		// now execute the pojo
		Boolean result = Boolean.FALSE;
		if( executeMethod != null ) {
			result = (Boolean) executeMethod.invoke( pojo, new Object[] {} );
		} else {
			// we can only assume we are ok so far
			result = Boolean.TRUE;
		}
		
		// now handle outputs
		Set<?> outputNames = getOutputNames();
		// first get the runtime outputs
		map = new HashMap<String,Object>();
		if( runtimeOutputsMethod != null ) {
			map = (Map<String,Object>) runtimeOutputsMethod.invoke( pojo, new Object[] {} );
		}
		if( map.size() > 0 ) {
			
		}
		it = outputNames.iterator();
		while( it.hasNext() ) {
			String name = (String) it.next();
			if( name.equals( "outputstream" ) ) {
				// we should be done
			} else {
				IActionParameter param = getOutputItem( name );
				Method method = getMethods.get( name.toUpperCase() );
				if( method != null ) {
					Object value = method.invoke(pojo, new Object[] { } );
					param.setValue( value );
				} else {
					Object value = map.get( name );
					if( value != null ) {
						param.setValue( value );
					} else {
						throw new NoSuchMethodException( name );
					}
				}
			}
		}
		
		return result.booleanValue();
	}

	@Override
	public boolean init() {
		// nothing to do here
		return true;
	}

	@Override
	protected boolean validateAction() {
				
		boolean ok = false;
		if( isDefinedInput( "class" ) ) {
			String className = getInputStringValue( "class" );
			// try to load the class
			try {
				// TODO support loading classes from the solution repository
				Class<?> aClass = getClass().getClassLoader().loadClass(className);
				pojo = aClass.newInstance();
				Method methods[] = pojo.getClass().getMethods();
				// create a method map
				for( int idx=0; idx<methods.length; idx++ ) {
					String name = methods[idx].getName();
					if( name.equals( "getOutputs" ) ) {
						runtimeOutputsMethod = methods[idx];
					}
					else if( name.equals( "setInputs" ) ) {
						runtimeInputsMethod = methods[idx];
					}
					else if( name.startsWith( "set" ) ) {
						name = name.substring( 3 ).toUpperCase();
						setMethods.put( name , methods[idx] );
					}
					else if( name.startsWith( "get" ) ) {
						name = name.substring( 3 ).toUpperCase();
						getMethods.put( name , methods[idx] );
					}
					else if( name.equalsIgnoreCase( "execute" ) ) {
						executeMethod = methods[idx];
					}
					else if( name.equalsIgnoreCase( "validate" ) ) {
						validateMethod = methods[idx];
					}
					else if( name.equalsIgnoreCase( "done" ) ) {
						doneMethod = methods[idx];
					}
				}

				if( validateMethod != null ) {
					Object obj = validateMethod.invoke( pojo, (Object[]) null );
					if( obj instanceof Boolean ) {
						ok = (Boolean) obj;
					}
					
				} else {
					// assume everything is ok
					ok = true;
				}
			} catch (Exception e) {
				error( "Could not load object class" , e);
			}
		}
		
		return ok;
	}

	@Override
	protected boolean validateSystemSettings() {
		// nothing to do here, the pojo must do this during its init
		return true;
	}

}
