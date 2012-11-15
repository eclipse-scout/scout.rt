/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.extension.internal;

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.rap.extension.ILookAndFeelDecorations;

public class LookAndFeelDecorations implements ILookAndFeelDecorations {
  private int m_scope;
  private int m_starMarkerPosition = STAR_MARKER_NONE;
  private FontSpec m_mandatoryLabelFont;
  private String m_mandatoryFieldBackgroundColor;
  private String m_mandatoryLabelTextColor;
  private String m_deviceTypeIdentifier;

  @Override
  public int getScope() {
    return m_scope;
  }

  public void setScope(int scope) {
    m_scope = scope;
  }

  @Override
  public String getDeviceTypeIdentifier() {
    return m_deviceTypeIdentifier;
  }

  public void setDeviceTypeIdentifier(String deviceTypeIdentifier) {
    m_deviceTypeIdentifier = deviceTypeIdentifier;
  }

  @Override
  public int getStarMarkerPosition() {
    return m_starMarkerPosition;
  }

  public void setStarMarkerPosition(int starMarkerPosition) {
    m_starMarkerPosition = starMarkerPosition;
  }

  @Override
  public FontSpec getMandatoryLabelFont() {
    return m_mandatoryLabelFont;
  }

  public void setMandatoryLabelFont(FontSpec mandatoryLabelFont) {
    m_mandatoryLabelFont = mandatoryLabelFont;
  }

  @Override
  public String getMandatoryFieldBackgroundColor() {
    return m_mandatoryFieldBackgroundColor;
  }

  public void setMandatoryFieldBackgroundColor(String mandatoryFieldBackgroundColor) {
    m_mandatoryFieldBackgroundColor = mandatoryFieldBackgroundColor;
  }

  @Override
  public String getMandatoryLabelTextColor() {
    return m_mandatoryLabelTextColor;
  }

  public void setMandatoryLabelTextColor(String mandatoryLabelTextColor) {
    m_mandatoryLabelTextColor = mandatoryLabelTextColor;
  }
}
