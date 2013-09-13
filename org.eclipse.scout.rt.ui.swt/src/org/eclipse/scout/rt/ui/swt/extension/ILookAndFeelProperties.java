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
package org.eclipse.scout.rt.ui.swt.extension;

public interface ILookAndFeelProperties {

  String PROP_FORM_FIELD_LABEL_WIDTH = "formFieldLabel.Width";
  String PROP_FORM_FIELD_LABEL_ALIGNMENT = "formFieldLabel.alignment";
  String PROP_LOGICAL_GRID_LAYOUT_DEFAULT_COLUMN_WIDTH = "logicalGridLayout.defaultColumnWidth";

  String PROP_LOGICAL_GRID_LAYOUT_ROW_HEIGHT = "logicalGridLayout.rowHeight";
  String PROP_LOGICAL_GRID_LAYOUT_HORIZONTAL_GAP = "logicalGridLayout.horizontalGap";
  String PROP_LOGICAL_GRID_LAYOUT_VERTICAL_GAP = "logicalGridLayout.verticalGap";
  String PROP_FORM_FIELD_ACTIVATION_BUTTON_WIDTH = "formfield.activationButton.width";
  String PROP_FORM_FIELD_ACTIVATION_BUTTON_HEIGHT = "formfield.activationButton.height";
  String PROP_FORM_FIELD_ACTIVATION_BUTTON_WITH_MENU_WIDTH = "formfield.activationButtonWithMenu.width";
  String PROP_PROCESS_BUTTON_HEIGHT = "processButton.height";
  String PROP_PROCESS_BUTTON_MIN_WIDTH = "processButton.minWidth";
  String PROP_PROCESS_BUTTON_MAX_WIDTH = "processButton.maxWidth";
  String PROP_DIALOG_MIN_WIDTH = "dialog.minWidth";
  String PROP_DIALOG_MIN_HEIGHT = "dialog.minHeight";
  String PROP_COLOR_FOREGROUND_DISABLED = "color.forground.disabled";
  String PROP_COLOR_BACKGROUND_DISABLED = "color.background.disabled";
  String PROP_MESSAGE_BOX_MIN_WIDTH = "messageBox.minWidth";
  String PROP_MESSAGE_BOX_MIN_HEIGHT = "messageBox.minHeight";
  String PROP_TABLE_MOUSE_MOVE_SELECTION_SUPPORT_ENABLED = "table.mouseMoveSelectionSupport.enabled";
  String PROP_TABLE_MULTILINE_TOOLTIP_SUPPORT_ENABLED = "table.multilineTooltipSupport.enabled";

  int getScope();

  int getPropertyInt(String name);

  String getPropertyString(String name);

  boolean getPropertyBool(String name);
}
