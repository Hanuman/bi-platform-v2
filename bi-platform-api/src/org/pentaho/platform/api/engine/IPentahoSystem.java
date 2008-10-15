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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 27, 2008
 * @author Angelo Rodriguez
 * 
 */
package org.pentaho.platform.api.engine;

/**
 * Since we have moved to a service locator pattern with PentahoSystem being the static root reference,
 * we do not support other implementations of PentahoSystem.
 * TODO a better approach to injecting collections is to inject locators into PentahoSystem that can
 * be easily substituted.
 */
public interface IPentahoSystem {
  public boolean init(IApplicationContext applicationContext);
}
