package org.pentaho.mantle.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.TreeImages;

public interface BookmarkImages extends TreeImages {
  AbstractImagePrototype treeOpen();

  AbstractImagePrototype treeClosed();

  @Resource ("fileIcon.gif")
  AbstractImagePrototype treeLeaf();
}
