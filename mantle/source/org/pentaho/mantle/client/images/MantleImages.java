package org.pentaho.mantle.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HorizontalSplitPanelImages;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.VerticalSplitPanelImages;

public interface MantleImages extends ImageBundle, TreeImages, HorizontalSplitPanelImages, VerticalSplitPanelImages {

  public static final MantleImages images = (MantleImages) GWT.create(MantleImages.class);
  public static final BookmarkImages bookmarkImages = (BookmarkImages) GWT.create(BookmarkImages.class);

  AbstractImagePrototype plus();

  AbstractImagePrototype minus();

  AbstractImagePrototype file();

  AbstractImagePrototype fileHover();

  AbstractImagePrototype folder();

  AbstractImagePrototype folderHover();

  AbstractImagePrototype smallFolder();

  AbstractImagePrototype smallFolderHover();

  AbstractImagePrototype fileIcon();

  AbstractImagePrototype file_url();

  AbstractImagePrototype file_action();

  AbstractImagePrototype file_analysis();
  
  AbstractImagePrototype file_report();
  
  AbstractImagePrototype treeOpen();

  AbstractImagePrototype treeClosed();

  AbstractImagePrototype treeLeaf();

  AbstractImagePrototype closeTab();

  AbstractImagePrototype closeTabHover();

  AbstractImagePrototype backButton();

  AbstractImagePrototype backToFirstPage();

  AbstractImagePrototype forwardButton();

  AbstractImagePrototype forwardToLastPage();

  AbstractImagePrototype run();
  
  AbstractImagePrototype runDisabled();

  AbstractImagePrototype update();

  AbstractImagePrototype updateDisabled();

  AbstractImagePrototype misc();
  
  AbstractImagePrototype miscDisabled();

  AbstractImagePrototype horizontalSplitPanelThumb();

  AbstractImagePrototype verticalSplitPanelThumb();

  AbstractImagePrototype new_analysis_32();

  AbstractImagePrototype new_report_32();

  AbstractImagePrototype print_32();

  AbstractImagePrototype print_32_disabled();

  AbstractImagePrototype save_32();

  AbstractImagePrototype saveAs_32();

  AbstractImagePrototype save_32_disabled();

  AbstractImagePrototype saveAs_32_disabled();

  AbstractImagePrototype space1x13();

  AbstractImagePrototype space1x20();

  AbstractImagePrototype space1x23();

  AbstractImagePrototype generic_square_32();
  
  AbstractImagePrototype generic_square_32_disabled();

  AbstractImagePrototype browser_hide_32();
  
  AbstractImagePrototype browser_show_32();
  
  AbstractImagePrototype workspace_32();
    
}
