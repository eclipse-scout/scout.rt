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
package org.eclipse.scout.rt.ui.swt.form.fields.colorpickerfield;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.ColorUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class ColorCanvas extends Canvas {

  private Color m_color;
  private ISwtEnvironment m_environment;

  public ColorCanvas(Composite parent, ISwtEnvironment environment) {
    super(parent, SWT.BORDER);
    m_environment = environment;
    addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        disposeResources();
      }
    });
  }

  protected void disposeResources() {
    if (m_color != null && !m_color.isDisposed()) {
      m_color.dispose();
    }
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public void setColor(String hexColor) {
    if (m_color != null) {
      m_color.dispose();
      m_color = null;
    }
    m_color = ColorUtility.createColor(getDisplay(), hexColor);
    setBackground(m_color);
  }

  public Color getColor() {
    return m_color;
  }

}
