package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

/**
 * Convenience class that defines common dialog controller functionality. <code>T</code> refers to the type of the value
 * returned when the user "accepts" the dialog (e.g. clicks OK). This type must match the type set on the
 * {@link DialogListener}.
 * 
 * @author mlowery
 */
/*
 * TODO mlowery Move this class to the pentaho-xul-core project.
 */
public abstract class AbstractXulDialogController<T> extends AbstractXulEventHandler implements DialogController<T> {



  // ~ Static fields/initializers ======================================================================================
  // ~ Instance fields =================================================================================================
  private List<DialogListener<T>> listeners = new ArrayList<DialogListener<T>>();

  // ~ Constructors ====================================================================================================

  // ~ Methods =========================================================================================================

  /**
   * Subclasses must override this method to return an instance of <code>XulDialog</code> so that this controller can
   * call <code>show()</code> and <code>hide()</code> on it.
   */
  protected abstract XulDialog getDialog();

  /**
   * Value returned by this method is returned to listeners during <code>onDialogAccept()</code>.
   */
  protected abstract T getDialogResult();

  public void showDialog() {
    getDialog().show();
  }

  /**
   * Called when the accept button is clicked.
   */
  public void onDialogAccept() {
    hideDialog();
    for (DialogListener<T> listener : listeners) {
      listener.onDialogAccept(getDialogResult());
    }
  }

  /**
   * Called when the cancel button is clicked.
   */
  public void onDialogCancel() {
    hideDialog();
    for (DialogListener<T> listener : listeners) {
      listener.onDialogCancel();
    }
  }

  public void addDialogListener(final DialogListener<T> listener) {
    listeners.add(listener);
  }

  public void removeDialogListener(final DialogListener<T> listener) {
    listeners.remove(listener);
  }

  public void hideDialog() {
    getDialog().hide();
  }

}
