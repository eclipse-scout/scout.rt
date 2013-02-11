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

import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface IUiDecoration {

  int getDialogMinWidth();

  int getDialogMinHeight();

  int getProcessButtonHeight();

  int getProcessButtonMinWidth();

  int getProcessButtonMaxWidth();

  int getFormFieldActivationButtonHeight();//XXX not used

  int getFormFieldActivationButtonWidth();

  int getFormFieldActivationButtonWithMenuWidth();

  String getMandatoryFieldBackgroundColor();

  int getMandatoryStarMarkerPosition();

  String getMandatoryLabelTextColor();

  FontSpec getMandatoryLabelFont();

  int getFormFieldLabelWidth();

  int getLogicalGridLayoutDefaultColumnWidth();

  int getLogicalGridLayoutHorizontalGap();

  int getLogicalGridLayoutVerticalGap();

  int getLogicalGridLayoutRowHeight();

  String getColorForegroundDisabled();

  int getFormFieldLabelAlignment();

  int getMessageBoxMinHeight();

  int getMessageBoxMinWidth();

  boolean isTableMouseMoveSelectionSupportEnabled();

  boolean isTableMultilineTooltipSupportEnabled();
}
