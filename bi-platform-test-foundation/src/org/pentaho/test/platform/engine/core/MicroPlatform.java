/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;

/**
 * This is a test-oriented booter class that extends {@link PentahoSystemBoot}.
 * @author aphillips
 * @see PentahoSystemBoot
 */
@SuppressWarnings("nls")
public class MicroPlatform extends PentahoSystemBoot {

  public MicroPlatform() {
    super();
  }
  public MicroPlatform(String solutionPath) {
    super(solutionPath);
  }

  public MicroPlatform(String solutionPath, String baseUrl) {
    super(solutionPath, baseUrl);
  }

  public MicroPlatform(String solutionPath, IPentahoDefinableObjectFactory factory) {
    super(solutionPath, factory);
  }

  public MicroPlatform(String solutionPath, String baseUrl, IPentahoDefinableObjectFactory factory) {
    super(solutionPath, baseUrl, factory);
  }

}