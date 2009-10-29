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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;

public class UrlCommand extends AbstractCommand {

  String url;
  String title;
  
  public UrlCommand(String url, String title ) {
    this.url = url;
    this.title = title;
  }

  protected void performOperation()
  {
    SolutionBrowserPerspective navigatorPerspective = SolutionBrowserPerspective.getInstance();
    navigatorPerspective.getContentTabPanel().showNewURLTab( title, "", url); //$NON-NLS-1$
  }

  protected void performOperation(boolean feedback)
  {
    SolutionBrowserPerspective navigatorPerspective = SolutionBrowserPerspective.getInstance();
    navigatorPerspective.getContentTabPanel().showNewURLTab( title, "", url); //$NON-NLS-1$
  }
}
