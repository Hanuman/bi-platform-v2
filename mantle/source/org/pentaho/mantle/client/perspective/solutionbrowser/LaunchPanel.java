package org.pentaho.mantle.client.perspective.solutionbrowser;

import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.ManageContentCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class LaunchPanel extends VerticalPanel implements ClickListener {
  Image launchWaqrImage = new Image();
  Image launchAnalysisViewImage = new Image();
  Image manageContentImage = new Image();
  SolutionBrowserPerspective perspective;

  public LaunchPanel(SolutionBrowserPerspective perspective) {

    this.perspective = perspective;

    FlexTable table = new FlexTable();
    table.setStyleName("launchButtonPanel");

    if (GWT.isScript()) {
      launchWaqrImage.setUrl("/pentaho/mantle/btn_ql_newreport.png");
    } else {
      launchWaqrImage.setUrl("btn_ql_newreport.png");
    }
    launchWaqrImage.setTitle(Messages.getInstance().newAdhocReport());
    launchWaqrImage.addClickListener(this);
    
    
    if (GWT.isScript()) {
      launchAnalysisViewImage.setUrl("/pentaho/mantle/btn_ql_newanalysis.png");
    } else {
      launchAnalysisViewImage.setUrl("btn_ql_newanalysis.png");
    }
    launchAnalysisViewImage.setTitle(Messages.getInstance().newAnalysisView());
    launchAnalysisViewImage.addClickListener(this);

    
    if (GWT.isScript()) {
      manageContentImage.setUrl("/pentaho/mantle/btn_ql_browse.png");
    } else {
      manageContentImage.setUrl("btn_ql_browse.png");
    }
    manageContentImage.setTitle(Messages.getInstance().manageContent());
    manageContentImage.addClickListener(this);

    launchWaqrImage.setStyleName("launchImage");
    launchAnalysisViewImage.setStyleName("launchImage");
    manageContentImage.setStyleName("launchImage");

    // set the style of contentTabPanel's "deck" (bottom)
    setStyleName("launchPanel");

    // set debug id's for selenium
    launchWaqrImage.getElement().setAttribute("id", "launch_new_report");
    launchAnalysisViewImage.getElement().setAttribute("id", "launch_new_analysis");
    manageContentImage.getElement().setAttribute("id", "manage_content");
    
    table.setWidget(0, 0, launchWaqrImage);
    table.setWidget(0, 1, launchAnalysisViewImage);
    table.setWidget(0, 2, manageContentImage);
    table.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
    table.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
    table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);

    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    add(table);
  }

  public void onClick(Widget sender) {
    // do the action
    if (sender == launchWaqrImage) {
      WAQRCommand command = new WAQRCommand(perspective);
      command.execute();
    } else if (sender == launchAnalysisViewImage) {
      AnalysisViewCommand command = new AnalysisViewCommand(perspective);
      command.execute();
    } else if (sender == manageContentImage) {
      ManageContentCommand command = new ManageContentCommand(perspective);
      command.execute();
    }
  }

}
