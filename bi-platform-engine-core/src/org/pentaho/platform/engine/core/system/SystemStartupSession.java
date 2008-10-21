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
 * @created Oct 21, 2008 
 * @author Will Gorman
 */
package org.pentaho.platform.engine.core.system;

/**
 * This class represents the system startup session.  It should
 * only be used when initializing the system.  This session should
 * not be audited.
 */
public class SystemStartupSession extends StandaloneSession {

  private static final long serialVersionUID = 6755414846351542828L;

  public SystemStartupSession() {
    super("system session"); //$NON-NLS-1$
  }
}
