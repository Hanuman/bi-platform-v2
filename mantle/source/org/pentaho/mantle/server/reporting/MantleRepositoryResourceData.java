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
 */
package org.pentaho.mantle.server.reporting;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.jfree.resourceloader.FactoryParameterKey;
import org.jfree.resourceloader.ResourceData;
import org.jfree.resourceloader.ResourceKey;
import org.jfree.resourceloader.ResourceLoadingException;
import org.jfree.resourceloader.ResourceManager;
import org.jfree.resourceloader.loader.AbstractResourceData;
import org.pentaho.platform.api.repository.ISolutionRepository;

/**
 * This class is implemented to support loading solution files from the pentaho repository into JFreeReport
 * 
 * @author Will Gorman/Michael D'Amour
 */
public class MantleRepositoryResourceData extends AbstractResourceData {

  public static final String PENTAHO_REPOSITORY_KEY = "pentahoRepositoryKey"; //$NON-NLS-1$

  private String filename;

  private ResourceKey key;

  private ISolutionRepository solutionRepository;

  /**
   * constructor which takes a resource key for data loading specifics
   * 
   * @param key
   *          resource key
   */
  public MantleRepositoryResourceData(final ResourceKey key) {
    if (key == null) {
      throw new NullPointerException();
    }

    this.key = key;
    this.filename = (String) key.getIdentifier();
    try {
      this.solutionRepository = (ISolutionRepository) key.getFactoryParameters().get(new FactoryParameterKey(PENTAHO_REPOSITORY_KEY));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (solutionRepository == null) {
      throw new InstantiationError("MantleRepositoryResourceData: Failed to solution repository from resource key"); //$NON-NLS-1$
    }
  }

  /**
   * gets a resource stream from the runtime context.
   * 
   * @param caller
   *          resource manager
   * @return input stream
   */
  public InputStream getResourceAsStream(ResourceManager caller) throws ResourceLoadingException {
    try {
      return solutionRepository.getResourceInputStream(key.getIdentifier().toString(), false);
    } catch (FileNotFoundException e) {
      throw new ResourceLoadingException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * returns a requested attribute, currently only supporting filename.
   * 
   * @param key
   *          attribute requested
   * @return attribute value
   */
  public Object getAttribute(String lookupKey) {
    if (lookupKey.equals(ResourceData.FILENAME)) {
      return filename;
    }
    return null;
  }

  /**
   * return the version number
   * 
   * @param caller
   *          resource manager
   * 
   * @return version
   */
  public long getVersion(ResourceManager caller) throws ResourceLoadingException {
    return solutionRepository.getFileByPath(key.getIdentifier().toString()).getLastModified();
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
