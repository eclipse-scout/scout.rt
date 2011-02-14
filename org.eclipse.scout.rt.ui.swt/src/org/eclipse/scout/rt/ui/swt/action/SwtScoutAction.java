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
package org.eclipse.scout.rt.ui.swt.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;

/**
 * <h3>SwtScoutAction</h3> ...
 * 
 * @since 1.0.0 28.03.2008
 */
public class SwtScoutAction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutAction.class);

  private static final Map<String, String> KEY_STROKE_MAP;

  private final IAction m_scoutAction;
  private final Action m_swtAction;
  private final ISwtEnvironment m_environment;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  static {
    KEY_STROKE_MAP = new HashMap<String, String>(12);
    //build keyStroke map
    for (int i = 1; i <= 12; i++) {
      KEY_STROKE_MAP.put("SHIFT-F" + i, "Shift-F" + i);
    }
  }

  public SwtScoutAction(IAction scoutAction, ISwtEnvironment environment) {
    this(scoutAction, environment, SWT.PUSH);
  }

  public SwtScoutAction(IAction scoutAction, ISwtEnvironment environment, int style) {
    m_scoutAction = scoutAction;
    m_environment = environment;
    m_swtAction = new P_SwtAction(style);
    // init
    String keyStroke = m_scoutAction.getKeyStroke();
    if (StringUtility.hasText(keyStroke)) {
      // '@' sign is used as delimiter for shortcut text
      m_swtAction.setText(m_scoutAction.getText() + "@" + lookupKeyStrokeText(keyStroke));
    }
    else {
      m_swtAction.setText(m_scoutAction.getText());
    }
    m_swtAction.setToolTipText(m_scoutAction.getTooltipText());
    m_swtAction.setImageDescriptor(m_environment.getImageDescriptor(m_scoutAction.getIconId()));

    setKeyStrokeFromScout(keyStroke);
  }

  protected void handleSwtAction() {
    if (!m_handleActionPending) {
      m_handleActionPending = true;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          try {
            getScoutAction().getUIFacade().fireActionFromUI();
          }
          finally {
            m_handleActionPending = false;
          }
        }
      };
      getEnvironment().invokeScoutLater(job, 0);
    }
  }

  public IAction getScoutAction() {
    return m_scoutAction;
  }

  public Action getSwtAction() {
    return m_swtAction;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  private class P_SwtAction extends Action {
    P_SwtAction(int style) {
      super("", style);
    }

    @Override
    public void run() {
      handleSwtAction();
    }
  } // end P_SwtAction

  private void setKeyStrokeFromScout(String keyStroke) {
    if (keyStroke != null) {
      int keyCode = SwtUtility.getSwtKeyCode(new KeyStroke(keyStroke));
      int stateMask = SwtUtility.getSwtStateMask(new KeyStroke(keyStroke));
      getSwtAction().setAccelerator(stateMask | keyCode);
    }
    else {
      getSwtAction().setAccelerator(SWT.NONE);
    }
  }

  private String lookupKeyStrokeText(String keyStroke) {
    return KEY_STROKE_MAP.get(keyStroke.toUpperCase());
  }
}
