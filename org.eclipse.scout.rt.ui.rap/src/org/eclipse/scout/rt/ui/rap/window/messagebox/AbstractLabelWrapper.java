/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.messagebox;

import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @since 4.2
 */
public abstract class AbstractLabelWrapper implements ILabelWrapper {

  private final boolean m_htmlEnabled;
  private final StyledText m_textLabel;
  private final Label m_htmlLabel;

  public AbstractLabelWrapper(boolean htmlEnabled, Composite container) {
    m_htmlEnabled = htmlEnabled;
    m_textLabel = createTextLabel(htmlEnabled, container);
    m_htmlLabel = createHtmlLabel(htmlEnabled, container);
  }

  protected abstract Label createHtmlLabel(boolean htmlEnabled, Composite container);

  protected abstract StyledText createTextLabel(boolean htmlEnabled, Composite container);

  @Override
  public boolean isHtmlEnabled() {
    return m_htmlEnabled;
  }

  @Override
  public Control getLabel() {
    if (m_htmlEnabled) {
      return m_htmlLabel;
    }
    return m_textLabel;
  }

  @Override
  public String getLabelText() {
    if (m_htmlEnabled) {
      return m_htmlLabel.getText();
    }
    return m_textLabel.getText();
  }

  @Override
  public void setLabelText(String text) {
    if (m_htmlEnabled) {
      m_htmlLabel.setText(text);
    }
    else {
      m_textLabel.setText(text);
    }
  }
}
