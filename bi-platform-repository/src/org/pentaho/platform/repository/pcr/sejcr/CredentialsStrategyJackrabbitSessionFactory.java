package org.pentaho.platform.repository.pcr.sejcr;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.extensions.jcr.SessionHolderProviderManager;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Copy-and-paste of {@link org.springframework.extensions.jcr.jackrabbit.JackrabbitSessionFactory} except that it 
 * extends {@link CredentialsStrategySessionFactory}.
 * 
 * @author mlowery
 */
public class CredentialsStrategyJackrabbitSessionFactory extends CredentialsStrategySessionFactory {

  public CredentialsStrategyJackrabbitSessionFactory(Repository repository, CredentialsStrategy credentialsStrategy) {
    super(repository, credentialsStrategy);
  }

  public CredentialsStrategyJackrabbitSessionFactory(Repository repository, String workspaceName,
      CredentialsStrategy credentialsStrategy, SessionHolderProviderManager sessionHolderProviderManager) {
    super(repository, workspaceName, credentialsStrategy, sessionHolderProviderManager);
  }

  public CredentialsStrategyJackrabbitSessionFactory(Repository repository, String workspaceName,
      CredentialsStrategy credentialsStrategy) {
    super(repository, workspaceName, credentialsStrategy);
  }

  private static final Logger LOG = LoggerFactory.getLogger(CredentialsStrategyJackrabbitSessionFactory.class);

  /**
   * Node definitions in CND format.
   */
  private Resource[] nodeDefinitions;

  private String contentType = JackrabbitNodeTypeManager.TEXT_X_JCR_CND;

  /*
   * (non-Javadoc)
   * @see org.springframework.extensions.jcr.JcrSessionFactory#registerNodeTypes()
   */
  protected void registerNodeTypes() throws Exception {
    if (!ObjectUtils.isEmpty(nodeDefinitions)) {
      Workspace ws = getBareSession().getWorkspace();

      JackrabbitNodeTypeManager jackrabbitNodeTypeManager = (JackrabbitNodeTypeManager) ws.getNodeTypeManager();

      boolean debug = LOG.isDebugEnabled();
      for (int i = 0; i < nodeDefinitions.length; i++) {
        Resource resource = nodeDefinitions[i];
        if (debug) {
          LOG.debug("adding node type definitions from " + resource.getDescription());
        }
        try {
          jackrabbitNodeTypeManager.registerNodeTypes(resource.getInputStream(), contentType);
        } catch (RepositoryException ex) {
          LOG.error("Error registering nodetypes ", ex.getCause());
        }
      }
    }
  }

  /**
   * @param nodeDefinitions The nodeDefinitions to set.
   */
  public void setNodeDefinitions(Resource[] nodeDefinitions) {
    this.nodeDefinitions = nodeDefinitions;
  }

  /**
   * Indicate the node definition content type (by default, JackrabbitNodeTypeManager#TEXT_XML).
   * @see JackrabbitNodeTypeManager#TEXT_X_JCR_CND
   * @see JackrabbitNodeTypeManager#TEXT_XML
   * @param contentType The contentType to set.
   */
  public void setContentType(String contentType) {
    Assert.hasText(contentType, "contentType is required");
    this.contentType = contentType;
  }

}
