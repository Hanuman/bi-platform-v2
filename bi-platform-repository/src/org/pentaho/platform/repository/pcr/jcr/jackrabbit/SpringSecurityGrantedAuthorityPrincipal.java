package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

/**
 * The Jackrabbit representation of a {@code org.springframework.security.acls.sid.GrantedAuthoritySid}. This was 
 * required as no {@link Group} implementations that could re-used were found. (Jackrabbit's {@code UserPrincipal} is 
 * used for representing {@code org.springframework.security.acls.sid.PrincipalSid}.
 * 
 * @author mlowery
 */
public class SpringSecurityGrantedAuthorityPrincipal implements Group {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private String name;

  // ~ Constructors ====================================================================================================

  public SpringSecurityGrantedAuthorityPrincipal(final String name) {
    super();
    this.name = name;
  }

  // ~ Methods =========================================================================================================

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SpringSecurityGrantedAuthorityPrincipal other = (SpringSecurityGrantedAuthorityPrincipal) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PentahoRolePrincipal[name=" + name + "]";
  }

  public boolean addMember(final Principal user) {
    throw new UnsupportedOperationException();
  }

  public boolean isMember(final Principal member) {
    throw new UnsupportedOperationException();
  }

  public Enumeration<? extends Principal> members() {
    throw new UnsupportedOperationException();
  }

  public boolean removeMember(final Principal user) {
    throw new UnsupportedOperationException();
  }

}
