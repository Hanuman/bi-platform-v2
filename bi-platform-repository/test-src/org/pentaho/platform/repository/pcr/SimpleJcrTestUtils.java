package org.pentaho.platform.repository.pcr;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

public class SimpleJcrTestUtils {

  public static void deleteItem(final JcrTemplate jcrTemplate, final String absPath) {
    jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item item;
        try {
          item = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return null;
        }
        item.remove();
        session.save();
        return null;
      }
    });
  }

  public static Item getItem(final JcrTemplate jcrTemplate, final String absPath) {
    return (Item) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item item;
        try {
          item = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return null;
        }
        return item;
      }
    });
  }

  public static String getNodeId(final JcrTemplate jcrTemplate, final String absPath) {
    return (String) jcrTemplate.execute(new JcrCallback() {
      public Object doInJcr(final Session session) throws RepositoryException {
        Item item;
        try {
          item = session.getItem(absPath);
        } catch (PathNotFoundException e) {
          return null;
        }
        Assert.isTrue(item.isNode());
        return ((Node) item).getUUID();
      }
    });
  }

}
