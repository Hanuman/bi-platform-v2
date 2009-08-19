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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.api.repository;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;

/**
 * This interface provides access to high level information about the solution repository
 * in XML format.
 */
public interface ISolutionRepositoryService {
  public org.w3c.dom.Document getSolutionRepositoryDoc(IPentahoSession session, String[] filters)
      throws ParserConfigurationException;

  /**
   * Returns an XML snippet consisting of a single <code>file</code> element. The <code>file</code> element is the same 
   * as would have been returned by <code>getSolutionRepositoryDoc</code>.
   * @param session current session
   * @return doc
   * @throws ParserConfigurationException
   */
  public org.w3c.dom.Document getSolutionRepositoryFileDetails(IPentahoSession session, String fullPath)
      throws ParserConfigurationException;

  /**
   * This method creates a folder along with it's index.xml file.  
   * This method also verifies that the user has PERM_CREATE permissions before
   * creating the folder.
   * 
   * @param userSession the current user 
   * @param solution the solution path
   * @param path the folder path
   * @param name the name of the new folder
   * @param desc the description of the new folder
   * @return true if success
   * @throws IOException
   */
  public boolean createFolder(IPentahoSession userSession, String solution, String path, String name, String desc)
      throws IOException;

  /**
   * This method will delete a file from the ISolutionRepository and respects IPentahoAclEntry.PERM_DELETE.
   * 
   * @param userSession
   *          An IPentahoSession for the user requesting the delete operation
   * @param solution
   *          The name of the solution, such as 'steel-wheels'
   * @param path
   *          The path within the solution to the file/folder to be deleted (does not include the file/folder itself)
   * @param name
   *          The name of the file or folder which will be deleted in the given solution/path
   * @return Success of the delete operation is returned
   * @throws IOException
   */
  public boolean delete(final IPentahoSession userSession, final String solution, final String path, final String name)
      throws IOException;
  
  public void setAcl(final String solution, final String path, final String filename, final String strAclXml,
      final IPentahoSession userSession) throws SolutionRepositoryServiceException,
      IOException, PentahoAccessControlException;
  
  public String getAclXml(final String solution, final String path, final String filename, final IPentahoSession userSession) throws SolutionRepositoryServiceException,
  IOException;

}
