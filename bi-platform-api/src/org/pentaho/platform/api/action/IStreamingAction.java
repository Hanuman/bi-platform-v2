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
 *
 */
package org.pentaho.platform.api.action;

import java.io.OutputStream;

/**
 * The interface for Actions that want to stream content to the caller.
 * @see IAction
 * @see ILoggingAction
 * @see ISessionAwareAction
 * @see ISystemAwareAction
 * @author aphillips
 * @since 3.6
 */
public interface IStreamingAction extends IAction {

  /**
   * This method sets the OutputStream to write streaming content on.
   * 
   * @param outputStream an OutputStream to write to
   */
  public void setOutputStream(OutputStream outputStream);

  /**
   * Gets the mimetype of the content that this object will write to the
   * output stream
   * @return
   */
  public String getMimeType();

}