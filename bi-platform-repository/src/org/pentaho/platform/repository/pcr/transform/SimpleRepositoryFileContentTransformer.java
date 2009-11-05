package org.pentaho.platform.repository.pcr.transform;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.repository.pcr.SimpleRepositoryFileContent;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.Transformer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class SimpleRepositoryFileContentTransformer implements Transformer {

  public IRepositoryFileContent fromNode(Session session, NodeIdStrategy nodeIdStrategy, Node resourceNode)
      throws RepositoryException, IOException {
    String encoding = null;
    if (resourceNode.hasProperty(JcrConstants.JCR_ENCODING)) {
      encoding = resourceNode.getProperty(JcrConstants.JCR_ENCODING).getString();
    }
    InputStream data = resourceNode.getProperty(JcrConstants.JCR_DATA).getStream();
    return new SimpleRepositoryFileContent(data, encoding);

  }

  public void toNode(Session session, NodeIdStrategy nodeIdStrategy, IRepositoryFileContent content, Node resourceNode)
      throws RepositoryException, IOException {
    if (StringUtils.hasText(content.getEncoding())) {
      resourceNode.setProperty(JcrConstants.JCR_ENCODING, content.getEncoding());
    }
    if (content.getData() != null) {
      resourceNode.setProperty(JcrConstants.JCR_DATA, content.getData());
    }
  }

  public <T extends IRepositoryFileContent> boolean supports(final Class<T> clazz) {
    Assert.notNull(clazz);
    return clazz.isAssignableFrom(SimpleRepositoryFileContent.class);
  }

}
