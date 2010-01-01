package org.pentaho.platform.api.repository;

/**
 * Same abstraction as {@code org.springframework.security.acls.sid.Sid}.
 */
public class RepositoryFileSid {
  private String name;

  private RepositoryFileSid.Type type;

  /**
   * Creates a new Sid with given name and type of {@link Type#USER}.
   */
  public RepositoryFileSid(final String name) {
    this(name, Type.USER);
  }

  public RepositoryFileSid(final String name, final RepositoryFileSid.Type type) {
    super();
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public RepositoryFileSid.Type getType() {
    return type;
  }

  public static enum Type {
    USER, ROLE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    RepositoryFileSid other = (RepositoryFileSid) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Sid[name=" + name + ", type=" + type + "]";
  }

}