/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
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
 * @created Jun 29, 2007 
 * @author wseyler
 */

package org.pentaho.platform.plugin.action.mondrian;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * Utility class used to save an analysis action sequence from a 
 * JPivot view. 
 */
public class AnalysisSaver extends PentahoMessenger {
  private static final long serialVersionUID = 6290291421129174060L;

  private static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

  private static final String ATTRIBUTE_STRING = "string"; //$NON-NLS-1$

  private static final String TITLE_NODE_NAME = "title"; //$NON-NLS-1$

  public static final String SUFFIX = ".xaction"; //$NON-NLS-1$

  public static final String PROPERTIES_SUFFIX = ".properties"; //$NON-NLS-1$

  private static Log logger = null;

  /* (non-Javadoc)
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return AnalysisSaver.logger;
  }

  public static int saveAnalysis(final IPentahoSession session, final HashMap props, final String path,
      String fileName, final boolean overwrite) {
	  
    if ("true".equals(PentahoSystem.getSystemSetting("hosted-demo-mode", "false"))) {
	  throw new RuntimeException("Save is disabled.");
	}
	  
    int result = 0;
    try {
      AnalysisSaver.logger = LogFactory.getLog(AnalysisSaver.class);
      String baseUrl = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
      ISolutionRepository solutionRepository = PentahoSystem.getSolutionRepository(session);

      // We will (at this point in time) always have an original action sequence to start from...
      String originalActionReference = (String) props.get("actionreference"); //$NON-NLS-1$

      if (originalActionReference == null) {
        throw new MissingParameterException(Messages
            .getErrorString("ANALYSISSAVER.ERROR_0001_MISSING_ACTION_REFERENCE")); //$NON-NLS-1$
      }

      org.dom4j.Document document = solutionRepository.getResourceAsDocument(originalActionReference);

      // Update the document with the stuff we passed in on the props
      document = AnalysisSaver.updateDocument(document, props);
      System.out.println(document.asXML());
      fileName = fileName.endsWith(AnalysisSaver.SUFFIX) ? fileName : fileName + AnalysisSaver.SUFFIX;
      result = solutionRepository.addSolutionFile(baseUrl, path, fileName, document.asXML().getBytes(), overwrite);

      // Now save the resource files
      ActionInfo actionInfo = ActionInfo.parseActionString(originalActionReference);
      String originalPath = actionInfo.getSolutionName() + "/" + actionInfo.getPath(); //$NON-NLS-1$
      String originalFileName = actionInfo.getActionName();
      originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf(AnalysisSaver.SUFFIX));
      ISolutionFile[] parentFiles = solutionRepository.getFileByPath(originalPath).listFiles();
      String baseFileName = fileName.substring(0, fileName.lastIndexOf(AnalysisSaver.SUFFIX));
      for (ISolutionFile aSolutionFile : parentFiles) {
        if (!aSolutionFile.isDirectory() && aSolutionFile.getFileName().startsWith(originalFileName)
            && aSolutionFile.getFileName().toLowerCase().endsWith(AnalysisSaver.PROPERTIES_SUFFIX)) {
          String newFileName = aSolutionFile.getFileName().replaceFirst(originalFileName, baseFileName);
          result = result
              & solutionRepository.addSolutionFile(baseUrl, path, newFileName, aSolutionFile.getData(), overwrite);
        }
      }

      solutionRepository.resetRepository();
    } catch (Exception e) {
      AnalysisSaver.logger.error(Messages.getErrorString("ANALYSISSAVER.ERROR_0000_UNKNOWN"), e); //$NON-NLS-1$
      result = ISolutionRepository.FILE_ADD_FAILED;
    }

    return result;
  }

  /**
   * @param document
   * @param props
   * @return
   */
  private static Document updateDocument(final Document document, final HashMap props) {
    try {
      Element componentDefinition = null;
      Element actionOutput = null;
      Element actionSequenceOutput = null;

      Node actionSequence = document.selectSingleNode("/action-sequence"); //$NON-NLS-1$
      if (actionSequence == null) {
        throw new InvalidDocumentException(Messages.getErrorString("ANALYSISSAVER.ERROR_0004_INVALID_ORIGIN_DOCUMENT")); //$NON-NLS-1$
      }

      Node title = null;
      String propertyTitle = (String) props.get(AnalysisSaver.TITLE_NODE_NAME);
      title = document.selectSingleNode("action-sequence/title"); //$NON-NLS-1$
      if ((propertyTitle != null) && (title != null)) {
        title.setText(propertyTitle);
      }

      // Next, we need to retrieve the PivotViewComponent action and
      // process/update it.. there could popssibly be more than one
      // PivotViewComponent in an action sequence, however, we have no idea
      // how to figure out which one to process, so we default to picking the last one we found. 

      componentDefinition = (Element) document
          .selectSingleNode("//action-definition[component-name='PivotViewComponent']/component-definition"); //$NON-NLS-1$
      if (componentDefinition == null) {
        throw new InvalidDocumentException(Messages.getErrorString("ANALYSISSAVER.ERROR_0005_INVALID_NO_PIVOT_ACTION")); //$NON-NLS-1$
      }

      AnalysisSaver.updateComponent(componentDefinition, props);

      // Get the action's root action-output node, in case we need to add the 
      // appropriate outputs for the pivot view...
      actionOutput = (Element) document
          .selectSingleNode("//action-definition[component-name='PivotViewComponent']/action-outputs"); //$NON-NLS-1$
      AnalysisSaver.updateOutput(actionOutput, props);

      // Get the action's root action sequence output node, in case we need to add the 
      // appropriate outputs for the pivot view...
      actionSequenceOutput = (Element) document.selectSingleNode("//action-sequence/outputs"); //$NON-NLS-1$
      AnalysisSaver.updateOutput(actionSequenceOutput, props);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return document;
  }

  /**
   * @param componentDefinition
   * @param props
   */
  private static void updateComponent(final Element componentDefinition, final HashMap props) {
    Iterator iter = props.keySet().iterator();

    while (iter.hasNext()) {
      Object key = iter.next();
      Node node = componentDefinition.selectSingleNode(key.toString());
      if (node == null) {
        node = componentDefinition.addElement(key.toString());
      }
      if (PivotViewComponent.OPTIONS.equals(node.getName())) {
        List optionsList = (List) props.get(key);
        Iterator optsIter = optionsList.iterator();
        while (optsIter.hasNext()) {
          String anOption = optsIter.next().toString();
          Node anOptionNode = node.selectSingleNode(anOption);
          if (anOptionNode == null) {
            ((Element) node).addElement(anOption);
          }
        }
      } else {
        Object value = props.get(key);
        if (value != null) {
          node.setText(value.toString());
        }
      }
    }

    String mdxValue = componentDefinition.selectSingleNode("mdx").getText();//$NON-NLS-1$
    componentDefinition.selectSingleNode("query").setText(mdxValue);//$NON-NLS-1$
  }

  /**
   * @param outputNode
   * @param props
   */
  private static void updateOutput(final Element outputNode, final HashMap props) {
    Iterator iter = props.keySet().iterator();

    while (iter.hasNext()) {
      Object key = iter.next();
      Node node = outputNode.selectSingleNode(key.toString());
      if (node == null) {
        outputNode.addElement(key.toString()).addAttribute(AnalysisSaver.ATTRIBUTE_TYPE,
            "options".equals(key.toString()) ? "list" : AnalysisSaver.ATTRIBUTE_STRING);//$NON-NLS-1$//$NON-NLS-2$
      }
    }
  }
}
