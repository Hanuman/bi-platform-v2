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
package org.pentaho.mantle.server.reporting;

import java.io.IOException;
import java.util.HashMap;

import org.jfree.report.JFreeReport;
import org.jfree.resourceloader.FactoryParameterKey;
import org.jfree.resourceloader.Resource;
import org.jfree.resourceloader.ResourceException;
import org.jfree.resourceloader.ResourceKey;
import org.jfree.resourceloader.ResourceManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ReportCreator {
  public static JFreeReport createReport(String reportDefinitionPath, IPentahoSession pentahoSession) throws ResourceException, IOException {
    ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    HashMap helperObjects = new HashMap();
    // add the runtime context so that PentahoResourceData class can get access to the solution repo
    helperObjects.put(new FactoryParameterKey(MantleRepositoryResourceData.PENTAHO_REPOSITORY_KEY), PentahoSystem.get(ISolutionRepository.class, pentahoSession));
    ResourceKey key = resourceManager.createKey(MantleRepositoryResourceLoader.SOLUTION_SCHEMA_NAME + MantleRepositoryResourceLoader.SCHEMA_SEPARATOR
        + reportDefinitionPath, helperObjects);
    Resource resource = resourceManager.create(key, null, JFreeReport.class);
    return (JFreeReport) resource.getResource();
  }

}
