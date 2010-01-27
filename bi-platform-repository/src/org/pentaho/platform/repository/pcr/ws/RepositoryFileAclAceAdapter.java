package org.pentaho.platform.repository.pcr.ws;

import java.util.Arrays;
import java.util.EnumSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;

public class RepositoryFileAclAceAdapter extends XmlAdapter<JaxbSafeRepositoryFileAclAce, RepositoryFileAce> {

  private static final Log logger = LogFactory.getLog(RepositoryFileAclAceAdapter.class);

  @Override
  public JaxbSafeRepositoryFileAclAce marshal(RepositoryFileAce v) throws Exception {
    try {
      JaxbSafeRepositoryFileAclAce jaxbAce = new JaxbSafeRepositoryFileAclAce();
      jaxbAce.recipient = v.getSid().getName();
      if (v.getSid().getType() == RepositoryFileSid.Type.USER) {
        jaxbAce.user = true;
      } else {
        jaxbAce.user = false;
      }
      jaxbAce.permissions = v.getPermissions().toArray(new RepositoryFilePermission[v.getPermissions().size()]);
      return jaxbAce;
    } catch (Exception e) {
      logger.error(String.format("error marshalling %s to %s", RepositoryFileAce.class.getName(),
          JaxbSafeRepositoryFileAclAce.class.getName()), e);
      throw e;
    }
  }

  @Override
  public RepositoryFileAce unmarshal(JaxbSafeRepositoryFileAclAce v) throws Exception {
    try {
      if (v.user) {
        return new RepositoryFileAce(new RepositoryFileSid(v.recipient, RepositoryFileSid.Type.USER), EnumSet
            .copyOf(Arrays.asList(v.permissions)));
      } else {
        return new RepositoryFileAce(new RepositoryFileSid(v.recipient, RepositoryFileSid.Type.ROLE), EnumSet
            .copyOf(Arrays.asList(v.permissions)));
      }
    } catch (Exception e) {
      logger.error(String.format("error unmarshalling %s to %s", JaxbSafeRepositoryFileAclAce.class.getName(),
          RepositoryFileAce.class.getName()), e);
      throw e;
    }
  }

}
