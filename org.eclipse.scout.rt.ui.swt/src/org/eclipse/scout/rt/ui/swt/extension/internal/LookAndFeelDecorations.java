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

import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.extension.ILookAndFeelDecorations;

public class LookAndFeelDecorations implements ILookAndFeelDecorations {
  private int m_scope;
  private int m_starMarkerPosition = STAR_MARKER_NONE;
  private FontSpec m_mandatoryLabelFont;
  private String m_mandatoryFieldBackgroundColor;
  private String m_mandatoryLabelTextColor;
  private boolean m_enabledAsReadOnly;

  public int getScope() {
    return m_scope;
  }

  public void setScope(int scope) {
    m_scope = scope;
  }

  public int getStarMarkerPosition() {
    return m_starMarkerPosition;
  }

  public void setStarMarkerPosition(int starMarkerPosition) {
    m_starMarkerPosition = starMarkerPosition;
  }

  public FontSpec getMandatoryLabelFont() {
    return m_mandatoryLabelFont;
  }

  public void setMandatoryLabelFont(FontSpec mandatoryLabelFont) {
    m_mandatoryLabelFont = mandatoryLabelFont;
  }

  public String getMandatoryFieldBackgroundColor() {
    return m_mandatoryFieldBackgroundColor;
  }

  public void setMandatoryFieldBackgroundColor(String mandatoryFieldBackgroundColor) {
    m_mandatoryFieldBackgroundColor = mandatoryFieldBackgroundColor;
  }

  public String getMandatoryLabelTextColor() {
    return m_mandatoryLabelTextColor;
  }

  public void setMandatoryLabelTextColor(String mandatoryLabelTextColor) {
    m_mandatoryLabelTextColor = mandatoryLabelTextColor;
  }

  public void setEnableAsReadOnly(boolean equals) {
    m_enabledAsReadOnly = equals;
  }

  public boolean isEnableAsReadOnly() {
    return m_enabledAsReadOnly;
  }

}
