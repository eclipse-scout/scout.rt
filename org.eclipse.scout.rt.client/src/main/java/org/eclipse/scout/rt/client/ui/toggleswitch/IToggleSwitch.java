/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.toggleswitch;

import org.eclipse.scout.rt.client.ui.IWidget;

public interface IToggleSwitch extends IWidget {

  String PROP_ACTIVATED = "activated";
  String PROP_LABEL = "label";
  String PROP_LABEL_HTML_ENABLED = "labelHtmlEnabled";
  String PROP_LABEL_VISIBLE = "labelVisible";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_ICON_VISIBLE = "iconVisible";
  String PROP_DISPLAY_STYLE = "displayStyle";
  String PROP_TABBABLE = "tabbable";

  String DISPLAY_STYLE_DEFAULT = "default";
  String DISPLAY_STYLE_SLIDER = "slider";

  IToggleSwitchUIFacade getUIFacade();

  boolean isActivated();

  void setActivated(boolean activated);

  String getLabel();

  void setLabel(String label);

  boolean isLabelHtmlEnabled();

  void setLabelHtmlEnabled(boolean labelHtmlEnabled);

  /**
   * If this is {@code null}, the label is automatically visible when {@link #getLabel()} contains text.
   */
  Boolean getLabelVisible();

  void setLabelVisible(Boolean labelVisible);

  String getTooltipText();

  void setTooltipText(String tooltipText);

  boolean isIconVisible();

  void setIconVisible(boolean iconVisible);

  String getDisplayStyle();

  void setDisplayStyle(String displayStyle);

  boolean isTabbable();

  void setTabbable(boolean tabbable);
}
