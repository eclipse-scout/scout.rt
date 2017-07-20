package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * Common UI facade for value fields with value, displayText and error-status.
 * 
 * @since 7.0
 */
public interface IValueFieldUIFacade<VALUE> {

  void setValueFromUI(VALUE value);

  void setDisplayTextFromUI(String text);

  void setErrorStatusFromUI(IStatus errorStatus);

}
