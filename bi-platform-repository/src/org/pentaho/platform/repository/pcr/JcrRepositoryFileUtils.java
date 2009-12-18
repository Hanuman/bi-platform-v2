package org.pentaho.platform.repository.pcr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.Transformer;
import org.springframework.util.Assert;

public class JcrRepositoryFileUtils {
  public static RepositoryFile getFileById(final Session session, final NodeIdStrategy nodeIdStrategy,
      final Serializable id) throws RepositoryException, IOException {
    Node fileNode = nodeIdStrategy.findNodeById(session, id);
    Assert.notNull(fileNode);
    return fromFileNode(session, nodeIdStrategy, fileNode);
  }

  public static RepositoryFile fromFileNode(final Session session, final NodeIdStrategy nodeIdStrategy, final Node node)
      throws RepositoryException, IOException {
    Assert.isTrue(isSupportedNodeType(node));

    Date created = null;
    Date lastModified = null;
    String contentType = null;
    boolean folder = false;
    boolean versioned = false;

    if (isFolder(node)) {
      folder = true;
    }

    if (node.hasProperty(PentahoJcrConstants.JCR_CREATED)) {
      Calendar tmpCal = node.getProperty(PentahoJcrConstants.JCR_CREATED).getDate();
      if (tmpCal != null) {
        created = tmpCal.getTime();
      }
    }

    if (isFileOrLinkedFile(node)) {
      Node resourceNode = getResourceNode(session, node);
      Calendar tmpCal = resourceNode.getProperty(PentahoJcrConstants.JCR_LASTMODIFIED).getDate();
      if (tmpCal != null) {
        lastModified = tmpCal.getTime();
      }
    }

    if (isPentahoFile(session, node)) {
      contentType = node.getProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_CONTENTTYPE)).getString();
    }

    versioned = isVersioned(session, node);

    RepositoryFile file = new RepositoryFile.Builder(node.getName(), nodeIdStrategy.getId(node), !node.getParent()
        .isSame(session.getRootNode()) ? nodeIdStrategy.getId(node.getParent()) : null).createdDate(created)
        .lastModificationDate(lastModified).contentType(contentType).folder(folder).versioned(versioned).absolutePath(
            node.getPath()).build();

    return file;
  }

  public static Node createFolderNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile parentFolder, final RepositoryFile folder) throws RepositoryException, IOException {
    Node parentFolderNode;
    if (parentFolder != null) {
      parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
    } else {
      parentFolderNode = session.getRootNode();
    }

    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(parentFolderNode);

    Node folderNode = parentFolderNode.addNode(folder.getName(), PentahoJcrConstants.NT_FOLDER);

    if (folder.isVersioned()) {
      //      folderNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      folderNode.addMixin(PentahoJcrConstants.MIX_VERSIONABLE);
    }

    nodeIdStrategy.setId(folderNode, null);
    return folderNode;
  }

  public static Node createFileNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile parentFolder, final RepositoryFile file, final IRepositoryFileContent content,
      final Transformer<IRepositoryFileContent> transformer) throws RepositoryException, IOException {

    Node parentFolderNode;
    if (parentFolder != null) {
      parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
    } else {
      parentFolderNode = session.getRootNode();
    }

    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(parentFolderNode);

    Node fileNode = parentFolderNode.addNode(file.getName(), PentahoJcrConstants.NT_FILE);
    fileNode.addMixin(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_MIXIN_PENTAHOFILE));
    fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_CONTENTTYPE), content.getContentType());
    nodeIdStrategy.setId(fileNode, null);
    Node resourceNode = fileNode.addNode(PentahoJcrConstants.JCR_CONTENT, PentahoJcrConstants.NT_RESOURCE);

    // mandatory properties on nt:resource; give them a value to satisfy Jackrabbit

    resourceNode.setProperty(PentahoJcrConstants.JCR_LASTMODIFIED, fileNode
        .getProperty(PentahoJcrConstants.JCR_CREATED).getDate());

    resourceNode.addMixin(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_MIXIN_PENTAHORESOURCE));
    if (file.isVersioned()) {
      //      fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      fileNode.addMixin(PentahoJcrConstants.MIX_VERSIONABLE);
    }

    transformer.createContentNode(session, nodeIdStrategy, content, resourceNode);
    return fileNode;
  }

  public static Node updateFileNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file, final IRepositoryFileContent content,
      final Transformer<IRepositoryFileContent> transformer) throws RepositoryException, IOException {

    Calendar lastModified = Calendar.getInstance();

    Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);

    fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_CONTENTTYPE), file.getContentType());

    Node resourceNode = fileNode.getNode(PentahoJcrConstants.JCR_CONTENT);

    // mandatory properties on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty(PentahoJcrConstants.JCR_LASTMODIFIED, lastModified);

    transformer.updateContentNode(session, nodeIdStrategy, content, resourceNode);
    return fileNode;
  }

  public static Node getResourceNode(final Session session, final Node node) throws RepositoryException, IOException {
    Assert.isTrue(isFileOrLinkedFile(node));
    Node resourceNode = null;
    if (isFile(node)) {
      resourceNode = node.getNode(PentahoJcrConstants.JCR_CONTENT);
    } else {
      // linked file
      String resourceNodeUuid = node.getProperty(PentahoJcrConstants.JCR_CONTENT).getString();
      resourceNode = session.getNodeByUUID(resourceNodeUuid);
    }
    return resourceNode;
  }

  public static IRepositoryFileContent getContent(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file, final Transformer<IRepositoryFileContent> transformer) throws RepositoryException,
      IOException {
    Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
    Assert.isTrue(!isFolder(fileNode));

    return transformer.fromContentNode(session, nodeIdStrategy, getResourceNode(session, fileNode));
  }

  public static List<RepositoryFile> getChildren(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile folder) throws RepositoryException, IOException {
    Node folderNode = nodeIdStrategy.findNodeById(session, folder.getId());
    Assert.isTrue(isFolder(folderNode));

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    // get all immediate child nodes that are of type NT_FOLDER, NT_FILE, or NT_LINKEDFILE
    NodeIterator nodeIterator = folderNode.getNodes();
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode();
      if (isFolder(node) || isFileOrLinkedFile(node)) {
        children.add(fromFileNode(session, nodeIdStrategy, node));
      }
    }
    Collections.sort(children);
    return children;
  }

  public static boolean isFolder(final Node node) throws RepositoryException {
    Assert.notNull(node);
    return node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString().equals(PentahoJcrConstants.NT_FOLDER);
  }

  private static boolean isResource(final Node node) throws RepositoryException {
    Assert.notNull(node);
    return node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString().equals(PentahoJcrConstants.NT_RESOURCE);
  }

  private static boolean isFileOrLinkedFile(final Node node) throws RepositoryException {
    Assert.notNull(node);
    String nodeTypeName = node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString();
    return PentahoJcrConstants.NT_LINKEDFILE.equals(nodeTypeName) || PentahoJcrConstants.NT_FILE.equals(nodeTypeName);
  }

  private static boolean isPentahoFile(final Session session, final Node node) throws RepositoryException {
    Assert.notNull(node);
    Value[] mixinTypeNames = node.getProperty(PentahoJcrConstants.JCR_MIXINTYPES).getValues();
    for (Value v : mixinTypeNames) {
      if (addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_MIXIN_PENTAHOFILE).equals(v.getString())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isVersioned(final Session session, final Node node) throws RepositoryException {
    Assert.notNull(node);
    Value[] mixinTypeNames = node.getProperty(PentahoJcrConstants.JCR_MIXINTYPES).getValues();
    for (Value v : mixinTypeNames) {
      if (PentahoJcrConstants.MIX_VERSIONABLE.equals(v.getString())) {
        return true;
      }
    }
    return false;
    //    return node.getProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED)).getBoolean();
  }

  private static boolean isFile(final Node node) throws RepositoryException {
    Assert.notNull(node);
    return node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString().equals(PentahoJcrConstants.NT_FILE);
  }

  private static boolean isSupportedNodeType(final Node node) throws RepositoryException {
    Assert.notNull(node);
    String nodeTypeName = node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString();
    return PentahoJcrConstants.NT_FOLDER.equals(nodeTypeName) || PentahoJcrConstants.NT_LINKEDFILE.equals(nodeTypeName)
        || PentahoJcrConstants.NT_FILE.equals(nodeTypeName) || PentahoJcrConstants.NT_RESOURCE.equals(nodeTypeName);
  }

  public static String addPentahoPrefix(final Session session, final String name) throws RepositoryException {
    Assert.notNull(session);
    Assert.hasText(name);
    String prefix = session.getWorkspace().getNamespaceRegistry().getPrefix(PentahoJcrConstants.PENTAHO_NAMESPACE_URI);
    return prefix + ":" + name;
  }

  public static String removePentahoPrefix(final Session session, final String nameWithPrefix)
      throws RepositoryException {
    Assert.notNull(session);
    Assert.hasText(nameWithPrefix);
    String prefix = session.getWorkspace().getNamespaceRegistry().getPrefix(PentahoJcrConstants.PENTAHO_NAMESPACE_URI);
    return nameWithPrefix.substring(prefix.length() + 1); // plus one for the colon
  }

  /**
   * Conditionally checks out node representing file if node is versionable.
   */
  public static void checkoutNearestVersionableFileIfNecessary(final Session session,
      final NodeIdStrategy nodeIdStrategy, final RepositoryFile file) throws RepositoryException, IOException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node in
    // question would be the repository root node and that is never versioned
    if (file != null) {
      Node node = nodeIdStrategy.findNodeById(session, file.getId());
      checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, node);
    }
  }

  /**
   * Conditionally checks out node if node is versionable.
   */
  public static void checkoutNearestVersionableNodeIfNecessary(final Session session,
      final NodeIdStrategy nodeIdStrategy, final Node node) throws RepositoryException, IOException {
    Assert.notNull(node);

    Node versionableNode = findNearestVersionableNode(node);
    
    if (versionableNode != null) {
      versionableNode.checkout();
    }
  }

  /**
   * Conditionally checks in node representing file if node is versionable.
   */
  public static void checkinNearestVersionableFileIfNecessary(final Session session,
      final NodeIdStrategy nodeIdStrategy, final RepositoryFile file) throws RepositoryException, IOException {
    // file could be null meaning the caller is using null as the parent folder; that's OK; in this case the node in
    // question would be the repository root node and that is never versioned
    if (file != null) {
      Node node = nodeIdStrategy.findNodeById(session, file.getId());
      checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, node);
    }
  }

  /**
   * Conditionally checks in node if node is versionable.
   */
  public static void checkinNearestVersionableNodeIfNecessary(final Session session,
      final NodeIdStrategy nodeIdStrategy, final Node node) throws RepositoryException, IOException {
    Assert.notNull(node);
    
    Node versionableNode = findNearestVersionableNode(node);
    
    if (versionableNode != null) {
      versionableNode.checkin();
    }
  }

  /**
   * Returns the nearest versionable node (possibly the node itself) or null if the root is reached.
   */
    private static Node findNearestVersionableNode(final Node node) throws RepositoryException, IOException {
      Node currentNode = node;
      while (!currentNode.isNodeType(PentahoJcrConstants.MIX_VERSIONABLE)) {
        try {
          currentNode = currentNode.getParent();
        } catch (ItemNotFoundException e) {
          // at the root
          return null;
        }
      }
      return currentNode;
    }

  public static void deleteFile(final Session session, final NodeIdStrategy nodeIdStrategy, final RepositoryFile file)
      throws RepositoryException, IOException {
    Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull(fileNode);
    fileNode.remove();
  }
}
