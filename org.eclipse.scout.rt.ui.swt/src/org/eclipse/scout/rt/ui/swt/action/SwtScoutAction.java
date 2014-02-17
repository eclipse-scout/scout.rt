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

import org.eclipse.jface.action.Action;
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
public class SwtScoutAction extends AbstractSwtScoutAction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutAction.class);

  private boolean m_initialized;
  private final Action m_swtAction;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  public SwtScoutAction(IAction scoutAction, ISwtEnvironment environment) {
    this(scoutAction, environment, Action.AS_PUSH_BUTTON);
  }

  public SwtScoutAction(IAction scoutAction, ISwtEnvironment environment, int style) {
    super(scoutAction, environment);
    m_swtAction = new P_SwtAction(style);
    callInitializers(m_swtAction);
  }

  /**
   * @param swtAction
   */
  private void callInitializers(Action swtAction) {
    if (m_initialized) {
      return;
    }
    else {
      m_initialized = true;
      //
      initializeSwt(swtAction);
      connectToScout();
    }
  }

  /**
   * @param swtAction
   */
  protected void initializeSwt(Action swtAction) {
  }

  protected void handleSwtAction() {
    if (SwtUtility.runSwtInputVerifier()) {
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
  }

  public Action getSwtAction() {
    return m_swtAction;
  }

  private class P_SwtAction extends Action {
    P_SwtAction(int style) {
      super("", style);
    }

    @Override
    public void run() {
      handleSwtAction();
    }

    @Override
    public boolean isChecked() {
      return super.isChecked();
    }
  } // end P_SwtAction

  @Override
  protected void setEnabledFromScout(boolean enabled) {
    m_swtAction.setEnabled(enabled);
  }

  @Override
  protected void setTextWithMnemonicFromScout(String textWithMnemonic) {
    m_swtAction.setText(textWithMnemonic);
  }

  @Override
  protected void setTooltipTextFromScout(String tooltipText) {
    m_swtAction.setToolTipText(tooltipText);
  }

  @Override
  protected void setIconFromScout(String iconId) {
    m_swtAction.setImageDescriptor(getEnvironment().getImageDescriptor(iconId));
  }

  @Override
  protected void setKeyStrokeFromScout(String keyStroke) {
    if (keyStroke != null) {
      int keyCode = SwtUtility.getSwtKeyCode(new KeyStroke(keyStroke));
      int stateMask = SwtUtility.getSwtStateMask(new KeyStroke(keyStroke));
      getSwtAction().setAccelerator(stateMask | keyCode);
    }
    else {
      getSwtAction().setAccelerator(SWT.NONE);
    }
  }
}
