package org.pentaho.platform.repository.pcr.transform;

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
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.repository.pcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository.pcr.PentahoJcrConstants;
import org.pentaho.platform.repository.pcr.RunResultRepositoryFileContent;
import org.pentaho.platform.repository.pcr.SimpleRepositoryFileContent;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.Transformer;
import org.springframework.util.Assert;

public class RunResultRepositoryFileContentTransformer implements Transformer<RunResultRepositoryFileContent> {

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

  public RunResultRepositoryFileContent nodeToContent(final Session session, final NodeIdStrategy nodeIdStrategy,
      final Node resourceNode) throws RepositoryException, IOException {
    SimpleRepositoryFileContent simpleContent = simpleTransformer.nodeToContent(session, nodeIdStrategy, resourceNode);

    Map<String, String> args = new HashMap<String, String>();
    if (resourceNode.hasNode(JcrRepositoryFileUtils.addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_AUX))) {
      Node auxNode = resourceNode.getNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
          PentahoJcrConstants.PENTAHO_AUX));
      if (auxNode.hasNode(JcrRepositoryFileUtils.addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_RUNARGUMENTS))) {
        Node runArgsNode = auxNode.getNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
            PentahoJcrConstants.PENTAHO_RUNARGUMENTS));
        PropertyIterator propertyIterator = runArgsNode.getProperties();
        while (propertyIterator.hasNext()) {
          Property property = propertyIterator.nextProperty();
          // skip jcr:primaryType property that exists on all nodes 
          if (!PentahoJcrConstants.JCR_PRIMARYTYPE.equals(property.getName())) {
            String propertyName = JcrRepositoryFileUtils.removePentahoPrefix(session, property.getName());
            String propertyValue = property.getString();
            args.put(propertyName, propertyValue);
          }
        }
      }
    }

    return new RunResultRepositoryFileContent(simpleContent.getData(), simpleContent.getEncoding(), args);
  }

  public <S extends IRepositoryFileContent> boolean supports(final Class<S> clazz) {
    Assert.notNull(clazz);
    return clazz.isAssignableFrom(RunResultRepositoryFileContent.class);
  }

  public void contentToNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RunResultRepositoryFileContent content, final Node resourceNode) throws RepositoryException, IOException {
    simpleTransformer.contentToNode(session, nodeIdStrategy, content, resourceNode);

    Node runArgsNode = null;
    if (!content.getArguments().isEmpty()) {
      Node auxNode = resourceNode.addNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
          PentahoJcrConstants.PENTAHO_AUX));
      runArgsNode = auxNode.addNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
          PentahoJcrConstants.PENTAHO_RUNARGUMENTS), PentahoJcrConstants.NT_UNSTRUCTURED);
    } else {
      return;
    }
    for (Map.Entry<String, String> entry : content.getArguments().entrySet()) {
      runArgsNode.setProperty(JcrRepositoryFileUtils.addPentahoPrefix(session, entry.getKey()), entry.getValue());
    }
  }

}
