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
package org.eclipse.scout.rt.ui.rap.keystroke;

import org.eclipse.rwt.lifecycle.PhaseListener;
import org.eclipse.swt.SWT;

public abstract class RwtKeyStroke implements IRwtKeyStroke {

  private final int m_stateMask;
  private final int m_keyCode;
  private PhaseListener m_phaseListener;

  public RwtKeyStroke(int keyCode) {
    this(keyCode, SWT.NONE);
  }

  public RwtKeyStroke(int keyCode, int stateMask) {
    m_keyCode = keyCode;
    m_stateMask = stateMask;

  }

  @Override
  public int getStateMask() {
    return m_stateMask;
  }

  @Override
  public int getKeyCode() {
    return m_keyCode;
  }

  /**
   * @return the phaseListener
   */
  @Override
  public PhaseListener getPhaseListener() {
    return m_phaseListener;
  }

  /**
   * @param phaseListener
   *          the phaseListener to set
   */
  @Override
  public void setPhaseListener(PhaseListener phaseListener) {
    m_phaseListener = phaseListener;
  }

}
