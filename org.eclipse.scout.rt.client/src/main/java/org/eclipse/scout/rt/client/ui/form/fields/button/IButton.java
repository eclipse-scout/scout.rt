/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

/**
 * A Button has 2 aspects
 * <ol>
 * <li>System-button / NonSystem-button is marked by getSystemType()<br>
 * System buttons in a dialog have a pre-defined action handling
 * <li>Process-button / NonProcess-button is marked by isProcessButton()<br>
 * Process buttons are normally placed on dialogs button bar on the lower dialog bar
 * </ol>
 */
public interface IButton extends IFormField {
  /*
   * Properties
   */
  String PROP_ICON_ID = "iconId";
  String PROP_SELECTED = "selected";
  String PROP_KEY_STROKE = "keyStroke";
  String PROP_KEY_STROKE_SCOPE_CLASS = "keyStrokeScopeClass";
  String PROP_KEY_STROKE_SCOPE = "keyStrokeScope";
  String PROP_PREVENT_DOUBLE_CLICK = "preventDoubleClick";
  String PROP_DEFAULT_BUTTON = "defaultButton";
  String PROP_STACKABLE = "stackable";
  String PROP_SHRINKABLE = "shrinkable";

  /*
   * System Types
   */
  int SYSTEM_TYPE_NONE = 0;
  int SYSTEM_TYPE_CANCEL = 1;
  int SYSTEM_TYPE_CLOSE = 2;
  int SYSTEM_TYPE_OK = 3;
  int SYSTEM_TYPE_RESET = 4;
  int SYSTEM_TYPE_SAVE = 5;
  int SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE = 6;
  /*
   * Display Styles
   */
  int DISPLAY_STYLE_DEFAULT = 0;
  int DISPLAY_STYLE_TOGGLE = 1;
  int DISPLAY_STYLE_RADIO = 2;
  int DISPLAY_STYLE_LINK = 3;
  int DISPLAY_STYLE_BORDERLESS = 4;

  void doClick();

  int getSystemType();

  void setSystemType(int systemType);

  boolean isProcessButton();

  void setProcessButton(boolean on);

  Boolean getDefaultButton();

  void setDefaultButton(Boolean defaultButton);

  /**
   * Model Observer
   */
  IFastListenerList<ButtonListener> buttonListeners();

  default void addButtonListener(ButtonListener listener) {
    buttonListeners().add(listener);
  }

  default void removeButtonListener(ButtonListener listener) {
    buttonListeners().remove(listener);
  }

  String getIconId();

  void setIconId(String iconId);

  String getKeyStroke();

  void setKeyStroke(String keyStroke);

  /**
   * display style<br>
   * default, toggle, radio
   */
  int getDisplayStyle();

  /**
   * do not change this property when ui is attached<br>
   * major gui frameworks don't support runtime itemtype changes
   */
  void setDisplayStyleInternal(int i);

  /**
   * @return toggle and radio button state
   */
  boolean isSelected();

  /**
   * @param b
   *          toggle and radio button state
   */
  void setSelected(boolean b);

  boolean isPreventDoubleClick();

  void setPreventDoubleClick(boolean preventDoubleClick);

  boolean isStackable();

  /**
   * A stackable button will be stacked in a dropdown menu if there is not enough space in the menubar.
   */
  void setStackable(boolean stackable);

  boolean isShrinkable();

  /**
   * A shrinkable button will be displayed without label but only with its configured icon if there is not enough space
   * in the menubar.
   */
  void setShrinkable(boolean shrinkable);

  /**
   * request showing the (dropdown) menu popup
   */
  void requestPopup();

  IButtonUIFacade getUIFacade();

  void setView(boolean visible, boolean enabled);

  Object getKeyStrokeScope();

  /**
   * local images and local resources bound to the html text
   */
  Set<BinaryResource> getAttachments();

  BinaryResource getAttachment(String filename);

  void setAttachments(Collection<? extends BinaryResource> attachments);

}
