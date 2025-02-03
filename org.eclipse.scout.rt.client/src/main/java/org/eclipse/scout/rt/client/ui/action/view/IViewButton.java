/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.view;

import org.eclipse.scout.rt.client.ui.action.IAction;

/**
 * Interface for buttons that represent a view or an outline, normally displayed as menus.
 */
public interface IViewButton extends IAction {

  String PROP_DISPLAY_STYLE = "displayStyle";

  enum DisplayStyle {
    /**
     * Outline view button is rendered as menu (default).
     */
    MENU,
    /**
     * Outline view button is rendered as tab.
     */
    TAB
  }

  DisplayStyle getDisplayStyle();

  void setDisplayStyle(DisplayStyle displayStyle);
}
