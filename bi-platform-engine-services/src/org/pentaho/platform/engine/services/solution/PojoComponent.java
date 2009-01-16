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
 * Copyright 2008-2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
 package org.pentaho.platform.engine.services.solution;

import java.io.OutputStream;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IConfiguredPojo;
import org.pentaho.platform.api.engine.IStreamingPojo;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.services.solution.ComponentBase;

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

  protected void callMethod( Method method, Object value ) throws Throwable {
    if( value instanceof String ) {
      callMethodWithString( method, value.toString() );
      return;
    }
    Class<?> paramClasses[] = method.getParameterTypes();
    if( paramClasses.length != 1 ) {
      // we don't know how to handle this
      throw new GenericSignatureFormatError();
    }
    Class<?> paramclass = paramClasses[0];
    // do some type safety. this would be the point to do automatic type conversions
    if( value instanceof IPentahoResultSet && paramclass.equals( IPentahoResultSet.class )) {
      method.invoke(pojo, new Object[] { (IPentahoResultSet) value } );
    }
    else if( value instanceof java.lang.Boolean && ( paramclass.equals( Boolean.class ) || paramclass.equals( boolean.class ) ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof java.lang.Integer && ( paramclass.equals( Integer.class ) || paramclass.equals( int.class ) ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof java.lang.Long && ( paramclass.equals( Long.class ) || paramclass.equals( long.class ) ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof java.lang.Double && ( paramclass.equals( Double.class ) || paramclass.equals( double.class ) ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof java.lang.Float && ( paramclass.equals( Float.class ) || paramclass.equals( float.class ) ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof IPentahoStreamSource && paramclass.equals( IPentahoStreamSource.class ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof IContentItem && paramclass.equals( IContentItem.class ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( value instanceof IContentItem && paramclass.equals( String.class ) ) {
      method.invoke(pojo, new Object[] { value.toString() } );
    }
    else {
      // just try it I guess
      method.invoke(pojo, new Object[] { value.toString() } );
    }
    
  }
  
  protected void callMethodWithString( Method method, String value ) throws Throwable {
    Class<?> paramClasses[] = method.getParameterTypes();
    if( paramClasses.length != 1 ) {
      // we don't know how to handle this
      throw new GenericSignatureFormatError();
    }
    Class<?> paramclass = paramClasses[0];
    if( paramclass.equals( String.class ) ) {
      method.invoke(pojo, new Object[] { value } );
    }
    else if( paramclass.equals( Boolean.class ) || paramclass.equals( boolean.class ) ) {
      method.invoke(pojo, new Object[] { new Boolean( value ) } );
    }
    else if( paramclass.equals( Integer.class ) || paramclass.equals( int.class )) {
      method.invoke(pojo, new Object[] { new Integer( value ) } );
    }
    else if( paramclass.equals( Long.class ) || paramclass.equals( long.class )) {
      method.invoke(pojo, new Object[] { new Long( value ) } );
    }
    else if( paramclass.equals( Double.class ) || paramclass.equals( double.class )) {
      method.invoke(pojo, new Object[] { new Double( value ) } );
    }
    else if( paramclass.equals( Float.class ) || paramclass.equals( float.class )) {
      method.invoke(pojo, new Object[] { new Float( value ) } );
    } else {
      // TODO handle dates
      throw new GenericSignatureFormatError();
    }
  }
  
  @Override
  protected boolean executeAction() throws Throwable {

    Set<?> inputNames = getInputNames();
    Element defnNode = (Element) getComponentDefinition();

    // first do the system settings so that component settings and inputs can override them if necessary
    if( pojo instanceof IConfiguredPojo ) {
      IConfiguredPojo config = (IConfiguredPojo) pojo;
      Set<String> settingsPaths = config.getConfigSettingsPaths();
      Iterator<String> keys = settingsPaths.iterator();
      Map<String,String> settings = new HashMap<String,String>();
      SystemSettingsParameterProvider params = new SystemSettingsParameterProvider();
      while( keys.hasNext() ) {
        String path = keys.next();
        String value = params.getStringParameter( path, null );
        if( value != null ) {
          settings.put( path, value );
        }
        /*
        String path = keys.next();
        // parse out the path
        int pos1 = path.indexOf( '{' );
        int pos2 = path.indexOf( '}' );
        if( pos1 > 0 && pos2 > 0 ) {
          String file = path.substring( 0, pos1 );
          String setting = path.substring( pos1+1, pos2 );
          String value = PentahoSystem.getSystemSetting( file, setting, null );
          if( value != null ) {
            settings.put( path, value );
          }
        }
        */
      }
      config.configure( settings );
    }

    Map<String,Object> map = new HashMap<String,Object>();
    // look at the component settings
    List<?> nodes = defnNode.selectNodes( "*" ); //$NON-NLS-1$
    for( int idx=0; idx<nodes.size(); idx++ ) {
      Element node = (Element) nodes.get( idx );
      String name = node.getName().toUpperCase();
      if( !name.equals( "CLASS" ) && !name.equals( "OUTPUTSTREAM" )) { //$NON-NLS-1$ //$NON-NLS-2$
        String value = node.getText();
        Method method = setMethods.get( name );
        if( method != null ) {
          callMethodWithString( method, value );
        } 
        else if( runtimeInputsMethod != null ) {
          map.put(name, value);
        } else {
          throw new NoSuchMethodException( "set"+name ); //$NON-NLS-1$
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
        callMethod( method, value );
      } 
      else if( runtimeInputsMethod != null ) {
        map.put(name, value);
      } else {
        throw new NoSuchMethodException( "set"+name ); //$NON-NLS-1$
      }
    }

    // now process all of the resources and see if we can call them as setters
    Set<?> resourceNames = getResourceNames();
    if( resourceNames!= null && resourceNames.size() > 0 ) {
      it = resourceNames.iterator();
      while( it.hasNext() ) {
        String name = (String) it.next();
        IActionSequenceResource resource = getResource( name );
        IPentahoStreamSource stream = getResourceDataSource( resource );
        Method method = setMethods.get( name.toUpperCase() );
        if( method != null ) {
          method.invoke(pojo, new Object[] { stream } );
        } else {
          // BISERVER-2715 we should ignore this as the resource might be meant for another component
        }
      }
    }
    
    if( map.size() > 0 && runtimeInputsMethod != null ) {
      // call the generic input setter
      runtimeInputsMethod.invoke( pojo , new Object[] { map } );
    }
    
    if( getOutputNames().contains( "outputstream" ) && setMethods.containsKey( "OUTPUTSTREAM" ) && pojo instanceof IStreamingPojo) { //$NON-NLS-1$ //$NON-NLS-2$
      // set the output stream
      String mimeType = ((IStreamingPojo) pojo).getMimeType();
      IContentItem contentItem = getOutputContentItem( "outputstream", mimeType ); //$NON-NLS-1$
      OutputStream out = contentItem.getOutputStream( null );
      Method method = setMethods.get( "OUTPUTSTREAM" ); //$NON-NLS-1$
      method.invoke( pojo , new Object[] {out} );
    }
    
    if( validateMethod != null ) {
      Object obj = validateMethod.invoke( pojo, (Object[]) null );
      if( obj instanceof Boolean ) {
        Boolean ok = (Boolean) obj;
        if( !ok ) {
          return false;
        }
      }
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
      if( name.equals( "outputstream" ) ) { //$NON-NLS-1$
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
    if( isDefinedInput( "class" ) ) { //$NON-NLS-1$
      String className = getInputStringValue( "class" ); //$NON-NLS-1$
      // try to load the class
      try {
        // TODO support loading classes from the solution repository
        Class<?> aClass = getClass().getClassLoader().loadClass(className);
        pojo = aClass.newInstance();
        Method methods[] = pojo.getClass().getMethods();
        // create a method map
        for( int idx=0; idx<methods.length; idx++ ) {
          String name = methods[idx].getName();
          if( name.equals( "getOutputs" ) ) { //$NON-NLS-1$
            runtimeOutputsMethod = methods[idx];
          }
          else if( name.equals( "setInputs" ) ) { //$NON-NLS-1$
            runtimeInputsMethod = methods[idx];
          }
          else if( name.startsWith( "set" ) ) { //$NON-NLS-1$
            name = name.substring( 3 ).toUpperCase();
            setMethods.put( name , methods[idx] );
          }
          else if( name.startsWith( "get" ) ) { //$NON-NLS-1$
            name = name.substring( 3 ).toUpperCase();
            getMethods.put( name , methods[idx] );
          }
          else if( name.equalsIgnoreCase( "execute" ) ) { //$NON-NLS-1$
            executeMethod = methods[idx];
          }
          else if( name.equalsIgnoreCase( "validate" ) ) { //$NON-NLS-1$
            validateMethod = methods[idx];
          }
          else if( name.equalsIgnoreCase( "done" ) ) { //$NON-NLS-1$
            doneMethod = methods[idx];
          }
        }

        ok = true;
      } catch (Throwable e) {
        error( "Could not load object class" , e); //$NON-NLS-1$
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
