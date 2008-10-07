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
 *
 * @created Jan 2, 2006
 * @author Matt Casters
 */

package org.pentaho.platform.plugin.action.kettle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandlerCache;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;

/**
 * KettleComponent shows a list of available transformations in the root of the choosen repository.
 * 
 * @author Matt
 * 
 * Legitimate outputs:
 *  EXECUTION_STATUS_OUTPUT - (execution-status)
 *    [JOB | TRANS] Returns the resultant execution status
 *    
 *  EXECUTION_LOG_OUTPUT - (execution-log)
 *    [JOB | TRANS] Returns the resultant log
 *  
 *  TRANSFORM_SUCCESS_OUTPUT - (transformation-written)
 *    [Requires MONITORSTEP to be defined]
 *    [TRANS] Returns a "result-set" for all successful rows written (Unless 
 *    error handling is not defined for the specified step, in which case ALL
 *    rows are returned here)
 *  
 *  TRANSFORM_ERROR_OUTPUT - (transformation-errors)
 *    [Requires MONITORSTEP to be defined]
 *    [TRANS] Returns a "result-set" for all rows written that have caused an error
 *  
 *  TRANSFORM_SUCCESS_COUNT_OUTPUT - (transformation-written-count)
 *    [Requires MONITORSTEP to be defined]
 *    [TRANS] Returns a count of all rows returned in TRANSFORM_SUCCESS_OUTPUT
 *  
 *  TRANSFORM_ERROR_COUNT_OUTPUT - (transformation-errors-count)
 *    [Requires MONITORSTEP to be defined]
 *    [TRANS] Returns a count of all rows returned in TRANSFORM_ERROR_OUTPUT
 * 
 * Legitimate inputs:
 *  MONITORSTEP
 *    Takes the name of the step from which success and error rows can be detected
 * 
 *  KETTLELOGLEVEL
 *    Sets the logging level to be used in the EXECUTION_LOG_OUTPUT
 *    Valid settings:
 *      basic
 *      detail
 *      error
 *      debug
 *      minimal
 *      rowlevel
 * 
 */
public class KettleComponent extends ComponentBase implements RowListener {

  private static final long serialVersionUID = 8217343898202366129L;

  private static final String DIRECTORY = "directory"; //$NON-NLS-1$

  private static final String TRANSFORMATION = "transformation"; //$NON-NLS-1$

  private static final String JOB = "job"; //$NON-NLS-1$

  private static final String TRANSFORMFILE = "transformation-file"; //$NON-NLS-1$

  private static final String JOBFILE = "job-file"; //$NON-NLS-1$
  
  //IMPORTSTEP here for backwards compatibility; Superceded by MONITORSTEP
  private static final String IMPORTSTEP = "importstep"; //$NON-NLS-1$
  
  private static final String MONITORSTEP = "monitorstep"; //$NON-NLS-1$
  
  private static final String KETTLELOGLEVEL = "loglevel";
  
  private static final String EXECUTION_STATUS_OUTPUT = "execution-status";
  
  private static final String EXECUTION_LOG_OUTPUT = "execution-log";
  
  private static final String TRANSFORM_SUCCESS_OUTPUT = "transformation-written";
  
  private static final String TRANSFORM_ERROR_OUTPUT = "transformation-errors";
  
  private static final String TRANSFORM_SUCCESS_COUNT_OUTPUT = "transformation-written-count";
  
  private static final String TRANSFORM_ERROR_COUNT_OUTPUT = "transformation-errors-count";
  
  private static final ArrayList<String> outputParams = new ArrayList<String>(Arrays.asList(EXECUTION_STATUS_OUTPUT, EXECUTION_LOG_OUTPUT, TRANSFORM_SUCCESS_OUTPUT, TRANSFORM_ERROR_OUTPUT, TRANSFORM_SUCCESS_COUNT_OUTPUT, TRANSFORM_ERROR_COUNT_OUTPUT));
  

  /**
   * The repositories.xml file location, if empty take the default $HOME/.kettle/repositories.xml
   */
  private String repositoriesXMLFile;

  /** The name of the repository to use */
  private String repositoryName;

  /** The username to login with */
  private String username;

  private MemoryResultSet results;
  
  private MemoryResultSet errorResults;
  
  private String executionStatus;
  
  private String executionLog;

  /** The password to login with */
  private String password;

  private Log4jStringAppender kettleUserAppender;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(KettleComponent.class);
  }

  @Override
  protected boolean validateSystemSettings() {
    // set pentaho.solutionpath so that it can be used in file paths
    boolean useRepository = PentahoSystem
        .getSystemSetting("kettle/settings.xml", "repository.type", "files").equals("rdbms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    if (useRepository) {
      repositoriesXMLFile = PentahoSystem.getSystemSetting("kettle/settings.xml", "repositories.xml.file", null); //$NON-NLS-1$ //$NON-NLS-2$
      repositoryName = PentahoSystem.getSystemSetting("kettle/settings.xml", "repository.name", null); //$NON-NLS-1$ //$NON-NLS-2$
      username = PentahoSystem.getSystemSetting("kettle/settings.xml", "repository.userid", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      password = PentahoSystem.getSystemSetting("kettle/settings.xml", "repository.password", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // Check the Kettle settings...
      if ("".equals(repositoryName) || username.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
        // looks like the Kettle stuff is not configured yet...
        // see if we can provide feedback to the user...

        error(Messages.getErrorString("Kettle.ERROR_0001_SERVER_SETTINGS_NOT_SET")); //$NON-NLS-1$
        return false;
      }

      boolean ok = ((repositoryName != null) && (repositoryName.length() > 0));
      ok = ok || ((username != null) && (username.length() > 0));

      return ok;
    }
    return true;
  }

  @Override
  public boolean init() {
    return true;

  }

  @Override
  public boolean validateAction() {

    if (isDefinedResource(KettleComponent.TRANSFORMFILE) || isDefinedResource(KettleComponent.JOBFILE)) {
      return true;
    }

    boolean useRepository = PentahoSystem
        .getSystemSetting("kettle/settings.xml", "repository.type", "files").equals("rdbms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if (!useRepository) {
      error(Messages.getErrorString("Kettle.ERROR_0019_REPOSITORY_TYPE_FILES")); //$NON-NLS-1$
      return false;
    }

    if (isDefinedInput(KettleComponent.DIRECTORY)
        && (isDefinedInput(KettleComponent.TRANSFORMATION) || isDefinedInput(KettleComponent.JOB))) {
      return true;
    }

    if (!isDefinedInput(KettleComponent.DIRECTORY)) {
      error(Messages.getErrorString("Kettle.ERROR_0002_DIR_OR_FILE__NOT_DEFINED", getActionName())); //$NON-NLS-1$
      return false;
    } else {
      if (!isDefinedInput(KettleComponent.TRANSFORMATION)) {
        error(Messages.getErrorString("Kettle.ERROR_0003_TRANS_NOT_DEFINED", getActionName())); //$NON-NLS-1$
        return false;
      }
    }

    return false;

  }

  /**
   * Execute the specified transformation in the chosen repository.
   */
  @Override
  public boolean executeAction() {

    if (ComponentBase.debug) {
      debug(Messages.getString("Kettle.DEBUG_START")); //$NON-NLS-1$
    }

    TransMeta transMeta = null;
    JobMeta jobMeta = null;
    LogWriter logWriter = null;

    kettleUserAppender = LogWriter.createStringAppender();
    try {
      if (isDefinedInput(KettleComponent.KETTLELOGLEVEL)) {
        String logLevel = getInputStringValue(KettleComponent.KETTLELOGLEVEL);
        
        if(logLevel.equalsIgnoreCase("basic")){
          logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_BASIC); //$NON-NLS-1$
        } else if(logLevel.equalsIgnoreCase("detail")){
          logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_DETAILED); //$NON-NLS-1$
        } else if(logLevel.equalsIgnoreCase("error")){
          logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_ERROR); //$NON-NLS-1$
        } else if(logLevel.equalsIgnoreCase("debug")){
          logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_DEBUG); //$NON-NLS-1$
        } else if(logLevel.equalsIgnoreCase("minimal")){
          logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_MINIMAL); //$NON-NLS-1$
        } else if(logLevel.equalsIgnoreCase("rowlevel")){
          logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_ROWLEVEL); //$NON-NLS-1$
        }
      } else {
        logWriter = LogWriter.getInstance("Kettle-pentaho", false, LogWriter.LOG_LEVEL_NOTHING); //$NON-NLS-1$
      }
    } catch (Throwable t) {

    }

    // this use is now considered obsolete, as we prefer the action-sequence inputs since they
    // now maintain order
    boolean running = true;
    int index = 1;
    ArrayList<String> parameterList = new ArrayList<String>();
    while (running) {
      if (isDefinedInput("parameter" + index)) { //$NON-NLS-1$
        String value = null;
        String inputName = getInputStringValue("parameter" + index); //$NON-NLS-1$
        // see if we have an input with this name
        if (isDefinedInput(inputName)) {
          value = getInputStringValue(inputName);
        }
        parameterList.add(value);
      } else {
        running = false;
      }
      index++;
    }

    // initialize environment variables
    KettleSystemListener.environmentInit(getSession());

    // this is the preferred way to provide inputs to the KetteComponent, the order of inputs is now preserved
    Iterator inputNamesIter = getInputNames().iterator();
    while (inputNamesIter.hasNext()) {
      String name = (String) inputNamesIter.next();
      String value = null;
      if (isDefinedInput(name)) {
        value = getInputStringValue(name);
      }
      if (!parameterList.contains(value)) {
        parameterList.add(value);
      }
    }

    String parameters[] = (String[]) parameterList.toArray(new String[parameterList.size()]);

    String solutionPath = PentahoSystem.getApplicationContext().getFileOutputPath(""); //$NON-NLS-1$
    solutionPath = solutionPath.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
    solutionPath = solutionPath.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$

    Repository repository = connectToRepository(logWriter);
    boolean result = false;
    logWriter.addAppender(kettleUserAppender);
    try {
      if (isDefinedInput(KettleComponent.DIRECTORY)) {
        String directoryName = getInputStringValue(KettleComponent.DIRECTORY);

        if (repository == null) {
          return false;
        }

        if (isDefinedInput(KettleComponent.TRANSFORMATION)) {
          String transformationName = getInputStringValue(KettleComponent.TRANSFORMATION);
          transMeta = loadTransformFromRepository(directoryName, transformationName, repository, logWriter);
          if (transMeta != null) {
            transMeta.setArguments(parameters);
          } else {
            return false;
          }
        } else if (isDefinedInput(KettleComponent.JOB)) {
          String jobName = getInputStringValue(KettleComponent.JOB);
          jobMeta = loadJobFromRepository(directoryName, jobName, repository, logWriter);
          if (jobMeta != null) {
            jobMeta.setArguments(parameters);
          } else {
            return false;
          }
        }
      } else if (isDefinedResource(KettleComponent.TRANSFORMFILE)) {
        IActionSequenceResource transformResource = getResource(KettleComponent.TRANSFORMFILE);
        String fileAddress = getActualFileName(transformResource);

        try {
          if (fileAddress != null) { // We have an actual loadable filesystem and file
            transMeta = new TransMeta(fileAddress, repository, true);
            transMeta.setFilename(fileAddress);
          } else {
            String jobXmlStr = getResourceAsString(getResource(KettleComponent.TRANSFORMFILE));
            jobXmlStr = jobXmlStr.replaceAll("\\$\\{pentaho.solutionpath\\}", solutionPath); //$NON-NLS-1$
            jobXmlStr = jobXmlStr.replaceAll("\\%\\%pentaho.solutionpath\\%\\%", solutionPath); //$NON-NLS-1$
            org.w3c.dom.Document doc = XmlW3CHelper.getDomFromString(jobXmlStr);
            // create a tranformation from the document
            transMeta = new TransMeta(doc.getFirstChild(), repository);
          }
        } catch (Exception e) {
          error(
              Messages.getErrorString("Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.TRANSFORMFILE, fileAddress), e); //$NON-NLS-1$
          return false;
        }

        if (transMeta == null) {
          error(Messages.getErrorString("Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.TRANSFORMFILE, fileAddress)); //$NON-NLS-1$
          debug(kettleUserAppender.getBuffer().toString());
          return false;
        } else { // Don't forget to set the parameters here as well...
          transMeta.setArguments(parameters);
          /* 
           * We do not need to concatenate the solutionPath info as the fileAddress has the complete location of the file
           * from start to end. This is to resolve BISERVER-502.
           */
          transMeta.setFilename(fileAddress);
        }
      } else if (isDefinedResource(KettleComponent.JOBFILE)) {
        String fileAddress = ""; //$NON-NLS-1$
        try {
          fileAddress = getResource(KettleComponent.JOBFILE).getAddress();
          String jobXmlStr = getResourceAsString(getResource(KettleComponent.JOBFILE));
          //         String jobXmlStr = XmlW3CHelper.getContentFromSolutionResource(fileAddress);
          jobXmlStr = jobXmlStr.replaceAll("\\$\\{pentaho.solutionpath\\}", solutionPath); //$NON-NLS-1$
          jobXmlStr = jobXmlStr.replaceAll("\\%\\%pentaho.solutionpath\\%\\%", solutionPath); //$NON-NLS-1$
          org.w3c.dom.Document doc = XmlW3CHelper.getDomFromString(jobXmlStr);
          if (doc == null) {
            error(Messages.getErrorString("Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.JOBFILE, fileAddress)); //$NON-NLS-1$
            debug(kettleUserAppender.getBuffer().toString());
            return false;
          }
          // create a job from the document
          try {
            repository = connectToRepository(logWriter);
            // if we get a valid repository its great, if not try it without
            jobMeta = new JobMeta(logWriter, doc.getFirstChild(), repository, null);
            jobMeta.setFilename(solutionPath + fileAddress);
          } catch (Exception e) {
            error(Messages.getString("Kettle.ERROR_0023_NO_META"), e); //$NON-NLS-1$
          } finally {
            if (repository != null) {
              if (ComponentBase.debug) {
                debug(Messages.getString("Kettle.DEBUG_DISCONNECTING")); //$NON-NLS-1$
              }
              repository.disconnect();
            }
          }
        } catch (Exception e) {
          error(Messages.getErrorString("Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.JOBFILE, fileAddress), e); //$NON-NLS-1$
          return false;
        }
        if (jobMeta == null) {
          error(Messages.getErrorString("Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.JOBFILE, fileAddress)); //$NON-NLS-1$
          debug(kettleUserAppender.getBuffer().toString());
          return false;
        } else {
          jobMeta.setArguments(parameters);
          jobMeta.setFilename(solutionPath + fileAddress);
        }

      }

      // OK, we have the information, let's load and execute the
      // transformation or job

      if (transMeta != null) {
        result = executeTransformation(transMeta, logWriter);
      }
      if (jobMeta != null) {
        result = executeJob(jobMeta, repository, logWriter);
      }

    } finally {
      logWriter.removeAppender(kettleUserAppender);
      if (repository != null) {
        if (ComponentBase.debug) {
          debug(Messages.getString("Kettle.DEBUG_DISCONNECTING")); //$NON-NLS-1$
        }
        repository.disconnect();
      }
    }
    
    if(isDefinedOutput(EXECUTION_LOG_OUTPUT)){
      setOutputValue(EXECUTION_LOG_OUTPUT, executionLog);
    }
    
    if(isDefinedOutput(EXECUTION_STATUS_OUTPUT)){
      setOutputValue(EXECUTION_STATUS_OUTPUT, executionStatus);
    }
    
    XMLHandlerCache.getInstance().clear();
    return result;

  }

  private String getActualFileName(final IActionSequenceResource resource) {
    String fileAddress = null;

    // Is it a hardcoded path?
    if ((resource.getSourceType() == IActionSequenceResource.FILE_RESOURCE)) {
      fileAddress = resource.getAddress();
    }
    // Is it a solution relative path?
    else if (resource.getSourceType() == IActionSequenceResource.SOLUTION_FILE_RESOURCE) {
      fileAddress = PentahoSystem.getApplicationContext().getSolutionPath(resource.getAddress());
    }

    // Can it be loaded? this may not be true if using the DB Based repos
    if (fileAddress != null) {
      File file = new File(fileAddress);
      if (!file.exists() || !file.isFile()) {
        fileAddress = null;
      }
    }
    return (fileAddress);
  }

  protected boolean customizeTrans( Trans trans, LogWriter logWriter ) {
	  // override this to customize the transformation before it runs
	  // by default there is no transformation
	  return true;
  }
  
  private boolean executeTransformation(final TransMeta transMeta, final LogWriter logWriter) {
    boolean success = true;
    Trans trans = null;
    try {
      if (transMeta != null) {
        try {
          trans = new Trans(transMeta);
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0010_BAD_TRANSFORMATION_METADATA"), t); //$NON-NLS-1$
          
          extractKettleStatus(trans);
          extractKettleLog();

          return false;
        }

      }
      if (trans == null) {
        error(Messages.getErrorString("Kettle.ERROR_0010_BAD_TRANSFORMATION_METADATA")); //$NON-NLS-1$
        error(kettleUserAppender.getBuffer().toString());
        
        extractKettleStatus(trans);
        extractKettleLog();

        return false;
      }
      if (trans != null) {
        // OK, we have the transformation, now run it!
    	if( !customizeTrans( trans, logWriter ) ) {
    		// the customization function says we should bail
    		// TODO throw an error
    	  
        extractKettleStatus(trans);
        extractKettleLog();

    		return false;
    	}
        if (ComponentBase.debug) {
          debug(Messages.getString("Kettle.DEBUG_PREPARING_TRANSFORMATION")); //$NON-NLS-1$
        }
        try {
          trans.prepareExecution(transMeta.getArguments());
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0011_TRANSFORMATION_PREPARATION_FAILED"), t); //$NON-NLS-1$
          
          extractKettleStatus(trans);
          extractKettleLog();
          
          return false;
        }

        String stepName = null;
        String outputName = null;
        if (ComponentBase.debug) {
          debug(Messages.getString("Kettle.DEBUG_FINDING_STEP_IMPORTER")); //$NON-NLS-1$
        }
        
        //Supporting "importstep" for backwards compatibility
        if (isDefinedInput(KettleComponent.IMPORTSTEP)) {
          try {
            // get the name of the step that we are going to listen
            // to
            stepName = getInputStringValue(KettleComponent.IMPORTSTEP);
            if (getOutputNames().size() == 1) {
              outputName = (String) getOutputNames().iterator().next();
            } else {
              // Need to find the name that does not match one of the predefined output parameters
              outputName = getUndefinedOutputParameter(getOutputNames().iterator());
            }
            boolean foundStep = false;
            if ((stepName != null) && (outputName != null)) {
              List<StepMetaDataCombi> stepList = trans.getSteps();
              // find the specified step
              for (int stepNo = 0; stepNo < stepList.size(); stepNo++) {
                // get the next step
                StepMetaDataCombi step = (StepMetaDataCombi) stepList.get(stepNo);
                if (step.stepname.equals(stepName)) {
                  if (ComponentBase.debug) {
                    debug(Messages.getString("Kettle.DEBUG_FOUND_STEP_IMPORTER")); //$NON-NLS-1$
                  }
                  // this is the step we are looking for
                  if (ComponentBase.debug) {
                    debug(Messages.getString("Kettle.DEBUG_GETTING_STEP_METADATA")); //$NON-NLS-1$
                  }
                  RowMetaInterface row = transMeta.getStepFields(stepName);
                  // create the metadata that the Pentaho
                  // result set needs
                  String fieldNames[] = row.getFieldNames();
                  String columns[][] = new String[1][fieldNames.length];
                  for (int column = 0; column < fieldNames.length; column++) {
                    columns[0][column] = fieldNames[column];
                  }
                  if (ComponentBase.debug) {
                    debug(Messages.getString("Kettle.DEBUG_CREATING_RESULTSET_METADATA")); //$NON-NLS-1$
                  }

                  MemoryMetaData metaData = new MemoryMetaData(columns, null);
                  results = new MemoryResultSet(metaData);
                  // add ourself as a row listener
                  step.step.addRowListener(this);
                  foundStep = true;
                  break;
                }
              }
            }
            if (!foundStep) {
              error(Messages.getErrorString("Kettle.ERROR_0012_ROW_LISTENER_CREATE_FAILED")); //$NON-NLS-1$
            }
          } catch (Exception e) {
            error(Messages.getErrorString("Kettle.ERROR_0012_ROW_LISTENER_CREATE_FAILED"), e); //$NON-NLS-1$
            
            extractKettleStatus(trans);
            extractKettleLog();

            return false;
          }
        } else {
          //New method that supports specified output variables only
          //MONITORSTEP supports error row detection as well
          
          if(isDefinedInput(KettleComponent.MONITORSTEP)){
            try {
              // get the name of the step that we are going to listen
              // to
              stepName = getInputStringValue(KettleComponent.MONITORSTEP);
              boolean foundStep = false;
              if (stepName != null) {
                List<StepMetaDataCombi> stepList = trans.getSteps();
                // find the specified step
                for (int stepNo = 0; stepNo < stepList.size(); stepNo++) {
                  // get the next step
                  StepMetaDataCombi step = (StepMetaDataCombi) stepList.get(stepNo);
                  if (step.stepname.equals(stepName)) {
                    if (ComponentBase.debug) {
                      debug(Messages.getString("Kettle.DEBUG_FOUND_STEP_IMPORTER")); //$NON-NLS-1$
                    }
                    // this is the step we are looking for
                    if (ComponentBase.debug) {
                      debug(Messages.getString("Kettle.DEBUG_GETTING_STEP_METADATA")); //$NON-NLS-1$
                    }
                    RowMetaInterface row = transMeta.getStepFields(stepName);
                    
                    // create the metadata that the Pentaho
                    // result set needs
                    String fieldNames[] = row.getFieldNames();
                    String columns[][] = new String[1][fieldNames.length];
                    for (int column = 0; column < fieldNames.length; column++) {
                      columns[0][column] = fieldNames[column];
                    }
                    if (ComponentBase.debug) {
                      debug(Messages.getString("Kettle.DEBUG_CREATING_RESULTSET_METADATA")); //$NON-NLS-1$
                    }

                    MemoryMetaData metaData = new MemoryMetaData(columns, null);
                    results = new MemoryResultSet(metaData);
                    errorResults = new MemoryResultSet(metaData);
                    
                    // add ourself as a row listener
                    step.step.addRowListener(this);
                    foundStep = true;
                    break;
                  }
                }
              }
              if (!foundStep) {
                error(Messages.getErrorString("Kettle.ERROR_0012_ROW_LISTENER_CREATE_FAILED")); //$NON-NLS-1$
              }
            } catch (Exception e) {
              error(Messages.getErrorString("Kettle.ERROR_0012_ROW_LISTENER_CREATE_FAILED"), e); //$NON-NLS-1$
              
              extractKettleStatus(trans);
              extractKettleLog();

              return false;
            }
          }
        }

        try {
          if (ComponentBase.debug) {
            debug(Messages.getString("Kettle.DEBUG_STARTING_TRANSFORMATION")); //$NON-NLS-1$
          }
          trans.startThreads();
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0013_TRANSFORMATION_START_FAILED"), t); //$NON-NLS-1$
          
          extractKettleStatus(trans);
          extractKettleLog();

          return false;
        }

        try {
          // It's running in a separate tread to allow monitoring,
          // etc.
          if (ComponentBase.debug) {
            debug(Messages.getString("Kettle.DEBUG_TRANSFORMATION_RUNNING")); //$NON-NLS-1$
          }
          trans.waitUntilFinished();
          trans.endProcessing("end");
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0014_ERROR_DURING_EXECUTE")); //$NON-NLS-1$
          
          extractKettleStatus(trans);
          extractKettleLog();

          return false;
        }
        // Dump the Kettle log...
        debug(kettleUserAppender.getBuffer().toString());
        if (outputName != null) {
          // Support original IMPORTSTEP method
          if(results != null) {
            if (ComponentBase.debug) {
              debug(Messages.getString("Kettle.DEBUG_SETTING_OUTPUT")); //$NON-NLS-1$
            }
            setOutputValue(outputName, results);
          }
        } else {
          // MONITOR method output
          if(results != null){
            if(isDefinedOutput(TRANSFORM_SUCCESS_OUTPUT)){
              setOutputValue(TRANSFORM_SUCCESS_OUTPUT, results);
            }
            if(isDefinedOutput(TRANSFORM_SUCCESS_COUNT_OUTPUT)){
              setOutputValue(TRANSFORM_SUCCESS_COUNT_OUTPUT, results.getRowCount());
            }
          }
          if(errorResults != null){
            if(isDefinedOutput(TRANSFORM_ERROR_OUTPUT)){
              setOutputValue(TRANSFORM_ERROR_OUTPUT, errorResults);
            }
            if(isDefinedOutput(TRANSFORM_ERROR_COUNT_OUTPUT)){
              setOutputValue(TRANSFORM_ERROR_COUNT_OUTPUT, errorResults.getRowCount());
            }
          }          
        }
      }
    } catch (Exception e) {
      error(Messages.getErrorString("Kettle.ERROR_0008_ERROR_RUNNING", e.toString()), e); //$NON-NLS-1$
      success = false;
    }
    //     NOT REQUIRED FOR PDI 3
    //finally {
    //      if (trans != null) {
    //        LocalVariables.getInstance().removeKettleVariables(Thread.currentThread().getName());
    //      }
    //    }
    
    extractKettleStatus(trans);
    extractKettleLog();
    
    return success;

  }
  
  private void extractKettleStatus(Trans trans){
    if(trans != null) {
      executionStatus = trans.getStatus();
    } else {
      executionStatus = "Transformation is not loaded";
    }      
  }
  
  private void extractKettleStatus(Job job){
    if(job != null) {
      executionStatus = job.getStatus();
    } else {
      executionStatus = "Job is not loaded";
    }      
  }
  
  private void extractKettleLog(){
    executionLog = kettleUserAppender.getBuffer().toString();
  }

  private String getUndefinedOutputParameter(Iterator<String> outputNames) {
    String tempName = null;
    while(outputNames.hasNext()){
      tempName = (String) outputNames.next();      
      if(!outputParams.contains(tempName)){
        //Found user defined named
        return(tempName);
      }
    }
    return null;
  }

  private boolean executeJob(final JobMeta jobMeta, final Repository repository, final LogWriter logWriter) {
    boolean success = true;
    Job job = null;
    try {
      if (jobMeta != null) {
        try {
          job = new Job(logWriter, StepLoader.getInstance(), repository, jobMeta);
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0021_BAD_JOB_METADATA"), t); //$NON-NLS-1$
          
          extractKettleStatus(job);
          extractKettleLog();
          
          return false;
        }

      }
      if (job == null) {
        error(Messages.getErrorString("Kettle.ERROR_0021_BAD_JOB_METADATA")); //$NON-NLS-1$
        debug(kettleUserAppender.getBuffer().toString());
        
        extractKettleStatus(job);
        extractKettleLog();
        
        return false;
      }
      if (job != null) {
        try {
          if (ComponentBase.debug) {
            debug(Messages.getString("Kettle.DEBUG_STARTING_JOB")); //$NON-NLS-1$
          }
          job.start();
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0022_JOB_START_FAILED"), t); //$NON-NLS-1$
          
          extractKettleStatus(job);
          extractKettleLog();
          
          return false;
        }

        try {
          // It's running in a separate tread to allow monitoring,
          // etc.
          if (ComponentBase.debug) {
            debug(Messages.getString("Kettle.DEBUG_JOB_RUNNING")); //$NON-NLS-1$
          }
          job.waitUntilFinished(5000000);
          job.endProcessing("end", job.getResult()); //$NON-NLS-1$
          if ((job.getErrors() > 0) || (job.getResult().getNrErrors() > 0)) {
            error(Messages.getErrorString("Kettle.ERROR_0014_ERROR_DURING_EXECUTE")); //$NON-NLS-1$
            debug(kettleUserAppender.getBuffer().toString());
            
            extractKettleStatus(job);
            extractKettleLog();
            
            return false;
          }
        } catch (Throwable t) {
          error(Messages.getErrorString("Kettle.ERROR_0014_ERROR_DURING_EXECUTE"), t); //$NON-NLS-1$
          
          extractKettleStatus(job);
          extractKettleLog();
          
          return false;
        }
        // Dump the Kettle log...
        debug(kettleUserAppender.getBuffer().toString());
      }
    } catch (Exception e) {
      error(Messages.getErrorString("Kettle.ERROR_0008_ERROR_RUNNING", e.toString()), e); //$NON-NLS-1$
      success = false;
    }
    //    finally {
    //      if (job != null) {
    //        LocalVariables.getInstance().removeKettleVariables(Thread.currentThread().getName());
    //      }
    //    }
    
    extractKettleStatus(job);
    extractKettleLog();
    
    return success;

  }

  private TransMeta loadTransformFromRepository(final String directoryName, final String transformationName,
      final Repository repository, final LogWriter logWriter) {
    if (ComponentBase.debug) {
      debug(Messages.getString("Kettle.DEBUG_DIRECTORY", directoryName)); //$NON-NLS-1$
    }
    if (ComponentBase.debug) {
      debug(Messages.getString("Kettle.DEBUG_TRANSFORMATION", transformationName)); //$NON-NLS-1$
    }
    TransMeta transMeta = null;
    try {

      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_FINDING_DIRECTORY")); //$NON-NLS-1$
      }

      // Find the directory specified.
      RepositoryDirectory repositoryDirectory = null;
      try {
        repositoryDirectory = repository.getDirectoryTree().findDirectory(directoryName);
      } catch (Throwable t) {
        error(Messages.getErrorString("Kettle.ERROR_0006_DIRECTORY_NOT_FOUND", directoryName), t); //$NON-NLS-1$
        return null;
      }
      if (repositoryDirectory == null) {
        error(Messages.getErrorString("Kettle.ERROR_0006_DIRECTORY_NOT_FOUND", directoryName)); //$NON-NLS-1$
        return null;
      }

      if (repositoryDirectory != null) {
        if (ComponentBase.debug) {
          debug(Messages.getString("Kettle.DEBUG_GETTING_TRANSFORMATION_METADATA")); //$NON-NLS-1$
        }

        try {
          // Load the transformation from the repository
          transMeta = new TransMeta(repository, transformationName, repositoryDirectory);
        } catch (Throwable t) {
          error(Messages.getErrorString(
              "Kettle.ERROR_0009_TRANSFROMATION_METADATA_NOT_FOUND", repositoryDirectory + "/" + transformationName), t); //$NON-NLS-1$ //$NON-NLS-2$
          return null;
        }
        if (transMeta == null) {
          error(Messages.getErrorString(
              "Kettle.ERROR_0009_TRANSFROMATION_METADATA_NOT_FOUND", repositoryDirectory + "/" + transformationName)); //$NON-NLS-1$ //$NON-NLS-2$
          debug(kettleUserAppender.getBuffer().toString());
          return null;
        } else {
          return transMeta;
        }
      }

      if (ComponentBase.debug) {
        debug(kettleUserAppender.getBuffer().toString());
        // OK, close shop!
      }

    } catch (Throwable e) {
      error(Messages.getErrorString("Kettle.ERROR_0008_ERROR_RUNNING", e.toString()), e); //$NON-NLS-1$
    }
    return null;
  }

  private JobMeta loadJobFromRepository(final String directoryName, final String jobName, final Repository repository,
      final LogWriter logWriter) {
    if (ComponentBase.debug) {
      debug(Messages.getString("Kettle.DEBUG_DIRECTORY", directoryName)); //$NON-NLS-1$
    }
    if (ComponentBase.debug) {
      debug(Messages.getString("Kettle.DEBUG_JOB", jobName)); //$NON-NLS-1$
    }
    JobMeta jobMeta = null;
    try {

      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_FINDING_DIRECTORY")); //$NON-NLS-1$
      }

      // Find the directory specified.
      RepositoryDirectory repositoryDirectory = null;
      try {
        repositoryDirectory = repository.getDirectoryTree().findDirectory(directoryName);
      } catch (Throwable t) {
        error(Messages.getErrorString("Kettle.ERROR_0006_DIRECTORY_NOT_FOUND", directoryName), t); //$NON-NLS-1$
        return null;
      }
      if (repositoryDirectory == null) {
        error(Messages.getErrorString("Kettle.ERROR_0006_DIRECTORY_NOT_FOUND", directoryName)); //$NON-NLS-1$
        return null;
      }

      if (repositoryDirectory != null) {
        if (ComponentBase.debug) {
          debug(Messages.getString("Kettle.DEBUG_GETTING_JOB_METADATA")); //$NON-NLS-1$
        }

        try {
          // Load the transformation from the repository
          jobMeta = new JobMeta(logWriter, repository, jobName, repositoryDirectory);
        } catch (Throwable t) {
          error(Messages
              .getErrorString("Kettle.ERROR_0020_JOB_METADATA_NOT_FOUND", repositoryDirectory + "/" + jobName), t); //$NON-NLS-1$ //$NON-NLS-2$
          return null;
        }
        if (jobMeta == null) {
          error(Messages
              .getErrorString("Kettle.ERROR_0020_JOB_METADATA_NOT_FOUND", repositoryDirectory + "/" + jobName)); //$NON-NLS-1$ //$NON-NLS-2$
          debug(kettleUserAppender.getBuffer().toString());
          return null;
        } else {
          return jobMeta;
        }
      }

      if (ComponentBase.debug) {
        debug(kettleUserAppender.getBuffer().toString());
        // OK, close shop!
      }

    } catch (Throwable e) {
      error(Messages.getErrorString("Kettle.ERROR_0008_ERROR_RUNNING", e.toString()), e); //$NON-NLS-1$
    }
    return null;
  }

  private Repository connectToRepository(final LogWriter logWriter) {
    boolean useRepository = PentahoSystem
        .getSystemSetting("kettle/settings.xml", "repository.type", "files").equals("rdbms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if (!useRepository) {
      return null;
    }

    try {
      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_META_REPOSITORY")); //$NON-NLS-1$
      }

      RepositoriesMeta repositoriesMeta = null;
      try {
        repositoriesMeta = new RepositoriesMeta(logWriter);
      } catch (Throwable t) {
        error(Messages.getErrorString("Kettle.ERROR_0007_BAD_META_REPOSITORY"), t); //$NON-NLS-1$
        return null;
      }
      if (repositoriesMeta == null) {
        error(Messages.getErrorString("Kettle.ERROR_0007_BAD_META_REPOSITORY")); //$NON-NLS-1$
        debug(kettleUserAppender.getBuffer().toString());
        return null;
      }

      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_POPULATING_META")); //$NON-NLS-1$
      }
      try {
        // TODO: add support for specified repositories.xml files...
        repositoriesMeta.readData(); // Read from the default $HOME/.kettle/repositories.xml file.
      } catch (Throwable t) {
        error(Messages.getErrorString("Kettle.ERROR_0018_META_REPOSITORY_NOT_POPULATED"), t); //$NON-NLS-1$
        return null;
      }
      if ((repositoriesXMLFile != null) && !"".equals(repositoriesXMLFile)) //$NON-NLS-1$
      {
        error(Messages.getErrorString("Kettle.ERROR_0017_XML_REPOSITORY_NOT_SUPPORTED")); //$NON-NLS-1$
        debug(kettleUserAppender.getBuffer().toString());
        return null;
      }

      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_FINDING_REPOSITORY")); //$NON-NLS-1$
      }
      // Find the specified repository.
      RepositoryMeta repositoryMeta = null;
      try {
        repositoryMeta = repositoriesMeta.findRepository(repositoryName);
      } catch (Throwable t) {
        error(Messages.getErrorString("Kettle.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName), t); //$NON-NLS-1$
        return null;
      }

      if (repositoryMeta == null) {
        error(Messages.getErrorString("Kettle.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName)); //$NON-NLS-1$
        debug(kettleUserAppender.getBuffer().toString());
        return null;
      }

      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_GETTING_REPOSITORY")); //$NON-NLS-1$
      }
      Repository repository = null;
      UserInfo userInfo = null;
      try {
        repository = new Repository(logWriter, repositoryMeta, userInfo);
      } catch (Throwable t) {
        error(Messages.getErrorString("Kettle.ERROR_0016_COULD_NOT_GET_REPOSITORY_INSTANCE"), t); //$NON-NLS-1$
        return null;
      }

      // OK, now try the username and password
      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_CONNECTING")); //$NON-NLS-1$
      }
      if (repository.connect(getClass().getName())) {
        try {
          userInfo = new UserInfo(repository, username, password);
        } catch (KettleException e) {
          userInfo = null;
        } finally {
        }
      } else {
        error(Messages.getErrorString("Kettle.ERROR_0005_LOGIN_FAILED")); //$NON-NLS-1$
        debug(kettleUserAppender.getBuffer().toString());
        return null;
      }

      // OK, the repository is open and ready to use.
      if (ComponentBase.debug) {
        debug(Messages.getString("Kettle.DEBUG_FINDING_DIRECTORY")); //$NON-NLS-1$
      }

      return repository;

    } catch (Throwable e) {
      error(Messages.getErrorString("Kettle.ERROR_0008_ERROR_RUNNING", e.toString()), e); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  public void done() {

  }

  public void rowReadEvent(final RowMetaInterface row, final Object[] values) {
  }

  public void rowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row) throws KettleStepException {

    if (results == null) {
      return;
    }
    try {
      Object pentahoRow[] = new Object[results.getColumnCount()];
      for (int columnNo = 0; columnNo < results.getColumnCount(); columnNo++) {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta(columnNo);

        switch (valueMeta.getType()) {
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoRow[columnNo] = rowMeta.getBigNumber(row, columnNo);
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoRow[columnNo] = rowMeta.getBoolean(row, columnNo);
            break;
          case ValueMetaInterface.TYPE_DATE:
            pentahoRow[columnNo] = rowMeta.getDate(row, columnNo);
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoRow[columnNo] = rowMeta.getInteger(row, columnNo);
            break;
          case ValueMetaInterface.TYPE_NONE:
            pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoRow[columnNo] = rowMeta.getNumber(row, columnNo);
            break;
          case ValueMetaInterface.TYPE_STRING:
            pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
            break;
          default:
            pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
        }
      }
      results.addRow(pentahoRow);
    } catch (KettleValueException e) {
      throw new KettleStepException(e);
    }
  }

  public void errorRowWrittenEvent(final RowMetaInterface rowMeta, final Object[] row) throws KettleStepException {
    
    //Only support for MONITORSTEP (IMPORTSTEP is for backwards compatibility and does not support error rows)
    if(isDefinedInput(KettleComponent.MONITORSTEP)){
      if (errorResults == null) {
        return;
      }
      try {
        Object pentahoRow[] = new Object[errorResults.getColumnCount()];
        for (int columnNo = 0; columnNo < errorResults.getColumnCount(); columnNo++) {
          ValueMetaInterface valueMeta = rowMeta.getValueMeta(columnNo);
  
          switch (valueMeta.getType()) {
            case ValueMetaInterface.TYPE_BIGNUMBER:
              pentahoRow[columnNo] = rowMeta.getBigNumber(row, columnNo);
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              pentahoRow[columnNo] = rowMeta.getBoolean(row, columnNo);
              break;
            case ValueMetaInterface.TYPE_DATE:
              pentahoRow[columnNo] = rowMeta.getDate(row, columnNo);
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              pentahoRow[columnNo] = rowMeta.getInteger(row, columnNo);
              break;
            case ValueMetaInterface.TYPE_NONE:
              pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              pentahoRow[columnNo] = rowMeta.getNumber(row, columnNo);
              break;
            case ValueMetaInterface.TYPE_STRING:
              pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
              break;
            default:
              pentahoRow[columnNo] = rowMeta.getString(row, columnNo);
          }
        }
        errorResults.addRow(pentahoRow);
      } catch (KettleValueException e) {
        throw new KettleStepException(e);
      }
    }
  }
  
}
