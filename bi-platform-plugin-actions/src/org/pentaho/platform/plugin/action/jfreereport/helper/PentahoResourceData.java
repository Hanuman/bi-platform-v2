/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.action.jfreereport.helper;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.reporting.libraries.resourceloader.FactoryParameterKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.loader.AbstractResourceData;

/**
 * This class is implemented to support loading solution files
 * from the pentaho repository into JFreeReport
 *
 * @author Will Gorman
 */
public class PentahoResourceData extends AbstractResourceData {

  private static final long serialVersionUID = 1806026106310340013L;

  /**
   * @deprecated replaced with the solution repository key (where applicable)
   */
  @Deprecated
  public static final String PENTAHO_RUNTIME_CONTEXT_KEY = "pentahoRuntimeContext"; //$NON-NLS-1$

  public static final String PENTAHO_SOLUTION_REPOSITORY_KEY = "pentahoSolutionRepository"; //$NON-NLS-1$

  private String filename;

  private ResourceKey key;

  private ISolutionRepository solutionRepository;

  /**
   * constructor which takes a resource key for data loading specifics
   * 
   * @param key resource key
   */
  public PentahoResourceData(final ResourceKey key) throws ResourceLoadingException {
    if (key == null) {
      throw new NullPointerException();
    }

    this.key = key;
    this.filename = (String) key.getIdentifier();
    this.solutionRepository = (ISolutionRepository) key.getFactoryParameters().get(
        new FactoryParameterKey(PentahoResourceData.PENTAHO_SOLUTION_REPOSITORY_KEY));
    if (solutionRepository == null) {
      throw new ResourceLoadingException(
          "PentahoResourceData: Failed to retrieve solution repository from resource key"); //$NON-NLS-1$
    }
  }

  /**
   * gets a resource stream from the runtime context.
   * 
   * @param caller resource manager
   * @return input stream
   */
  public InputStream getResourceAsStream(final ResourceManager caller) throws ResourceLoadingException {
    final IActionSequenceResource resource = new ActionSequenceResource(
        "", IActionResource.SOLUTION_FILE_RESOURCE, "application/binary", (String) key.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      return solutionRepository.getResourceInputStream(resource, true);
    } catch (FileNotFoundException e) {
      throw new ResourceLoadingException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * returns a requested attribute, currently only supporting filename.
   * 
   * @param key attribute requested
   * @return attribute value
   */
  public Object getAttribute(final String lookupKey) {
    if (lookupKey.equals(ResourceData.FILENAME)) {
      return filename;
    }
    return null;
  }

  /**
   * return the version number.  We don't have access to file dates or versions
   * so return 0
   * 
   * @param caller resource manager
   * 
   * @return version
   */
  public long getVersion(final ResourceManager caller) throws ResourceLoadingException {
    final IActionSequenceResource resource = new ActionSequenceResource(
        "", IActionResource.SOLUTION_FILE_RESOURCE, "application/binary", (String) key.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
    final ISolutionFile file = solutionRepository.getSolutionFile(resource);
    long version = -1L;
    if (file != null) {
      version = file.getLastModified();
    }
    return version;
  }

  /**
   * get the resource key
   * 
   * @return resource key
   */
  public ResourceKey getKey() {
    return key;
  }
}
