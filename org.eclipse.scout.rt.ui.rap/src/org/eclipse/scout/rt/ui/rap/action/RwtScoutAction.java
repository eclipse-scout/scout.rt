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
package org.eclipse.scout.rt.ui.rap.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;

/**
 * <h3>RwtScoutAction</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutAction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutAction.class);

  private static final Map<String, String> KEY_STROKE_MAP;

  private final IAction m_scoutAction;
  private final Action m_uiAction;
  private final IRwtEnvironment m_uiEnvironment;

  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  static {
    KEY_STROKE_MAP = new HashMap<String, String>(12);
    //build keyStroke map
    for (int i = 1; i <= 12; i++) {
      KEY_STROKE_MAP.put("SHIFT-F" + i, "Shift-F" + i);
    }
  }

  public RwtScoutAction(IAction scoutAction, IRwtEnvironment uiEnvironment) {
    this(scoutAction, uiEnvironment, SWT.PUSH);
  }

  public RwtScoutAction(IAction scoutAction, IRwtEnvironment uiEnvironment, int style) {
    m_scoutAction = scoutAction;
    m_uiEnvironment = uiEnvironment;
    m_uiAction = new P_RwtAction(style);
    // init
    String keyStroke = m_scoutAction.getKeyStroke();
    if (StringUtility.hasText(keyStroke)) {
      // '@' sign is used as delimiter for shortcut text
      m_uiAction.setText(m_scoutAction.getText() + "@" + lookupKeyStrokeText(keyStroke));
    }
    else {
      m_uiAction.setText(m_scoutAction.getText());
    }
    m_uiAction.setToolTipText(m_scoutAction.getTooltipText());
    m_uiAction.setImageDescriptor(getUiEnvironment().getImageDescriptor(m_scoutAction.getIconId()));

    setKeyStrokeFromScout(keyStroke);
  }

  protected void handleUiAction() {
    //XXX
    /*if (getScoutAction().getClass().getSimpleName().equals("ImoMenu")) {
      ImoDevDialog d = new ImoDevDialog();
      d.open();
      return;
    }*/

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
      getUiEnvironment().invokeScoutLater(job, 0);
    }
  }

  public IAction getScoutAction() {
    return m_scoutAction;
  }

  public Action getUiAction() {
    return m_uiAction;
  }

  protected IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  private class P_RwtAction extends Action {
    private static final long serialVersionUID = 1L;

    P_RwtAction(int style) {
      super("", style);
    }

    @Override
    public void run() {
      handleUiAction();
    }
  } // end P_RwtAction

  private void setKeyStrokeFromScout(String keyStroke) {
    if (keyStroke != null) {
      int keyCode = RwtUtility.getRwtKeyCode(new KeyStroke(keyStroke));
      int stateMask = RwtUtility.getRwtStateMask(new KeyStroke(keyStroke));
      getUiAction().setAccelerator(stateMask | keyCode);
    }
    else {
      getUiAction().setAccelerator(SWT.NONE);
    }
  }

  private String lookupKeyStrokeText(String keyStroke) {
    return KEY_STROKE_MAP.get(keyStroke.toUpperCase());
  }
}
