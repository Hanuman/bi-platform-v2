package org.pentaho.mantle.client.perspective.solutionbrowser.reporting;

import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.ReportParameter;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ReportView extends VerticalPanel {

  HTML htmlReportContent = new HTML();
  ScrollPanel htmlScroller = new ScrollPanel(htmlReportContent);
  String reportPath = null;
  ReportContainer reportContainer = null;
  ReportParameterUI parameterPanel = new ReportParameterUI();
  int logicalPage = 0;
  VerticalPanel parameterSlot = new VerticalPanel();
  VerticalPanel pageSlot = new VerticalPanel();
  VerticalPanel reportSlot = new VerticalPanel();

  IParameterSubmissionCallback parameterSubmitter = new IParameterSubmissionCallback() {
    public void submitReportParameters() {
      // parameters updated, destroy report cache
      logicalPage = 0;
      ReportCache.removeReport(reportPath);
      fetchLogicalPage();
    }
  };

  public ReportView(final String reportKey, final ReportContainer reportContainer) {
    this.reportPath = reportKey;
    this.reportContainer = reportContainer;
    initUI();
    loadReportUI(false);
  }

  private void initUI() {
    setWidth("100%");

    htmlScroller.setHeight("100%");
    reportSlot.add(htmlScroller);

    parameterSlot.setWidth("100%");
    pageSlot.setWidth("100%");
    reportSlot.setWidth("100%");

    add(parameterSlot);
    add(pageSlot);
    add(reportSlot);
  }

  private void loadReportUI(boolean fromCache) {
    List<ReportParameter> reportParameters = reportContainer.getReportParameters();
    if (reportParameters != null && reportParameters.size() > 0) {
      parameterPanel.init(reportContainer, reportParameters, parameterSubmitter);
      parameterSlot.clear();
      parameterSlot.add(parameterPanel);
    }

    if (!reportContainer.isPromptNeeded()) {
      ReportCache.addReportPages(reportPath, reportContainer);
      ReportCache.setNumberOfReportPages(reportPath, reportContainer.getNumPages());

      Widget pageControlPanel = buildPageController();
      pageSlot.clear();
      pageSlot.add(pageControlPanel);

      if (fromCache) {
        htmlReportContent.setHTML(ReportCache.getReportPage(reportPath, logicalPage));
      } else {
        htmlReportContent.setHTML(reportContainer.getReportPages().get(logicalPage));
      }
    }
  }

  private void fetchLogicalPage() {
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        if (result instanceof ReportContainer) {
          reportContainer = (ReportContainer) result;
          ReportCache.addReportPages(reportPath, reportContainer);
          ReportCache.setNumberOfReportPages(reportPath, reportContainer.getNumPages());
        }
        loadReportUI(true);
      }
    };

    String reportHTML = ReportCache.getReportPage(reportPath, logicalPage);
    if (reportHTML == null) {
      MantleServiceCache.getService().getLogicalReportPage(reportContainer.getReportParameters(), reportPath, logicalPage, callback);
    } else {
      loadReportUI(true);
    }
  }

  private Widget buildPageController() {
    final Label errorLabel = new Label();
    errorLabel.setStyleName("errorLabel");
    errorLabel.setWidth("100%");
    final TextBox pageField = new TextBox();
    pageField.setVisibleLength(4);
    pageField.setText("" + (logicalPage + 1));
    pageField.setTextAlignment(TextBox.ALIGN_RIGHT);
    pageField.addKeyboardListener(new KeyboardListener() {

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        if (KeyboardListener.KEY_ENTER == keyCode) {
          try {
            int page = Integer.parseInt(pageField.getText());
            if (page < 1 || page > reportContainer.getNumPages()) {
              errorLabel.setText("Page must be between 1 and " + reportContainer.getNumPages() + ".");
              return;
            }
            errorLabel.setText("");
            logicalPage = page - 1;
            fetchLogicalPage();
          } catch (Exception e) {
            errorLabel.setText("Invalid page number: " + pageField.getText());
          }
        }
      }

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
      }

    });

    Image nextPageButton = new Image();
    MantleImages.images.forwardButton().applyTo(nextPageButton);
    nextPageButton.setStyleName("reportPageControl");
    nextPageButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        errorLabel.setText("");
        if (logicalPage < reportContainer.getNumPages() - 1) {
          logicalPage++;
          fetchLogicalPage();
        }
      }

    });
    Image lastPageButton = new Image();
    MantleImages.images.forwardToLastPage().applyTo(lastPageButton);
    lastPageButton.setStyleName("reportPageControl");
    lastPageButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        errorLabel.setText("");
        logicalPage = reportContainer.getNumPages() - 1;
        fetchLogicalPage();
      }

    });

    Image previousPageButton = new Image();
    MantleImages.images.backButton().applyTo(previousPageButton);
    previousPageButton.setStyleName("reportPageControl");
    previousPageButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        errorLabel.setText("");
        if (logicalPage > 0) {
          logicalPage--;
          fetchLogicalPage();
        }
      }

    });
    Image firstPageButton = new Image();
    MantleImages.images.backToFirstPage().applyTo(firstPageButton);
    firstPageButton.setStyleName("reportPageControl");
    firstPageButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        errorLabel.setText("");
        logicalPage = 0;
        fetchLogicalPage();
      }

    });

    FlexTable pageControlTable = new FlexTable();
    pageControlTable.setWidget(0, 0, firstPageButton);
    pageControlTable.setWidget(0, 1, previousPageButton);
    pageControlTable.setWidget(0, 2, new Label("Page"));
    pageControlTable.setWidget(0, 3, pageField);
    pageControlTable.setWidget(0, 4, new Label("of " + reportContainer.getNumPages()));
    pageControlTable.setWidget(0, 5, nextPageButton);
    pageControlTable.setWidget(0, 6, lastPageButton);
    pageControlTable.setWidget(0, 7, errorLabel);

    HorizontalPanel pageControlPanel = new HorizontalPanel();
    pageControlPanel.setWidth("100%");
    pageControlPanel.setStyleName("pageControlPanel");
    pageControlPanel.add(pageControlTable);

    return pageControlPanel;
  }

}
