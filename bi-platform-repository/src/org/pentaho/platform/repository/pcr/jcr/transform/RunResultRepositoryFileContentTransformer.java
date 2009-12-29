package org.pentaho.platform.repository.pcr.jcr.transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.pcr.RunResultRepositoryFileContent;
import org.pentaho.platform.repository.pcr.SimpleRepositoryFileContent;
import org.pentaho.platform.repository.pcr.jcr.ITransformer;
import org.pentaho.platform.repository.pcr.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository.pcr.jcr.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;

public class RunResultRepositoryFileContentTransformer implements ITransformer<RunResultRepositoryFileContent> {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RunResultRepositoryFileContentTransformer.class);

  // ~ Instance fields =================================================================================================

  private SimpleRepositoryFileContentTransformer simpleTransformer;

  // ~ Constructors ====================================================================================================

  public RunResultRepositoryFileContentTransformer(final SimpleRepositoryFileContentTransformer simpleTransformer) {
    super();
    this.simpleTransformer = simpleTransformer;
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public RunResultRepositoryFileContent fromContentNode(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final NodeIdStrategy nodeIdStrategy, final Node resourceNode)
      throws RepositoryException, IOException {
    SimpleRepositoryFileContent simpleContent = simpleTransformer.fromContentNode(session, pentahoJcrConstants,
        nodeIdStrategy, resourceNode);

    Map<String, String> args = new HashMap<String, String>();
    Node auxNode = resourceNode.getNode(pentahoJcrConstants.getPHO_AUX());
    Node runArgsNode = auxNode.getNode(pentahoJcrConstants.getPHO_RUNARGUMENTS());
    PropertyIterator propertyIterator = runArgsNode.getProperties();
    while (propertyIterator.hasNext()) {
      Property property = propertyIterator.nextProperty();
      // skip jcr:primaryType property that exists on all nodes 
      if (!pentahoJcrConstants.getJCR_PRIMARYTYPE().equals(property.getName())) {
        String propertyName = property.getName();
        String propertyValue = property.getString();
        args.put(propertyName, propertyValue);
      }
    }

    return new RunResultRepositoryFileContent(simpleContent.getData(), simpleContent.getEncoding(), simpleContent
        .getMimeType(), args);
  }

  /**
   * {@inheritDoc}
   */
  public boolean supports(final String contentType) {
    return RunResultRepositoryFileContent.CONTENT_TYPE.equals(contentType);
  }

  /**
   * {@inheritDoc}
   */
  public void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final RunResultRepositoryFileContent content, final Node resourceNode)
      throws RepositoryException, IOException {
    simpleTransformer.createContentNode(session, pentahoJcrConstants, nodeIdStrategy, content, resourceNode);
    Node auxNode = resourceNode.addNode(pentahoJcrConstants.getPHO_AUX());
    Node runArgsNode = auxNode.addNode(pentahoJcrConstants.getPHO_RUNARGUMENTS(), pentahoJcrConstants
        .getNT_UNSTRUCTURED());
    for (Map.Entry<String, String> entry : content.getArguments().entrySet()) {
      runArgsNode.setProperty(entry.getKey(), entry.getValue());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void updateContentNode(Session session, final PentahoJcrConstants pentahoJcrConstants,
      NodeIdStrategy nodeIdStrategy, RunResultRepositoryFileContent content, Node resourceNode)
      throws RepositoryException, IOException {
    simpleTransformer.updateContentNode(session, pentahoJcrConstants, nodeIdStrategy, content, resourceNode);
    Node auxNode = resourceNode.getNode(pentahoJcrConstants.getPHO_AUX());
    Node runArgsNode = auxNode.getNode(pentahoJcrConstants.getPHO_RUNARGUMENTS());
    runArgsNode.remove();
    runArgsNode = auxNode.addNode(pentahoJcrConstants.getPHO_RUNARGUMENTS(), pentahoJcrConstants.getNT_UNSTRUCTURED());
    for (Map.Entry<String, String> entry : content.getArguments().entrySet()) {
      runArgsNode.setProperty(entry.getKey(), entry.getValue());
    }
  }

}
