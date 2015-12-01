/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.IValueFieldContextMenu;
import org.eclipse.scout.rt.platform.holders.IHolder;

/**
 * Basic interface for all user fields where user inputs a value
 *
 * @see ITextField, ISmartField, INumberField, IDateField, IFileChooser, IListBox, ITreeBox, ICheckBox, IRadioButton,
 *      IToogleButton
 */
public interface IValueField<VALUE> extends IFormField, IHolder<VALUE>, IContextMenuOwner {
  /*
   * Properties
   */
  String PROP_DISPLAY_TEXT = "displayText";
  String PROP_VALUE = "value";
  String PROP_AUTO_DISPLAY_TEXT = "autoDisplayText";
  String PROP_AUTO_ADD_DEFAULT_MENUS = "autoAddDefaultMenus";
  /**
   * {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenu";

  /**
   * set field value to initValue and clear all error flags
   */
  void resetValue();

  void refreshDisplayText();

  /**
   * Property to signal that component value is being verified for example smartfield text-> value resolution This flag
   * indicates that the ui value of the field changed and that the value needs to be resolved This is a
   * ui-to-model-process. This process contains the subprocess {@link #isValueChanging()}.
   */
  boolean isValueParsing();

  /**
   * Property to signal that component value is being set This flag indicates that the component value changed and
   * validate triggers as well as change triggers are being processed This is a model-to-model-process This process can
   * be part of the {@link #isValueParsing()} process. This process contains the subprocess {@link #isValueValidating()}
   * .
   */
  boolean isValueChanging();

  /**
   * Property to signal that component value is being validated This flag indicates that the component value changed and
   * validate triggers as well as change triggers are being processed This is a model-to-model-process This process is
   * part of the {@link #isValueChanging()} process.
   */
  boolean isValueValidating();

  void addMasterListener(MasterListener listener);

  void removeMasterListener(MasterListener listener);

  /**
   * Value
   */
  VALUE getInitValue();

  void setInitValue(VALUE initValue);

  /**
   * get currently validated value
   */
  @Override
  VALUE getValue();

  /**
   * set a new value The new value is validated calls validateInternal and then delegates to execValidateValue after
   * setting the value calls execChangedValue
   */
  @Override
  void setValue(VALUE o);

  /**
   * @deprecated Will be removed with scout 7. Use {@link IValueField#parseAndSetValue(String)}
   */
  @Deprecated
  boolean parseValue(String text);

  /**
   * Parses and sets either the value or an errorStatus, if parsing or validation fails.
   *
   * @param text
   */
  void parseAndSetValue(String text);

  String getDisplayText();

  void setDisplayText(String s);

  boolean isAutoDisplayText();

  void setAutoDisplayText(boolean b);

  /**
   * force a fire of valueChanged even if the value has not changed This can be used to manually re-trigger value change
   * processing in complex business logic
   *
   * @see setValue()
   */
  void fireValueChanged();

  @Override
  IValueFieldContextMenu getContextMenu();

  /**
   * Gets if the default system menus (cut, copy, paste) should be added automatically to the menus of this field.
   *
   * @return true if the default system menus should be available automatically, false otherwise.
   */
  boolean isAutoAddDefaultMenus();

  /**
   * Sets if the default system menus (cut, copy, paste) should be added automatically to the menus of this field.
   *
   * @param b
   *          true if the menus should be available automatically, false otherwise.
   */
  void setAutoAddDefaultMenus(boolean b);

}
