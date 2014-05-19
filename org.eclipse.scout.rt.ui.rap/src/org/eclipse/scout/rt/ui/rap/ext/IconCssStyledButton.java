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
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class IconCssStyledButton extends Button {
  private static final long serialVersionUID = 1L;
  private String m_baseVariant;

  public IconCssStyledButton(Composite parent, int style) {
    super(parent, style);
  }

  @Override
  public void setData(String key, Object value) {
    super.setData(key, value);
  }

  @Override
  public Object getData(String key) {
    return super.getData(key);
  }

  /**
   * since tab list on parent does not work
   */
  @Override
  public boolean forceFocus() {
    if ((getStyle() & SWT.NO_FOCUS) != 0) {
      return false;
    }
    else {
      return super.forceFocus();
    }
  }

  public void setBaseVariant(String baseVariant) {
    m_baseVariant = baseVariant;
    updateCustomVariant();
  }

  public String getBaseVariant() {
    return m_baseVariant;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    updateCustomVariant();
  }

  /**
   *
   */
  protected void updateCustomVariant() {
    if (StringUtility.hasText(m_baseVariant)) {
      String customVariant = m_baseVariant;
      if (isEnabled()) {
        customVariant += "_disabled";
      }
      setData(RWT.CUSTOM_VARIANT, customVariant);
    }
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

}
