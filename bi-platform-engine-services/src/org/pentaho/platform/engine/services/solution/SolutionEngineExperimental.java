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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 18, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services.solution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.engine.services.runtime.RuntimeContextExperimental;

public class SolutionEngineExperimental extends SolutionEngine {
  
  /**
   * 
   */
  private static final long serialVersionUID = 9122093928205589692L;
  private static final Log logger = LogFactory.getLog(SolutionEngineExperimental.class);
  
  @Override
  public Log getLogger() {
    return SolutionEngineExperimental.logger;
  }

  public SolutionEngineExperimental() {
    super();
  }

  @Override
  protected void createRuntime(final IRuntimeElement runtimeData, final String solutionName,
      final IOutputHandler outputHandler, final String processId, final IPentahoUrlFactory urlFactory) {
    runtime = new RuntimeContextExperimental(runtimeData.getInstanceId(), this, solutionName, runtimeData, getSession(), outputHandler,
        processId, urlFactory, getParameterProviders(), getMessages(), getCreateFeedbackParameterCallback());
  }

}
