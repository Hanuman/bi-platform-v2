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
 * Copyright 2010 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan, 2010 
 * @author James Dixon
 * 
 */
package org.pentaho.commons.util.repository.type;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

public class CmisObject {

  public static final String OBJECT_TYPE_FOLDER = "Folder"; //$NON-NLS-1$
  
  public static final String LOCALIZEDNAME = "LocalizedName";  //$NON-NLS-1$
  
  public static final String NAME = "Name";  //$NON-NLS-1$
  
  public static final String VISIBLE = "Visible";  //$NON-NLS-1$
  
  private CmisProperties properties;
  
  private AllowableActions allowableActions;
  
  private List<CmisObject> relationship;
  
  private List<CmisObject> child;

  public CmisProperties getProperties() {
    return properties;
  }

  public void setProperties(CmisProperties properties) {
    this.properties = properties;
  }

  public AllowableActions getAllowableActions() {
    return allowableActions;
  }

  public void setAllowableActions(AllowableActions allowableActions) {
    this.allowableActions = allowableActions;
  }

  public List<CmisObject> getRelationship() {
    return relationship;
  }

  public void setRelationship(List<CmisObject> relationship) {
    this.relationship = relationship;
  }

  public List<CmisObject> getChild() {
    return child;
  }

  public void setChild(List<CmisObject> child) {
    this.child = child;
  }
  
  public String findStringProperty( String name ) {
    return (String) findProperty( name, new PropertyType(PropertyType.STRING) ).getValue();
  }
  
  public Boolean findBooleanProperty( String name ) {
    return (Boolean) findProperty( name, new PropertyType(PropertyType.BOOLEAN) ).getValue();
  }
  
  public Calendar findDateTimeProperty( String name ) {
    return (Calendar) findProperty( name, new PropertyType(PropertyType.DATETIME) ).getValue();
  }
  
  public BigDecimal findDecimalProperty( String name ) {
    return (BigDecimal) findProperty( name, new PropertyType(PropertyType.DECIMAL) ).getValue();
  }
  
  public String findHtmlProperty( String name ) {
    return (String) findProperty( name, new PropertyType(PropertyType.HTML) ).getValue();
  }
  
  public String findIdProperty( String name ) {
    return (String) findProperty( name, new PropertyType(PropertyType.ID) ).getValue();
  }
  
  public Integer findIntegerProperty( String name ) {
    return (Integer) findProperty( name, new PropertyType(PropertyType.INTEGER) ).getValue();
  }
  
  public String findUriProperty( String name ) {
    return (String) findProperty( name, new PropertyType(PropertyType.URI) ).getValue();
  }
  
  public String findXmlProperty( String name ) {
    return (String) findProperty( name, new PropertyType(PropertyType.XML) ).getValue();
  }
  

  
  private CmisProperty findProperty( String name, PropertyType type ) {
    if( properties != null ) {
      for( CmisProperty aProperty : properties.getProperties() ) {
        if( aProperty.getName().equals(name) && aProperty.getPropertyType().getType().equals( type.getType() ) )
          return aProperty;
      }
    }
    return null;
  }
  
}
