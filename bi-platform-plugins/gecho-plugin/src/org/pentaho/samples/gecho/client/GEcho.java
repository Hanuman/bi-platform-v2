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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created May 20, 2009
 * @author Aaron Phillips
 */

package org.pentaho.samples.gecho.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GEcho implements EntryPoint {

  private VerticalPanel mainPanel = new VerticalPanel();

  private HorizontalPanel echoPanel = new HorizontalPanel();

  private Button echoMessageButton = new Button("Get Server Message");

  private Label serverResponseLabel = new Label();

  private Label debugLabel = new Label();

  public void onModuleLoad() {

    echoPanel.setSpacing(20);
    echoPanel.add(echoMessageButton);
    echoPanel.add(serverResponseLabel);
    echoPanel.add(debugLabel); //uncomment to see the request URL

    mainPanel.add(echoPanel);

    RootPanel.get("gechodiv").add(mainPanel);

    echoMessageButton.setFocus(true);

    echoMessageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getServerMessage();
      }
    });

  }

  /** 
   * This hackery has to be done so we get to the /pentaho context where our 
   * gecho servlet lives (/pentaho/gecho/service).  If we don't parse out the plugin-related
   * parts of the module url,  the GWT client code will wrongly POST to /pentaho/content/gecho-res/gecho/service.
   * 
   * @return the true URL to the rpc service
   */
  private String getBaseUrl() {
    String moduleUrl = GWT.getModuleBaseURL();
    
    //
    //Set the base url appropriately based on the context in which we are running this client
    //
    if(moduleUrl.indexOf("content") > -1) {
      //we are running the client in the context of a BI Server plugin, so 
      //point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));
      //NOTE: the dispatch URL ("gechoService") must match the bean id for 
      //this service object in your plugin.xml.  "gwtrpc" is the servlet 
      //that handles plugin gwt rpc requests in the BI Server.
      return  baseUrl + "gwtrpc/gechoService";
    }
    //we are running this client in hosted mode, so point to the servlet 
    //defined in war/WEB-INF/web.xml
    return moduleUrl + "gwtrpc";
  }

  private void getServerMessage() {
    GEchoServiceAsync gechoService = GWT.create(GEchoService.class);

    ServiceDefTarget endpoint = (ServiceDefTarget) gechoService;
    endpoint.setServiceEntryPoint(getBaseUrl());

    debugLabel.setText("posting to: " + endpoint.getServiceEntryPoint());

    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        serverResponseLabel.setText("Error communicating with GEchoService: " + caught.toString());
        caught.printStackTrace();
      }

      public void onSuccess(String result) {
        serverResponseLabel.setText(result);
      }
    };

    gechoService.echo("GEcho client", callback);
  }
}
