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

  public RunResultRepositoryFileContent fromContentNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final Node resourceNode) throws RepositoryException, IOException {
    SimpleRepositoryFileContent simpleContent = simpleTransformer
        .fromContentNode(session, nodeIdStrategy, resourceNode);

    Map<String, String> args = new HashMap<String, String>();
    Node auxNode = resourceNode.getNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_AUX));
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

    return new RunResultRepositoryFileContent(simpleContent.getData(), simpleContent.getEncoding(), simpleContent
        .getMimeType(), args);
  }

  public <S extends IRepositoryFileContent> boolean supports(final Class<S> clazz) {
    Assert.notNull(clazz);
    return clazz.isAssignableFrom(RunResultRepositoryFileContent.class);
  }

  public void createContentNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RunResultRepositoryFileContent content, final Node resourceNode) throws RepositoryException, IOException {
    simpleTransformer.createContentNode(session, nodeIdStrategy, content, resourceNode);
    Node auxNode = resourceNode.addNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_AUX));
    Node runArgsNode = auxNode.addNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_RUNARGUMENTS), PentahoJcrConstants.NT_UNSTRUCTURED);
    for (Map.Entry<String, String> entry : content.getArguments().entrySet()) {
      runArgsNode.setProperty(JcrRepositoryFileUtils.addPentahoPrefix(session, entry.getKey()), entry.getValue());
    }
  }

  public void updateContentNode(Session session, NodeIdStrategy nodeIdStrategy, RunResultRepositoryFileContent content,
      Node resourceNode) throws RepositoryException, IOException {
    simpleTransformer.updateContentNode(session, nodeIdStrategy, content, resourceNode);
    Node auxNode = resourceNode.getNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_AUX));
    Node runArgsNode = auxNode.getNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_RUNARGUMENTS));
    runArgsNode.remove();
    runArgsNode = auxNode.addNode(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_RUNARGUMENTS), PentahoJcrConstants.NT_UNSTRUCTURED);
    for (Map.Entry<String, String> entry : content.getArguments().entrySet()) {
      runArgsNode.setProperty(JcrRepositoryFileUtils.addPentahoPrefix(session, entry.getKey()), entry.getValue());
    }
  }

}
