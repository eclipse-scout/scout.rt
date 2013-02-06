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

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.widgets.Event;

/**
 * <h3>RwtScoutKeyStroke</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutKeyStroke extends RwtKeyStroke {

  private final IKeyStroke m_scoutKeyStroke;
  private final IRwtEnvironment m_uiEnvironment;

  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  public RwtScoutKeyStroke(IKeyStroke scoutKeyStroke, IRwtEnvironment uiEnvironment, int keyCode, int stateMask) {
    super(keyCode, stateMask);
    m_scoutKeyStroke = scoutKeyStroke;
    m_uiEnvironment = uiEnvironment;
  }

  @Override
  public void handleUiAction(Event e) {
    if (getScoutKeyStroke().isEnabled() && getScoutKeyStroke().isVisible()) {
      if (!m_handleActionPending) {
        m_handleActionPending = true;
        Runnable job = new Runnable() {
          @Override
          public void run() {
            try {
              getScoutKeyStroke().getUIFacade().fireActionFromUI();
            }
            finally {
              m_handleActionPending = false;
            }
          }
        };
        getUiEnvironment().invokeScoutLater(job, 0);
      }
      e.doit = false;
    }
  }

  public IKeyStroke getScoutKeyStroke() {
    return m_scoutKeyStroke;
  }

  private IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

}
