/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.server;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;

public class MantleStyleManager extends HttpServlet {

  public static final String MENUBAR_BACKGROUND_COLOR = "MANTLE_STYLE_MENUBAR_BACKGROUND_COLOR";
  public static final String MENUBAR_BACKGROUND_COLOR_DEFAULT = "#87944C";
  public static final String MENUBAR_BACKGROUND_COLOR_DISPLAY_NAME = "Menubar Background Color";

  public static final String BODY_BACKGROUND_COLOR = "MANTLE_STYLE_BODY_BACKGROUND_COLOR";
  public static final String BODY_BACKGROUND_COLOR_DEFAULT = "white";
  public static final String BODY_BACKGROUND_COLOR_DISPLAY_NAME = "Body Background Color";

  public static final String SPLITPANEL_DIVIDER_COLOR = "MANTLE_STYLE_SPLITPANEL_DIVIDER_COLOR";
  public static final String SPLITPANEL_DIVIDER_COLOR_DEFAULT = "#87944C";
  public static final String SPLITPANEL_DIVIDER_COLOR_DISPLAY_NAME = "Split-panel Divider Color";

  public static final String SPLITPANEL_DIVIDER_WIDTH = "MANTLE_STYLE_SPLITPANEL_DIVIDER_WIDTH";
  public static final String SPLITPANEL_DIVIDER_WIDTH_DEFAULT = "10px";
  public static final String SPLITPANEL_DIVIDER_WIDTH_DISPLAY_NAME = "Split-panel Divider Width";

  public static final String HORIZONTAL_MENUITEM_COLOR = "MANTLE_STYLE_HORIZONTAL_MENUITEM_COLOR";
  public static final String HORIZONTAL_MENUITEM_COLOR_DEFAULT = "white";
  public static final String HORIZONTAL_MENUITEM_COLOR_DISPLAY_NAME = "Horizontal Menu-Item Color";

  public static final String HORIZONTAL_SELECTED_MENUITEM_COLOR = "MANTLE_STYLE_HORIZONTAL_SELECTED_MENUITEM_COLOR";
  public static final String HORIZONTAL_SELECTED_MENUITEM_COLOR_DEFAULT = "black";
  public static final String HORIZONTAL_SELECTED_MENUITEM_COLOR_DISPLAY_NAME = "Horizontal Selected Menu-Item Color";

  public static final String HORIZONTAL_MENUITEM_BACKGROUND_COLOR = "MANTLE_STYLE_HORIZONTAL_MENUITEM_BACKGROUND_COLOR";
  public static final String HORIZONTAL_MENUITEM_BACKGROUND_COLOR_DEFAULT = "#87944C";
  public static final String HORIZONTAL_MENUITEM_BACKGROUND_COLOR_DISPLAY_NAME = "Horizontal Menu-Item Background Color";

  public static final String HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR = "MANTLE_STYLE_HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR";
  public static final String HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR_DEFAULT = "#f1a500";
  public static final String HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR_DISPLAY_NAME = "Horizontal Selected Menu-Item Background Color";

  public static final String VERTICAL_MENUITEM_COLOR = "MANTLE_STYLE_VERTICAL_MENUITEM_COLOR";
  public static final String VERTICAL_MENUITEM_COLOR_DEFAULT = "black";
  public static final String VERTICAL_MENUITEM_COLOR_DISPLAY_NAME = "Vertical Menu-Item Color";

  public static final String VERTICAL_SELECTED_MENUITEM_COLOR = "MANTLE_STYLE_VERTICAL_SELECTED_MENUITEM_COLOR";
  public static final String VERTICAL_SELECTED_MENUITEM_COLOR_DEFAULT = "black";
  public static final String VERTICAL_SELECTED_MENUITEM_COLOR_DISPLAY_NAME = "Vertical Selected Menu-Item Color";

  public static final String VERTICAL_MENUITEM_BACKGROUND_COLOR = "MANTLE_STYLE_VERTICAL_MENUITEM_BACKGROUND_COLOR";
  public static final String VERTICAL_MENUITEM_BACKGROUND_COLOR_DEFAULT = "#fef9ce";
  public static final String VERTICAL_MENUITEM_BACKGROUND_COLOR_DISPLAY_NAME = "Vertical Menu-Item Background Color";

  public static final String VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR = "MANTLE_STYLE_VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR";
  public static final String VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR_DEFAULT = "#f1a500";
  public static final String VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR_DISPLAY_NAME = "Vertical Selected Menu-Item Background Color";

  public static final String TAB_PANEL_DECORATOR_COLOR = "MANTLE_STYLE_TAB_PANEL_DECORATOR_COLOR";
  public static final String TAB_PANEL_DECORATOR_COLOR_DEFAULT = "#d0d0d0";
  public static final String TAB_PANEL_DECORATOR_COLOR_DISPLAY_NAME = "Tab-panel Decorator Color";

  public static final String TAB_PANEL_COLOR = "MANTLE_STYLE_TAB_PANEL_COLOR";
  public static final String TAB_PANEL_COLOR_DEFAULT = "black";
  public static final String TAB_PANEL_COLOR_DISPLAY_NAME = "Default Tab Color";

  public static final String TAB_PANEL_SELECTED_COLOR = "MANTLE_STYLE_TAB_PANEL_SELECTED_COLOR";
  public static final String TAB_PANEL_SELECTED_COLOR_DEFAULT = "black";
  public static final String TAB_PANEL_SELECTED_COLOR_DISPLAY_NAME = "Selected Tab Color";

  public static final String TAB_PANEL_BACKGROUND_COLOR = "MANTLE_STYLE_TAB_PANEL_BACKGROUND_COLOR";
  public static final String TAB_PANEL_BACKGROUND_COLOR_DEFAULT = "white";
  public static final String TAB_PANEL_BACKGROUND_COLOR_DISPLAY_NAME = "Tab Panel Background Color";

  public static final String TAB_PANEL_CONTENT_BACKGROUND_IMAGE = "MANTLE_STYLE_TAB_PANEL_CONTENT_BACKGROUND_IMAGE";
  public static final String TAB_PANEL_CONTENT_BACKGROUND_IMAGE_DEFAULT = "mantle/logo-small.png";
  public static final String TAB_PANEL_CONTENT_BACKGROUND_IMAGE_DISPLAY_NAME = "Content Tab Panel Background Image";

  public static final String TAB_PANEL_CONTENT_BACKGROUND_REPEAT = "MANTLE_STYLE_TAB_PANEL_CONTENT_BACKGROUND_REPEAT";
  public static final String TAB_PANEL_CONTENT_BACKGROUND_REPEAT_DEFAULT = "repeat";
  public static final String TAB_PANEL_CONTENT_BACKGROUND_REPEAT_DISPLAY_NAME = "Content Tab Panel Background Image Repeat";

  public static final String DIALOGBOX_CAPTION_BACKGROUND_COLOR = "MANTLE_STYLE_DIALOGBOX_CAPTION_BACKGROUND_COLOR";
  public static final String DIALOGBOX_CAPTION_BACKGROUND_COLOR_DEFAULT = "#d3d3d3";
  public static final String DIALOGBOX_CAPTION_BACKGROUND_COLOR_DISPLAY_NAME = "Dialog Box Caption Background Color";

  public static final String DIALOGBOX_CAPTION_COLOR = "MANTLE_STYLE_DIALOGBOX_CAPTION_COLOR";
  public static final String DIALOGBOX_CAPTION_COLOR_DEFAULT = "black";
  public static final String DIALOGBOX_CAPTION_COLOR_DISPLAY_NAME = "Dialog Box Caption Color";

  // this class should handle these cases (as a web service):
  // 1. return current styles (xml document?)
  // 2. set style (name/value)
  // 3. get style (name/value)
  // 4. this should work at the user and/or global level (flag)
  // only an administrator can actually set global values

  protected static IPentahoSession getPentahoSession(final HttpServletRequest request) {
    return PentahoHttpSessionHelper.getPentahoSession(request);
  }

  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    PentahoSystem.systemEntryPoint();
    try {
      response.setContentType("text/xml"); //$NON-NLS-1$
      response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

      // send the header of the message to prevent time-outs while we are working
      response.setHeader("expires", "0"); //$NON-NLS-1$ //$NON-NLS-2$

      org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      String method = request.getParameter("method");
      if (method.equalsIgnoreCase("getAvailableStyles")) {
        org.w3c.dom.Element root = document.createElement("styles");
        document.appendChild(root);
        List<String> styles = getAvailableStyles();
        for (String style : styles) {
          org.w3c.dom.Element styleNode = document.createElement("style");
          styleNode.setAttribute("name", style);
          styleNode.setAttribute("displayName", getStyleDisplayName(style));
          styleNode.setAttribute("globalValue", getGlobalStyle(request, style));
          styleNode.setAttribute("effectiveValue", getEffectiveStyle(request, style));
          styleNode.setAttribute("defaultValue", getStyleDefaultValue(style));
          root.appendChild(styleNode);
        }
      } else if (method.equalsIgnoreCase("getStyle")) {
        org.w3c.dom.Element root = document.createElement("style");
        document.appendChild(root);
        String style = request.getParameter("style");
        org.w3c.dom.Element styleNode = document.createElement("style");
        styleNode.setAttribute("name", style);
        styleNode.setAttribute("displayName", getStyleDisplayName(style));
        styleNode.setAttribute("globalValue", getGlobalStyle(request, style));
        styleNode.setAttribute("effectiveValue", getEffectiveStyle(request, style));
        styleNode.setAttribute("defaultValue", getStyleDefaultValue(style));
        root.appendChild(styleNode);
      } else if (method.equalsIgnoreCase("setStyle")) {
        String style = request.getParameter("style");
        String value = request.getParameter("value");
        boolean isGlobal = "true".equals(request.getParameter("global"));
        setStyle(request, style, value, isGlobal);

        org.w3c.dom.Element root = document.createElement("style");
        document.appendChild(root);
        org.w3c.dom.Element styleNode = document.createElement("style");
        styleNode.setAttribute("name", style);
        styleNode.setAttribute("displayName", getStyleDisplayName(style));
        styleNode.setAttribute("globalValue", getGlobalStyle(request, style));
        styleNode.setAttribute("effectiveValue", getEffectiveStyle(request, style));
        styleNode.setAttribute("defaultValue", getStyleDefaultValue(style));
        root.appendChild(styleNode);
      }

      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(new StringWriter());
      TransformerFactory.newInstance().newTransformer().transform(source, result);
      String theXML = result.getWriter().toString();

      response.getOutputStream().write(theXML.getBytes());
      response.getOutputStream().close();

    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  public static List<String> getAvailableStyles() {
    Field[] fields = MantleStyleManager.class.getDeclaredFields();
    List<String> availableStyleFields = new ArrayList<String>();
    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL && (!field.getName().endsWith("DEFAULT") && (!field.getName().endsWith("DISPLAY_NAME")))) {
        availableStyleFields.add(field.getName());
      }
    }
    Collections.sort(availableStyleFields);
    return availableStyleFields;
  }

  private static String getStyleFieldName(String styleName) {
    Field[] fields = MantleStyleManager.class.getDeclaredFields();
    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL && (field.getName().equals(styleName))) {
        try {
          return field.get("").toString();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  private static String getStyleDefaultValue(String styleName) {
    Field[] fields = MantleStyleManager.class.getDeclaredFields();
    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL && (field.getName().startsWith(styleName)) && (field.getName().endsWith("DEFAULT"))) {
        try {
          return field.get("").toString();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static void setStyle(final HttpServletRequest request, String styleName, String styleValue, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(getStyleFieldName(styleName), styleValue);
    } else {
      service.setUserSetting(getStyleFieldName(styleName), styleValue);
    }
  }

  public static String getStyleDisplayName(String styleName) {
    Field[] fields = MantleStyleManager.class.getDeclaredFields();
    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL && (field.getName().startsWith(styleName)) && (field.getName().endsWith("DISPLAY_NAME"))) {
        try {
          return field.get("").toString();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static String getGlobalStyle(final HttpServletRequest request, String styleName) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getGlobalUserSetting(getStyleFieldName(styleName), getStyleDefaultValue(styleName)).getSettingValue();
  }

  public static String getEffectiveStyle(final HttpServletRequest request, String styleName) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(getStyleFieldName(styleName), getStyleDefaultValue(styleName)).getSettingValue();
  }

  public static void main(String args[]) throws IllegalArgumentException, IllegalAccessException {
    Field[] fields = MantleStyleManager.class.getDeclaredFields();
    for (Field field : fields) {
      if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL && (!field.getName().endsWith("DEFAULT"))) {
        System.out.println(field.getName());
        System.out.println(getStyleFieldName(field.getName()));
        System.out.println(getStyleDefaultValue(field.getName()));
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // api methods
  // ///////////////////////////////////////////////////////////////////////////////////////////////////////

  public static void setMenuBarBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(MENUBAR_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(MENUBAR_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getMenuBarBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(MENUBAR_BACKGROUND_COLOR, MENUBAR_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setBodyBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(BODY_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(BODY_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getBodyBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(BODY_BACKGROUND_COLOR, BODY_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setSplitPanelDividerColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(SPLITPANEL_DIVIDER_COLOR, backgroundColor);
    } else {
      service.setUserSetting(SPLITPANEL_DIVIDER_COLOR, backgroundColor);
    }
  }

  public static String getSplitPanelDividerColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(SPLITPANEL_DIVIDER_COLOR, SPLITPANEL_DIVIDER_COLOR_DEFAULT).getSettingValue();
  }

  public static void setSplitPanelDividerWidth(final HttpServletRequest request, String width, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(SPLITPANEL_DIVIDER_WIDTH, width);
    } else {
      service.setUserSetting(SPLITPANEL_DIVIDER_WIDTH, width);
    }
  }

  public static String getSplitPanelDividerWidth(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(SPLITPANEL_DIVIDER_WIDTH, SPLITPANEL_DIVIDER_WIDTH_DEFAULT).getSettingValue();
  }

  public static void setHorizontalSelectedMenuItemColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(HORIZONTAL_SELECTED_MENUITEM_COLOR, backgroundColor);
    } else {
      service.setUserSetting(HORIZONTAL_SELECTED_MENUITEM_COLOR, backgroundColor);
    }
  }

  public static String getHorizontalSelectedMenuItemColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(HORIZONTAL_SELECTED_MENUITEM_COLOR, HORIZONTAL_SELECTED_MENUITEM_COLOR_DEFAULT).getSettingValue();
  }

  public static void setHorizontalMenuItemColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(HORIZONTAL_MENUITEM_COLOR, backgroundColor);
    } else {
      service.setUserSetting(HORIZONTAL_MENUITEM_COLOR, backgroundColor);
    }
  }

  public static String getHorizontalMenuItemColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(HORIZONTAL_MENUITEM_COLOR, HORIZONTAL_MENUITEM_COLOR_DEFAULT).getSettingValue();
  }

  public static void setHorizontalMenuItemBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(HORIZONTAL_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(HORIZONTAL_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getHorizontalMenuItemBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(HORIZONTAL_MENUITEM_BACKGROUND_COLOR, HORIZONTAL_MENUITEM_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setHorizontalSelectedMenuItemBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getHorizontalSelectedMenuItemBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR, HORIZONTAL_SELECTED_MENUITEM_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setVerticalSelectedMenuItemColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(VERTICAL_SELECTED_MENUITEM_COLOR, backgroundColor);
    } else {
      service.setUserSetting(VERTICAL_SELECTED_MENUITEM_COLOR, backgroundColor);
    }
  }

  public static String getVerticalSelectedMenuItemColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(VERTICAL_SELECTED_MENUITEM_COLOR, VERTICAL_SELECTED_MENUITEM_COLOR_DEFAULT).getSettingValue();
  }

  public static void setVerticalMenuItemColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(VERTICAL_MENUITEM_COLOR, backgroundColor);
    } else {
      service.setUserSetting(VERTICAL_MENUITEM_COLOR, backgroundColor);
    }
  }

  public static String getVerticalMenuItemColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(VERTICAL_MENUITEM_COLOR, VERTICAL_MENUITEM_COLOR_DEFAULT).getSettingValue();
  }

  public static void setVerticalMenuItemBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(VERTICAL_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(VERTICAL_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getVerticalMenuItemBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(VERTICAL_MENUITEM_BACKGROUND_COLOR, VERTICAL_MENUITEM_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setVerticalSelectedMenuItemBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getVerticalSelectedMenuItemBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR, VERTICAL_SELECTED_MENUITEM_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setTabPanelDecoratorColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(TAB_PANEL_DECORATOR_COLOR, backgroundColor);
    } else {
      service.setUserSetting(TAB_PANEL_DECORATOR_COLOR, backgroundColor);
    }
  }

  public static String getTabPanelDecoratorColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(TAB_PANEL_DECORATOR_COLOR, TAB_PANEL_DECORATOR_COLOR_DEFAULT).getSettingValue();
  }

  public static void setTabPanelColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(TAB_PANEL_COLOR, backgroundColor);
    } else {
      service.setUserSetting(TAB_PANEL_COLOR, backgroundColor);
    }
  }

  public static String getTabPanelColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(TAB_PANEL_COLOR, TAB_PANEL_COLOR_DEFAULT).getSettingValue();
  }

  public static void setTabPanelSelectedColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(TAB_PANEL_SELECTED_COLOR, backgroundColor);
    } else {
      service.setUserSetting(TAB_PANEL_SELECTED_COLOR, backgroundColor);
    }
  }

  public static String getTabPanelSelectedColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(TAB_PANEL_SELECTED_COLOR, TAB_PANEL_SELECTED_COLOR_DEFAULT).getSettingValue();
  }

  public static void setContentTabPanelBackgroundImage(final HttpServletRequest request, String backgroundImage, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(TAB_PANEL_CONTENT_BACKGROUND_IMAGE, backgroundImage);
    } else {
      service.setUserSetting(TAB_PANEL_CONTENT_BACKGROUND_IMAGE, backgroundImage);
    }
  }

  public static String getContentTabPanelBackgroundImage(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(TAB_PANEL_CONTENT_BACKGROUND_IMAGE, TAB_PANEL_CONTENT_BACKGROUND_IMAGE_DEFAULT).getSettingValue();
  }

  public static void setContentTabPanelBackgroundRepeat(final HttpServletRequest request, String repeat, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(TAB_PANEL_CONTENT_BACKGROUND_REPEAT, repeat);
    } else {
      service.setUserSetting(TAB_PANEL_CONTENT_BACKGROUND_REPEAT, repeat);
    }
  }

  public static String getContentTabPanelBackgroundRepeat(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(TAB_PANEL_CONTENT_BACKGROUND_REPEAT, TAB_PANEL_CONTENT_BACKGROUND_REPEAT_DEFAULT).getSettingValue();
  }

  public static void setTabPanelBackgroundColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(TAB_PANEL_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(TAB_PANEL_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getTabPanelBackgroundColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(TAB_PANEL_BACKGROUND_COLOR, TAB_PANEL_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setDialogBoxCaptionBackground(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(DIALOGBOX_CAPTION_BACKGROUND_COLOR, backgroundColor);
    } else {
      service.setUserSetting(DIALOGBOX_CAPTION_BACKGROUND_COLOR, backgroundColor);
    }
  }

  public static String getDialogBoxCaptionBackground(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(DIALOGBOX_CAPTION_BACKGROUND_COLOR, DIALOGBOX_CAPTION_BACKGROUND_COLOR_DEFAULT).getSettingValue();
  }

  public static void setDialogBoxCaptionColor(final HttpServletRequest request, String backgroundColor, boolean isGlobal) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    if (isGlobal) {
      service.setGlobalUserSetting(DIALOGBOX_CAPTION_COLOR, backgroundColor);
    } else {
      service.setUserSetting(DIALOGBOX_CAPTION_COLOR, backgroundColor);
    }
  }

  public static String getDialogBoxCaptionColor(final HttpServletRequest request) {
    IPentahoSession userSession = getPentahoSession(request);
    IUserSettingService service = PentahoSystem.get(IUserSettingService.class, userSession);
    return service.getUserSetting(DIALOGBOX_CAPTION_COLOR, DIALOGBOX_CAPTION_COLOR_DEFAULT).getSettingValue();
  }

}
