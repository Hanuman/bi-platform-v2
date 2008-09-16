package org.pentaho.mantle.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.TreeImages;

public interface BookmarkImages extends TreeImages {
  AbstractImagePrototype treeOpen();

  AbstractImagePrototype treeClosed();

  /**
   * @gwt.resource org/pentaho/mantle/client/images/fileIcon.gif
   */
  AbstractImagePrototype treeLeaf();
}
