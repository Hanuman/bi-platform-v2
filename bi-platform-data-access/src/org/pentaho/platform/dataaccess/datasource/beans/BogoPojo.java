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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created May 5, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import java.io.Serializable;

import org.pentaho.metadata.model.concept.security.RowLevelSecurity;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.Alignment;
import org.pentaho.metadata.model.concept.types.Color;
import org.pentaho.metadata.model.concept.types.ColumnWidth;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.pentaho.metadata.model.concept.types.Font;
import org.pentaho.metadata.model.concept.types.JoinType;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.RelationshipType;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.metadata.model.concept.types.TargetColumnType;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import java.lang.Boolean;
/*
 * This class is a workaround for GWT. GWT is not able to compile these classes are they have been used in a map
 * http://code.google.com/p/google-web-toolkit/issues/detail?id=3521
 */
public class BogoPojo implements Serializable{

  private static final long serialVersionUID = 7542132543385685472L;
  TargetTableType targetTableType;
  LocalizedString localizedString;
  DataType dataType;
  AggregationType aggType;
  TargetColumnType targetColumnType;
  LocaleType localeType;
  RowLevelSecurity rowLevelSecurity;
  SecurityOwner securityOwner;
  Security security;
  FieldType fieldType;
  Font font;
  TableType tableType;
  RelationshipType relationshipType;
  JoinType joinType;
  Alignment alignment;
  Color color;
  ColumnWidth columnWidth;
  Boolean booleanValue;
  
  public Boolean getBooleanValue() {
    return booleanValue;
  }
  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }
  public Alignment getAlignment() {
    return alignment;
  }
  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }
  public Color getColor() {
    return color;
  }
  public void setColor(Color color) {
    this.color = color;
  }
  public ColumnWidth getColumnWidth() {
    return columnWidth;
  }
  public void setColumnWidth(ColumnWidth columnWidth) {
    this.columnWidth = columnWidth;
  }
  public JoinType getJoinType() {
    return joinType;
  }
  public void setJoinType(JoinType joinType) {
    this.joinType = joinType;
  }
  public RelationshipType getRelationshipType() {
    return relationshipType;
  }
  public void setRelationshipType(RelationshipType relationshipType) {
    this.relationshipType = relationshipType;
  }
  public TableType getTableType() {
    return tableType;
  }
  public void setTableType(TableType tableType) {
    this.tableType = tableType;
  }
  public Font getFont() {
    return font;
  }
  public void setFont(Font font) {
    this.font = font;
  }
  public TargetTableType getTargetTableType() {
    return targetTableType;
  }
  public void setTargetTableType(TargetTableType targetTableType) {
    this.targetTableType = targetTableType;
  }
  public LocalizedString getLocalizedString() {
    return localizedString;
  }
  public void setLocalizedString(LocalizedString localizedString) {
    this.localizedString = localizedString;
  }
  public DataType getDataType() {
    return dataType;
  }
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  public AggregationType getAggType() {
    return aggType;
  }
  public void setAggType(AggregationType aggType) {
    this.aggType = aggType;
  }
  public TargetColumnType getTargetColumnType() {
    return targetColumnType;
  }
  public void setTargetColumnType(TargetColumnType targetColumnType) {
    this.targetColumnType = targetColumnType;
  }
  public void setLocaleType(LocaleType localeType) {
    this.localeType = localeType;
  }
  public LocaleType getLocaleType() {
    return localeType;
  }
  public void setRowLevelSecurity(RowLevelSecurity rowLevelSecurity) {
    this.rowLevelSecurity = rowLevelSecurity;
  }
  public RowLevelSecurity getRowLevelSecurity() {
    return rowLevelSecurity;
  }
  public void setSecurityOwner(SecurityOwner securityOwner) {
    this.securityOwner = securityOwner;
  }
  public SecurityOwner getSecurityOwner() {
    return securityOwner;
  }
  public void setSecurity(Security security) {
    this.security = security;
  }
  public Security getSecurity() {
    return security;
  }
  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }
  public FieldType getFieldType() {
    return fieldType;
  }
}
