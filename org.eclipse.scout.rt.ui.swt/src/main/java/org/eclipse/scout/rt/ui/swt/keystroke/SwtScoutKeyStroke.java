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

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.widgets.Event;

/**
 * <h3>SwtScoutKeyStroke</h3> ...
 * 
 * @since 1.0.0 07.05.2008
 */
public class SwtScoutKeyStroke extends SwtKeyStroke {

  private final ISwtEnvironment m_environment;
  private final IKeyStroke m_scoutKeyStroke;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  public SwtScoutKeyStroke(IKeyStroke scoutKeyStroke, int keyCode, int stateMask, ISwtEnvironment environment) {
    super(keyCode, stateMask);
    m_scoutKeyStroke = scoutKeyStroke;
    m_environment = environment;
  }

  @Override
  public void handleSwtAction(Event e) {
    if (getScoutKeyStroke().isEnabled() && getScoutKeyStroke().isVisible()) {
      SwtUtility.runSwtInputVerifier();
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
        getEnvironment().invokeScoutLater(job, 0);
      }
      e.doit = false;
    }
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public IKeyStroke getScoutKeyStroke() {
    return m_scoutKeyStroke;
  }

}
