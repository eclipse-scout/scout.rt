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
package org.eclipse.scout.rt.ui.swt.extension.internal;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.extension.IUiDecoration;
import org.eclipse.swt.SWT;

public class UiDecoration implements IUiDecoration {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UiDecoration.class);

  private String m_mandatoryFieldBackgroundColor;
  private int m_mandatoryStarMarkerPosition;
  private String m_mandatoryLabelTextColor;
  private FontSpec m_mandatoryLabelFont;
  private int m_dialogMinWidth;
  private int m_dialogMinHeight;
  private int m_formFieldActivationButtonHeight;
  private int m_formFieldActivationButtonWidth;
  private int m_formFieldActivationButtonWithMenuWidth;
  private int m_formFieldLabelWidth;
  private int m_logicalGridLayoutDefaultColumnWidth;
  private int m_logicalGridLayoutHorizontalGap;
  private int m_logicalGridLayoutVerticalGap;
  private int m_logicalGridLayoutRowHeight;
  private int m_processButtonHeight;
  private int m_processButtonMinWidth;
  private int m_processButtonMaxWidth;
  private String m_colorForegroundDisabled;
  private String m_colorBackgroundDisabled;
  private int m_messageBoxMinWidth = 400;
  private int m_messageBoxMinHeight = 100;
  private boolean m_tableMouseMoveSelectionSupportEnabled;
  private boolean m_tableMultilineTooltipSupportEnabled;

  /**
   * one of SWT.RIGHT SWT.LEFT SWT.CENTER
   */
  private int m_formFieldLabelAlignment = SWT.RIGHT;

  @Override
  public int getDialogMinWidth() {
    return m_dialogMinWidth;
  }

  public void setDialogMinWidth(int dialogMinWidth) {
    m_dialogMinWidth = dialogMinWidth;
  }

  @Override
  public int getDialogMinHeight() {
    return m_dialogMinHeight;
  }

  public void setDialogMinHeight(int dialogMinHeight) {
    m_dialogMinHeight = dialogMinHeight;
  }

  @Override
  public int getProcessButtonHeight() {
    return m_processButtonHeight;
  }

  public void setProcessButtonHeight(int processButtonHeight) {
    m_processButtonHeight = processButtonHeight;
  }

  @Override
  public int getProcessButtonMinWidth() {
    return m_processButtonMinWidth;
  }

  public void setProcessButtonMinWidth(int processButtonMinWidth) {
    m_processButtonMinWidth = processButtonMinWidth;
  }

  @Override
  public int getProcessButtonMaxWidth() {
    return m_processButtonMaxWidth;
  }

  public void setProcessButtonMaxWidth(int processButtonMaxWidth) {
    m_processButtonMaxWidth = processButtonMaxWidth;
  }

  @Override
  public int getFormFieldActivationButtonHeight() {
    return m_formFieldActivationButtonHeight;
  }

  public void setFormFieldActivationButtonHeight(int formFieldActivationButtonHeight) {
    m_formFieldActivationButtonHeight = formFieldActivationButtonHeight;
  }

  @Override
  public int getFormFieldActivationButtonWidth() {
    return m_formFieldActivationButtonWidth;
  }

  public void setFormFieldActivationButtonWidth(int formFieldActivationButtonWidth) {
    m_formFieldActivationButtonWidth = formFieldActivationButtonWidth;
  }

  @Override
  public int getFormFieldActivationButtonWithMenuWidth() {
    return m_formFieldActivationButtonWithMenuWidth;
  }

  public void setFormFieldActivationButtonWithMenuWidth(int formFieldActivationButtonWithMenuWidth) {
    m_formFieldActivationButtonWithMenuWidth = formFieldActivationButtonWithMenuWidth;
  }

  @Override
  public String getMandatoryFieldBackgroundColor() {
    return m_mandatoryFieldBackgroundColor;
  }

  public void setMandatoryFieldBackgroundColor(String mandatoryFieldBackgroundColor) {
    m_mandatoryFieldBackgroundColor = mandatoryFieldBackgroundColor;
  }

  @Override
  public int getMandatoryStarMarkerPosition() {
    return m_mandatoryStarMarkerPosition;
  }

  public void setMandatoryStarMarkerPosition(int mandatoryStarMarkerPosition) {
    m_mandatoryStarMarkerPosition = mandatoryStarMarkerPosition;
  }

  @Override
  public String getMandatoryLabelTextColor() {
    return m_mandatoryLabelTextColor;
  }

  public void setMandatoryLabelTextColor(String mandatoryLabelTextColor) {
    m_mandatoryLabelTextColor = mandatoryLabelTextColor;
  }

  @Override
  public FontSpec getMandatoryLabelFont() {
    return m_mandatoryLabelFont;
  }

  public void setMandatoryLabelFont(FontSpec mandatoryLabelFont) {
    m_mandatoryLabelFont = mandatoryLabelFont;
  }

  @Override
  public int getFormFieldLabelWidth() {
    return m_formFieldLabelWidth;
  }

  public void setFormFieldLabelWidth(int formFieldLabelWidth) {
    m_formFieldLabelWidth = formFieldLabelWidth;
  }

  @Override
  public int getLogicalGridLayoutDefaultColumnWidth() {
    return m_logicalGridLayoutDefaultColumnWidth;
  }

  public void setLogicalGridLayoutDefaultColumnWidth(int logicalGridLayoutDefaultColumnWidth) {
    m_logicalGridLayoutDefaultColumnWidth = logicalGridLayoutDefaultColumnWidth;
  }

  @Override
  public int getLogicalGridLayoutHorizontalGap() {
    return m_logicalGridLayoutHorizontalGap;
  }

  public void setLogicalGridLayoutHorizontalGap(int logicalGridLayoutHorizontalGap) {
    m_logicalGridLayoutHorizontalGap = logicalGridLayoutHorizontalGap;
  }

  @Override
  public int getLogicalGridLayoutVerticalGap() {
    return m_logicalGridLayoutVerticalGap;
  }

  public void setLogicalGridLayoutVerticalGap(int logicalGridLayoutVerticalGap) {
    m_logicalGridLayoutVerticalGap = logicalGridLayoutVerticalGap;
  }

  @Override
  public int getLogicalGridLayoutRowHeight() {
    return m_logicalGridLayoutRowHeight;
  }

  public void setLogicalGridLayoutRowHeight(int logicalGridLayoutRowHeight) {
    m_logicalGridLayoutRowHeight = logicalGridLayoutRowHeight;
  }

  @Override
  public String getColorBackgroundDisabled() {
    return m_colorBackgroundDisabled;
  }

  public void setColorBackgroundDisabled(String colorBackgroundDisabled) {
    m_colorBackgroundDisabled = colorBackgroundDisabled;
  }

  @Override
  public String getColorForegroundDisabled() {
    return m_colorForegroundDisabled;
  }

  public void setColorForegroundDisabled(String colorForegroundDisabled) {
    m_colorForegroundDisabled = colorForegroundDisabled;
  }

  @Override
  public int getFormFieldLabelAlignment() {
    return m_formFieldLabelAlignment;
  }

  public void setFormFieldLabelAlignment(int propertyString) {
    m_formFieldLabelAlignment = propertyString;
  }

  @Override
  public int getMessageBoxMinWidth() {
    return m_messageBoxMinWidth;
  }

  public void setMessageBoxMinWidth(int messageBoxMinWidth) {
    m_messageBoxMinWidth = messageBoxMinWidth;
  }

  @Override
  public int getMessageBoxMinHeight() {
    return m_messageBoxMinHeight;
  }

  public void setMessageBoxMinHeight(int messageBoxMinHeight) {
    m_messageBoxMinHeight = messageBoxMinHeight;
  }

  public void setTableMouseMoveSelectionSupportEnabled(boolean tableMouseMoveSelectionSupportEnabled) {
    m_tableMouseMoveSelectionSupportEnabled = tableMouseMoveSelectionSupportEnabled;
  }

  @Override
  public boolean isTableMouseMoveSelectionSupportEnabled() {
    return m_tableMouseMoveSelectionSupportEnabled;
  }

  public void setTableMultilineTooltipSupportEnabled(boolean tableMultilineTooltipSupportEnabled) {
    m_tableMultilineTooltipSupportEnabled = tableMultilineTooltipSupportEnabled;
  }

  @Override
  public boolean isTableMultilineTooltipSupportEnabled() {
    return m_tableMultilineTooltipSupportEnabled;
  }
}
