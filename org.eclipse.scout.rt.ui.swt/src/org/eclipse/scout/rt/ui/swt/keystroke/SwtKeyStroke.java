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
package org.eclipse.scout.rt.ui.swt.keystroke;

import org.eclipse.swt.SWT;

public abstract class SwtKeyStroke implements ISwtKeyStroke {

  private final int m_stateMask;
  private final int m_keyCode;

  public SwtKeyStroke(int keyCode) {
    this(keyCode, SWT.NONE);
  }

  public SwtKeyStroke(int keyCode, int stateMask) {
    m_keyCode = keyCode;
    m_stateMask = stateMask;

  }

  public int getStateMask() {
    return m_stateMask;
  }

  public int getKeyCode() {
    return m_keyCode;
  }

}
