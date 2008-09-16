package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.List;

import org.pentaho.mantle.client.images.MantleImages;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;

public class BreadCrumbsWidget extends HorizontalPanel {

  IBreadCrumbCallback breadCrumbCallback;

  public BreadCrumbsWidget(IBreadCrumbCallback breadCrumbCallback) {
    this.breadCrumbCallback = breadCrumbCallback;
  }

  public void buildBreadCrumbs(String currentSolutionPath, Document solutionDocument) {
    clear();
    FlexTable breadCrumbs = new FlexTable();
    // build top level
    breadCrumbs.setWidget(0, 0, buildBreadCrumb("Pentaho Solution Browser", "/"));
    List<String> pathParts = ClassicNavigatorView.getPathParts(currentSolutionPath);
    String myPath = "";
    int i = 1;
    for (String path : pathParts) {
      myPath += path;

      String localizedName = ClassicNavigatorView.getElementByPath(ClassicNavigatorView.getPathParts(myPath), solutionDocument.getDocumentElement()).getAttribute("localized-name");
      
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      // no paths will end with a / (getPathParts trims them)
      breadCrumbs.setWidget(0, i++, buildBreadCrumb(localizedName, myPath));
    }
    add(breadCrumbs);
  }

  private Widget buildBreadCrumb(final String text, final String path) {
    FlexTable breadCrumb = new FlexTable();
    final Image breadCrumbImage = new Image();
    final Label breadCrumbLabel = new Label(text);
    breadCrumbImage.setStyleName("breadCrumbLabel");
    breadCrumbLabel.setStyleName("breadCrumbLabel");
    MantleImages.images.smallFolder().applyTo(breadCrumbImage);
    MouseListener imageHoverListener = new MouseListener() {

      public void onMouseDown(Widget sender, int x, int y) {

      }

      public void onMouseEnter(Widget sender) {
        MantleImages.images.smallFolderHover().applyTo(breadCrumbImage);
        breadCrumbLabel.setStyleName("breadCrumbLabelHover");
        breadCrumbImage.setStyleName("breadCrumbLabelHover");
      }

      public void onMouseLeave(Widget sender) {
        MantleImages.images.smallFolder().applyTo(breadCrumbImage);
        breadCrumbLabel.setStyleName("breadCrumbLabel");
        breadCrumbImage.setStyleName("breadCrumbLabel");
      }

      public void onMouseMove(Widget sender, int x, int y) {
      }

      public void onMouseUp(Widget sender, int x, int y) {
      }

    };
    ClickListener clickListener = new ClickListener() {

      public void onClick(Widget sender) {
        breadCrumbCallback.breadCrumbSelected(path);
      }

    };
    breadCrumbImage.addMouseListener(imageHoverListener);
    breadCrumbLabel.addMouseListener(imageHoverListener);
    breadCrumbImage.addClickListener(clickListener);
    breadCrumbLabel.addClickListener(clickListener);
    breadCrumb.setWidget(0, 0, breadCrumbImage);
    breadCrumb.setWidget(0, 1, breadCrumbLabel);
    return breadCrumb;
  }
}
