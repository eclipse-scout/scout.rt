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
package org.eclipse.scout.rt.ui.rap.util;

import org.eclipse.swt.widgets.Control;

public class UiRedrawHandler {
  private final Control m_control;
  private int m_counter = 0;

  public UiRedrawHandler(Control control) {
    m_control = control;
  }

  public void pushControlChanging() {
    if (m_counter == 0) {
      m_control.setRedraw(false);
    }
    m_counter++;
  }

  public void popControlChanging() {
    m_counter--;
    if (m_counter == 0) {
      m_control.setRedraw(true);
    }
  }
}
