/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 12, 2005 
 * @author James Dixon, Angelo Rodriguez, Steven Barkdull
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.pms.core.exception.PentahoMetadataException;

/*
 * Refactoring notes:
 * Break out code into facade classes for SolutionRepository, WaqrRepository
 * For each of the methods in the switch statement in the dispatch method,
 * create a method that takes the parameter provider. These methods will
 * break the parameters out of the parameter provider, and call methods by
 * the same name
 */
/**
 * Servlet Class
 * 
 * web.servlet name="ViewAction" display-name="Name for ViewAction" description="Description for ViewAction" web.servlet-mapping url-pattern="/ViewAction" web.servlet-init-param name="A parameter" value="A value"
 */
public class AdhocWebServiceSaveDisabled extends AdhocWebService {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   * @param fileName
   * @param parameterProvider
   * @param outputStream
   * @param userSession
   * @param isAjax
   * @throws AdhocWebServiceException
   * @throws IOException
   * @throws PentahoMetadataException 
   */
  @Override
  protected void saveReportSpec(final String fileName, final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean isAjax) throws AdhocWebServiceException, IOException,
      PentahoMetadataException {

    if ( true ) {
      throw new AdhocWebServiceException( "Save is disabled." );
    }
  }

}