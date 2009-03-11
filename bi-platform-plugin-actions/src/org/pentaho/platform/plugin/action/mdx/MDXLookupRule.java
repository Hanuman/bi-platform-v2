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
 *
 * Created Sep 21, 2005 
 * @author wseyler
 */

package org.pentaho.platform.plugin.action.mdx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MDXLookupRule extends MDXBaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 7178952532238358504L;

  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(MDXLookupRule.class);
  }

  @Override
  public boolean init() {
    return true;
  }
}
