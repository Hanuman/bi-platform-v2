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
package org.pentaho.commons.util.repository;

import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;

public class GetDescendantsRequest {

  private String repositoryId;
  
  private String folderId;
  
  private TypesOfFileableObjects type;
  
  private int depth;
  
  private String filter;
  
  private boolean includeAllowableActions;
  
  private boolean includeRelationships;

  public String getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  public String getFolderId() {
    return folderId;
  }

  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

  public TypesOfFileableObjects getType() {
    return type;
  }

  public void setType(TypesOfFileableObjects type) {
    this.type = type;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public boolean isIncludeAllowableActions() {
    return includeAllowableActions;
  }

  public void setIncludeAllowableActions(boolean includeAllowableActions) {
    this.includeAllowableActions = includeAllowableActions;
  }

  public boolean isIncludeRelationships() {
    return includeRelationships;
  }

  public void setIncludeRelationships(boolean includeRelationships) {
    this.includeRelationships = includeRelationships;
  }

  
}
