package org.pentaho.platform.repository.pcr;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.Permission;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.MutableAclService;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.util.Assert;

/**
 * Implementation of {@link IPentahoContentRepository} using an {@link IRepositoryFileDao} and Spring 
 * Security's {@link MutableAclService}.
 * 
 * @author mlowery
 */
public class PentahoContentRepository implements IPentahoContentRepository {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoContentRepository.class);

  // ~ Instance fields =================================================================================================

  private IRepositoryFileDao contentDao;

  private IPentahoMutableAclService mutableAclService;

  private IRepositoryEventHandler repositoryEventHandler;

  private IAclConverter aclConverter = new AclConverter();

  // ~ Constructors ====================================================================================================

  public PentahoContentRepository(final IRepositoryFileDao contentDao,
      final IPentahoMutableAclService mutableAclService, final IRepositoryEventHandler repositoryEventHandler) {
    super();
    Assert.notNull(contentDao);
    Assert.notNull(mutableAclService);
    Assert.notNull(repositoryEventHandler);
    this.contentDao = contentDao;
    this.mutableAclService = mutableAclService;
    this.repositoryEventHandler = repositoryEventHandler;
  }

  // ~ Methods =========================================================================================================

  public synchronized IRepositoryEventHandler getRepositoryEventHandler() {
    return repositoryEventHandler;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(final String absPath) {
    Assert.hasText(absPath);

    return contentDao.getFile(absPath);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    Assert.notNull(content);
    // external callers never allowed to create files at repo root
    Assert.notNull(parentFolder);
    return internalCreateFile(parentFolder, file, content, true, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(file.isFolder());
    Assert.hasText(file.getName());
    // external callers never allowed to create folders at repo root
    Assert.notNull(parentFolder);
    return internalCreateFolder(parentFolder, file, true, versionMessageAndLabel);

  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Delegates to {@link #getStreamForRead(RepositoryFile)} but assumes that some external system (e.g. Spring Security)
   * is protecting this method with different authorization rules than {@link #getStreamForRead(RepositoryFile)}.
   * </p>
   * 
   * TODO mlowery figure this delegation out
   * 
   * @see #getContentForRead(RepositoryFile, Class)
   */
  public synchronized <T extends IRepositoryFileContent> T getContentForExecute(RepositoryFile file,
      Class<T> contentClass) {
    return getContentForRead(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized <T extends IRepositoryFileContent> T getContentForRead(RepositoryFile file, Class<T> contentClass) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return contentDao.getContent(file, contentClass);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<RepositoryFile> getChildren(final RepositoryFile folder) {
    Assert.notNull(folder);
    Assert.notNull(folder.getId());
    Assert.notNull(folder.isFolder());
    return contentDao.getChildren(folder);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.isTrue(!file.isFolder());
    Assert.hasText(file.getName());
    if (!file.isFolder()) {
      Assert.notNull(content);
      Assert.hasText(file.getContentType());
    }

    return internalUpdateFile(file, content, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void deleteFile(final RepositoryFile file, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    // acl deleted when file node is deleted
    contentDao.deleteFile(file, versionMessageAndLabel);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFileAcl getAcl(final RepositoryFile file) {
    Assert.notNull(file);
    return aclConverter.ssAclToPentahoAcl(mutableAclService.readAclById(new ObjectIdentityImpl(RepositoryFile.class,
        file.getId())));
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void lockFile(final RepositoryFile file, final String message) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    contentDao.lockFile(file, message);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void unlockFile(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    Assert.isTrue(!file.isFolder());
    contentDao.unlockFile(file);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized List<VersionSummary> getVersionSummaries(final RepositoryFile file) {
    Assert.notNull(file);
    Assert.notNull(file.getId());
    return contentDao.getVersionSummaries(file);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RepositoryFile getFile(VersionSummary versionSummary) {
    Assert.notNull(versionSummary);
    Assert.notNull(versionSummary.getId());
    Assert.notNull(versionSummary.getVersionedFileId());
    return contentDao.getFile(versionSummary);
  }

  /**
   * Returns the username of the current user.
   */
  private String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  /**
   * Returns the tenant ID of the current user.
   */
  private String internalGetTenantId() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
  }

  private RepositoryFile internalCreateFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(content);

    RepositoryFile newFile = contentDao.createFile(parentFolder, file, content, versionMessageAndLabel);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalCreateFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final boolean inheritAces, final String... versionMessageAndLabel) {
    Assert.notNull(file);

    RepositoryFile newFile = contentDao.createFolder(parentFolder, file, versionMessageAndLabel);
    internalCreateAcl(newFile, inheritAces);

    return newFile;
  }

  private RepositoryFile internalUpdateFile(final RepositoryFile file, final IRepositoryFileContent content,
      final String... versionMessageAndLabel) {
    Assert.notNull(file);
    Assert.notNull(content);

    return contentDao.updateFile(file, content, versionMessageAndLabel);
  }

  private MutableAcl internalCreateAcl(final RepositoryFile file, final boolean entriesInheriting) {
    Assert.notNull(file);

    ObjectIdentity parentOid = null;
    if (file.getParentId() != null) {
      parentOid = new ObjectIdentityImpl(RepositoryFile.class, file.getParentId());
    }
    return mutableAclService.createAndInitializeAcl(new ObjectIdentityImpl(RepositoryFile.class, file.getId()),
        parentOid, entriesInheriting, new PrincipalSid(internalGetUsername()));
  }

  /**
   * Converts to and from org.springframework.security.acls.Acl and org.pentaho.api.repository.RepositoryFileAcl.
   */
  public static interface IAclConverter {
    RepositoryFileAcl ssAclToPentahoAcl(final Acl ssAcl);

    Acl pentahoAclToSsAcl(final RepositoryFileAcl pentahoAcl);
  }

  private class AclConverter implements IAclConverter {

    public Acl pentahoAclToSsAcl(final RepositoryFileAcl pentahoAcl) {
      // TODO Auto-generated method stub 
      return null;
    }

    public RepositoryFileAcl ssAclToPentahoAcl(final Acl ssAcl) {
      Assert.notNull(ssAcl);

      String sidName = null;
      RepositoryFileSid.Type sidType = null;
      org.springframework.security.acls.sid.Sid sid = ssAcl.getOwner();
      if (sid instanceof PrincipalSid) {
        sidName = ((PrincipalSid) sid).getPrincipal();
        sidType = RepositoryFileSid.Type.USER;
      } else if (sid instanceof GrantedAuthoritySid) {
        sidName = ((GrantedAuthoritySid) sid).getGrantedAuthority();
        sidType = RepositoryFileSid.Type.ROLE;
      } else {
        throw new RuntimeException("unrecognized sid type");
      }

      RepositoryFileAcl.Builder builder = new RepositoryFileAcl.Builder(ssAcl.getObjectIdentity().getIdentifier(),
          sidName, sidType);

      AccessControlEntry[] aces = ssAcl.getEntries();
      for (int i = 0; i < aces.length; i++) {
        RepositoryFileSid recipient = springSecuritySidToRepositoryFileSid(aces[i].getSid());
        EnumSet<Permission> permissions = ssPermissionsToPentahoPermissions(aces[i].getPermission());
        builder.ace(recipient, permissions);
      }

      return builder.build();
    }

    private org.springframework.security.acls.sid.Sid repositoryFileSidToSpringSecuritySid(final RepositoryFileSid pentahoSid) {
      return null;

    }

    private RepositoryFileSid springSecuritySidToRepositoryFileSid(final org.springframework.security.acls.sid.Sid ssSid) {
      String sidName = null;
      RepositoryFileSid.Type sidType = null;
      if (ssSid instanceof PrincipalSid) {
        sidName = ((PrincipalSid) ssSid).getPrincipal();
        sidType = RepositoryFileSid.Type.USER;
      } else if (ssSid instanceof GrantedAuthoritySid) {
        sidName = ((GrantedAuthoritySid) ssSid).getGrantedAuthority();
        sidType = RepositoryFileSid.Type.ROLE;
      } else {
        throw new RuntimeException("unrecognized sid type");
      }
      return new RepositoryFileSid(sidName, sidType);
    }

    private EnumSet<Permission> ssPermissionsToPentahoPermissions(
        final org.springframework.security.acls.Permission ssPermission) {
      EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
      if ((RepositoryFilePermission.READ.getMask() & ssPermission.getMask()) == RepositoryFilePermission.READ.getMask()) {
        permissions.add(Permission.READ);
      }
      if ((RepositoryFilePermission.APPEND.getMask() & ssPermission.getMask()) == RepositoryFilePermission.APPEND
          .getMask()) {
        permissions.add(Permission.APPEND);
      }
      if ((RepositoryFilePermission.DELETE.getMask() & ssPermission.getMask()) == RepositoryFilePermission.DELETE
          .getMask()) {
        permissions.add(Permission.DELETE);
      }
      if ((RepositoryFilePermission.DELETE_CHILD.getMask() & ssPermission.getMask()) == RepositoryFilePermission.DELETE_CHILD
          .getMask()) {
        permissions.add(Permission.DELETE_CHILD);
      }
      if ((RepositoryFilePermission.EXECUTE.getMask() & ssPermission.getMask()) == RepositoryFilePermission.EXECUTE
          .getMask()) {
        permissions.add(Permission.EXECUTE);
      }
      if ((RepositoryFilePermission.READ_ACL.getMask() & ssPermission.getMask()) == RepositoryFilePermission.READ_ACL
          .getMask()) {
        permissions.add(Permission.READ_ACL);
      }
      if ((RepositoryFilePermission.WRITE.getMask() & ssPermission.getMask()) == RepositoryFilePermission.WRITE
          .getMask()) {
        permissions.add(Permission.WRITE);
      }
      if ((RepositoryFilePermission.WRITE_ACL.getMask() & ssPermission.getMask()) == RepositoryFilePermission.WRITE_ACL
          .getMask()) {
        permissions.add(Permission.WRITE_ACL);
      }
      return permissions;
    }

  }

}
