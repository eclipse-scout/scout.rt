/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

}
