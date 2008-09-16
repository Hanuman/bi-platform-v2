package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

public class ClassicNavigatorView extends DeckPanel implements IBreadCrumbCallback {

  String currentSolutionPath = "";
  Document solutionDocument;
  Widget oldNavigatorTable = null;

  public ClassicNavigatorView() {
    setWidth("100%");
    setAnimationEnabled(false);
  }

  public void buildSolutionNavigator() {
    // build breadcrumbs for current selection
    // bread crumbs built as a table because of ui flexibility
    BreadCrumbsWidget breadCrumbs = new BreadCrumbsWidget(this);
    breadCrumbs.buildBreadCrumbs(currentSolutionPath, solutionDocument);
    VerticalPanel wrapper = new VerticalPanel();
    wrapper.setWidth("100%");
    wrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

    FlexTable navigatorTable = new FlexTable();
    navigatorTable.setWidth("100%");
    navigatorTable.setWidget(0, 0, breadCrumbs);
    navigatorTable.getCellFormatter().setWidth(0, 0, "100%");
    navigatorTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
    navigatorTable.setWidget(1, 0, buildNavigator());
    navigatorTable.getCellFormatter().setWidth(1, 0, "100%");
    navigatorTable.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
    wrapper.add(navigatorTable);
    add(wrapper);
    for (int i = 0; i < getWidgetCount(); i++) {
      if (wrapper == getWidget(i)) {
        showWidget(i);
        break;
      }
    }
    if (oldNavigatorTable != null) {
      remove(oldNavigatorTable);
    }
    oldNavigatorTable = wrapper;
  }

  private Widget buildNavigator() {
    Element solutionRoot = solutionDocument.getDocumentElement();
    VerticalPanel navigatorPanel = new VerticalPanel();
    navigatorPanel.setStyleName("classicNavigatorPanel");

    if (currentSolutionPath == null || "".equals(currentSolutionPath) || "/".equals(currentSolutionPath)) {
      NodeList children = solutionRoot.getChildNodes();

      HTML solutionBrowserDescription = new HTML(Messages.getInstance().classicSolutionBrowserDescription(), true);
      solutionBrowserDescription.setStyleName("solutionBrowserDescription");
      navigatorPanel.add(solutionBrowserDescription);
      int numSolutions = 0;
      for (int i = 0; i < children.getLength(); i++) {
        boolean isDirectory = "true".equals(((Element) children.item(i)).getAttribute("isDirectory"));
        boolean visible = "true".equals(((Element) children.item(i)).getAttribute("visible"));
        if (isDirectory && visible) {
          numSolutions++;
        }
      }
      Label solutionBrowseLabel = new Label("Browse " + numSolutions + " solution(s)");
      solutionBrowseLabel.setStyleName("numSolutionsLabel");
      navigatorPanel.add(solutionBrowseLabel);

      // get solution paths
      for (int i = 0; i < children.getLength(); i++) {
        Element childElement = (Element) children.item(i);
        String fileName = childElement.getAttribute("name");
        String localizedName = childElement.getAttribute("localized-name");
        boolean isDirectory = "true".equals(childElement.getAttribute("isDirectory"));
        boolean visible = "true".equals(childElement.getAttribute("visible"));
        if (isDirectory && visible) {
          final Label solutionDisclosureHeaderLabel = new Label(localizedName);
          final Image solutionDisclosureHeaderImage = new Image();
          final FlexTable solutionDisclosureHeader = new FlexTable();
          solutionDisclosureHeader.setStyleName("solutionDisclosureHeaderWidget");
          MouseListener headerMouseListener = new MouseListener() {

            public void onMouseDown(Widget sender, int x, int y) {
            }

            public void onMouseEnter(Widget sender) {
              solutionDisclosureHeader.setStyleName("solutionDisclosureHeaderWidgetHover");
            }

            public void onMouseLeave(Widget sender) {
              solutionDisclosureHeader.setStyleName("solutionDisclosureHeaderWidget");
            }

            public void onMouseMove(Widget sender, int x, int y) {
            }

            public void onMouseUp(Widget sender, int x, int y) {
            }

          };
          solutionDisclosureHeaderLabel.addMouseListener(headerMouseListener);
          solutionDisclosureHeaderImage.addMouseListener(headerMouseListener);
          MantleImages.images.plus().applyTo(solutionDisclosureHeaderImage);

          solutionDisclosureHeader.setWidget(0, 0, solutionDisclosureHeaderImage);
          solutionDisclosureHeader.getCellFormatter().setWidth(0, 0, "12px");
          solutionDisclosureHeader.setWidget(0, 1, solutionDisclosureHeaderLabel);
          solutionDisclosureHeader.getCellFormatter().setWidth(0, 1, "100%");
          solutionDisclosureHeader.setWidth("100%");
          DisclosurePanel solutionDisclosurePanel = new DisclosurePanel(solutionDisclosureHeader);
          solutionDisclosurePanel.setAnimationEnabled(false);
          solutionDisclosurePanel.setStyleName("solutionDisclosureWidget");
          solutionDisclosurePanel.addEventHandler(new DisclosureHandler() {

            public void onClose(DisclosureEvent event) {
              MantleImages.images.plus().applyTo(solutionDisclosureHeaderImage);
            }

            public void onOpen(DisclosureEvent event) {
              MantleImages.images.minus().applyTo(solutionDisclosureHeaderImage);
            }

          });
          solutionDisclosurePanel.setContent(buildFolderContent(childElement));
          solutionDisclosurePanel.setWidth("100%");
          navigatorPanel.add(solutionDisclosurePanel);
        }
      }
    } else {
      // find element representing the folder aimed at currentSolutionPath
      navigatorPanel.add(buildFolderContent(getElementByPath(getPathParts(currentSolutionPath), solutionRoot)));
      navigatorPanel.setWidth("100%");
    }
    return navigatorPanel;
  }

  public static Element getElementByPath(List<String> pathParts, Element parentElement) {
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Element childElement = (Element) children.item(i);
      String fileName = childElement.getAttribute("name");
      String pathPart = pathParts.get(0);
      if (pathPart.startsWith("/")) {
        pathPart = pathPart.substring(1);
      }
      if (fileName.equals(pathPart)) {
        pathParts.remove(0);
        if (pathParts.size() > 0) {
          // keep looking
          return getElementByPath(pathParts, childElement);
        } else {
          return childElement;
        }
      }
    }
    return null;
  }

  public static List<String> getPathParts(String fullPath) {
    String currentSolutionPath = "" + fullPath;
    int startIndex = currentSolutionPath.indexOf("/");
    int endIndex = currentSolutionPath.indexOf("/", startIndex + 1);
    if (endIndex == -1) {
      endIndex = currentSolutionPath.length() - 1;
    }
    List<String> pathParts = new ArrayList<String>();
    while (startIndex != -1 && startIndex != endIndex) {
      String pathPart = currentSolutionPath.substring(startIndex, endIndex + 1);
      if (pathPart.endsWith("/")) {
        pathPart = pathPart.substring(0, pathPart.length() - 1);
      }
      pathParts.add(pathPart);
      startIndex = endIndex;
      endIndex = currentSolutionPath.indexOf("/", startIndex + 1);
      if (endIndex == -1) {
        endIndex = currentSolutionPath.length() - 1;
      }
    }
    return pathParts;
  }

  private Widget buildFolderContent(final Element parentElement) {
    // two view modes: icons / list
    // do list mode first

    // possibly use a flow panel for icons mode
    FlexTable contentList = new FlexTable();
    contentList.setStyleName("classicNavigatorTable");
    contentList.setWidth("100%");
    NodeList children = parentElement.getChildNodes();
    contentList.setWidget(0, 0, new Label("Name"));
    contentList.getCellFormatter().setStyleName(0, 0, "classicNavigatorTableHeader");
    // contentList.setWidget(0, 2, new Label("Author"));

    for (int i = 0; i < children.getLength(); i++) {
      final Element childElement = (Element) children.item(i);
      final String fileName = childElement.getAttribute("name");
      String localizedName = childElement.getAttribute("localized-name");
      String description = childElement.getAttribute("description");
      final boolean isDirectory = "true".equals(childElement.getAttribute("isDirectory"));
      final Image icon = new Image();
      final Label entryLabel = new Label(localizedName);
      final HTML entryDecriptionLabel = new HTML(description);
      FlexTable entryPanel = new FlexTable();
      entryPanel.setWidget(0, 0, entryLabel);
      entryPanel.setWidget(1, 0, entryDecriptionLabel);
      entryPanel.setWidth("100%");

      if (isDirectory) {
        MantleImages.images.folder().applyTo(icon);
      } else {
        MantleImages.images.file().applyTo(icon);
      }
      icon.setStyleName("classicNavigatorFileLabel");
      entryLabel.setStyleName("classicNavigatorFileLabel");
      MouseListener headerMouseListener = new MouseListener() {

        public void onMouseDown(Widget sender, int x, int y) {
        }

        public void onMouseEnter(Widget sender) {
          if (isDirectory) {
            MantleImages.images.folderHover().applyTo(icon);
          } else {
            MantleImages.images.fileHover().applyTo(icon);
          }
          entryLabel.setStyleName("classicNavigatorFileLabelHover");
        }

        public void onMouseLeave(Widget sender) {
          if (isDirectory) {
            MantleImages.images.folder().applyTo(icon);
          } else {
            MantleImages.images.file().applyTo(icon);
          }
          entryLabel.setStyleName("classicNavigatorFileLabel");
        }

        public void onMouseMove(Widget sender, int x, int y) {
        }

        public void onMouseUp(Widget sender, int x, int y) {
        }

      };
      ClickListener clickListener = new ClickListener() {
        public void onClick(Widget sender) {
          if (isDirectory) {
            breadCrumbSelected(buildPathForElement(childElement));
          } else {
            // load xaction or .url
            // get the default URL for this
        	  String url = childElement.getAttribute("url");
            if (fileName.endsWith(".xaction")) {
              String fullPath = buildPathForElement(childElement);
              List<String> pathParts = ClassicNavigatorView.getPathParts(fullPath);
              String solution = pathParts.get(0);
              if (solution.startsWith("/")) {
                solution = solution.substring(1);
              }
              String path = "";
              for (int i = 1; i < pathParts.size() - 1; i++) {
                path += pathParts.get(i);
              }
              if (path.startsWith("/")) {
                path = path.substring(1);
              }
              url = "/pentaho/ViewAction?solution=" + solution + "&path=" + path + "&action=" + fileName;
            }
            Window.open(url, "_blank", "");
          }
        }

      };
      icon.addMouseListener(headerMouseListener);
      entryLabel.addMouseListener(headerMouseListener);
      icon.addClickListener(clickListener);
      entryLabel.addClickListener(clickListener);

      FlexTable rowPanel = new FlexTable();
      rowPanel.setWidget(0, 0, icon);
      rowPanel.setWidget(0, 1, entryPanel);

      contentList.setWidget(i + 1, 0, rowPanel);
      contentList.getCellFormatter().setStyleName(i + 1, 0, "classicNavigatorTableCell");
      contentList.getCellFormatter().setWidth(i + 1, 0, "100%");
    }
    return contentList;
  }

  private String buildPathForElement(Element element) {
    List<String> parents = new ArrayList<String>();
    while (element != null) {
      String name = element.getAttribute("name");
      if (name != null && !"".equalsIgnoreCase(name)) {
        parents.add(name);
      }
      Object tmpParent = element.getParentNode();
      if (tmpParent instanceof Element) {
        element = (Element) tmpParent;
      } else {
        element = null;
      }
    }
    Collections.reverse(parents);
    String pathForElement = "";
    for (String pathPart : parents) {
      pathForElement += "/" + pathPart;
    }
    return pathForElement;
  }

  public void breadCrumbSelected(String path) {
    currentSolutionPath = path;
    buildSolutionNavigator();
  }

  public void setSolutionDocument(Document solutionDocument) {
    this.solutionDocument = solutionDocument;
  }

}
