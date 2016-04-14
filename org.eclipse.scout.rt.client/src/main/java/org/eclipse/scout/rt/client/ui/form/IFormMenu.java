package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * @since 6.0
 */
public interface IFormMenu<FORM extends IForm> extends IMenu {
  String PROP_FORM = "form";

  FORM getForm();

  /**
   * Set a new <b>started</b> form to the menu.
   * <p>
   * The form is shown whenever the menu is selected.
   */
  void setForm(FORM f);
}
