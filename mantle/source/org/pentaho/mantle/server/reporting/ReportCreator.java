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
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ReportCreator {
  public static JFreeReport createReport(String reportDefinitionPath, IPentahoSession pentahoSession) throws ResourceException, IOException {
    ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    HashMap helperObjects = new HashMap();
    // add the runtime context so that PentahoResourceData class can get access to the solution repo
    helperObjects.put(new FactoryParameterKey(MantleRepositoryResourceData.PENTAHO_REPOSITORY_KEY), PentahoSystem.getSolutionRepository(pentahoSession));
    ResourceKey key = resourceManager.createKey(MantleRepositoryResourceLoader.SOLUTION_SCHEMA_NAME + MantleRepositoryResourceLoader.SCHEMA_SEPARATOR
        + reportDefinitionPath, helperObjects);
    Resource resource = resourceManager.create(key, null, JFreeReport.class);
    return (JFreeReport) resource.getResource();
  }

}
