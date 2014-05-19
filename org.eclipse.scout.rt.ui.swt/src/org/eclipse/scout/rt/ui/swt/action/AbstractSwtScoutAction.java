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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;

/**
 *
 */
public abstract class AbstractSwtScoutAction extends Action {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutToolbarAction.class);

  private IAction m_scoutAction;
  private ISwtEnvironment m_swtEnvironment;
  private final OptimisticLock m_updateSwtFromScoutLock;
  private boolean m_updateUi = true;
  private boolean m_handleSelectionPending;

  private P_ScoutPropertyChangeListener m_scoutPropertyListener;

  public AbstractSwtScoutAction(IAction scoutAction, ISwtEnvironment environment) {
    this(scoutAction, environment, true);
  }

  public AbstractSwtScoutAction(IAction scoutAction, ISwtEnvironment environment, boolean attachScout) {
    super((scoutAction.getText() == null) ? (" ") : scoutAction.getText(), transformScoutStyle(scoutAction));
    m_swtEnvironment = environment;
    m_updateSwtFromScoutLock = new OptimisticLock();
    m_scoutAction = scoutAction;
    setId(getScoutObject().getActionId());
    if (attachScout) {
      attachScout();
    }
  }

  private static int transformScoutStyle(IAction scoutAction) {
    if (scoutAction.isToggleAction()) {
      return SWT.TOGGLE;
    }
    return AS_PUSH_BUTTON;
  }

  protected void attachScout() {
    try {
      setUpdateUi(false);
      updateEnabledFromScout();
      updateIconFromScout();
      updateKeystrokeFromScout();
      updateSelectedFromScout();
      updateTextFromScout();
      updateTooltipTextFromScout();
      m_scoutAction.addPropertyChangeListener(new P_ScoutPropertyChangeListener());
    }
    finally {
      setUpdateUi(true);
    }
  }

  /**
   * @return the swtEnvironment
   */
  public ISwtEnvironment getEnvironment() {
    return m_swtEnvironment;
  }

  protected IAction getScoutObject() {
    return m_scoutAction;
  }

  /**
   * @param updateUi
   *          the updateUi to set
   */
  protected void setUpdateUi(boolean updateUi) {
    if (updateUi != m_updateUi) {
      m_updateUi = updateUi;
      if (updateUi) {
        updateUi();

      }
    }
  }

  protected void updateUiIfNeeded() {
    if (isUpdateUi()) {
      updateUi();
    }
  }

  protected abstract void updateUi();

  /**
   * @return the updateUi
   */
  public boolean isUpdateUi() {
    return m_updateUi;
  }

  protected void updateEnabledFromScout() {
    setEnabled(getScoutObject().isEnabled());
    updateUiIfNeeded();
  }

  protected void updateIconFromScout() {
    setImageDescriptor(getEnvironment().getImageDescriptor(getScoutObject().getIconId()));
    updateUiIfNeeded();
  }

  protected void updateKeystrokeFromScout() {
    String keyStroke = getScoutObject().getKeyStroke();
    if (keyStroke != null) {
      int keyCode = SwtUtility.getSwtKeyCode(new KeyStroke(keyStroke));
      int stateMask = SwtUtility.getSwtStateMask(new KeyStroke(keyStroke));
      setAccelerator(stateMask | keyCode);
    }
    else {
      setAccelerator(SWT.NONE);
    }
    updateUiIfNeeded();
  }

  protected void updateTextFromScout() {
    setText(getScoutObject().getText());
    updateUiIfNeeded();
  }

  protected void updateTooltipTextFromScout() {
    setToolTipText(getScoutObject().getTooltipText());
    updateUiIfNeeded();
  }

  protected void updateSelectedFromScout() {
    setChecked(getScoutObject().isSelected());
    updateUiIfNeeded();
  }

  protected void updateVisibleFromScout() {
    LOG.warn("set visible on SWT action is not supported");
  }

  @Override
  public void run() {
    handleSwtAction();
  }

  protected void handleSwtAction() {
    try {
      if (getUpdateSwtFromScoutLock().acquire()) {
        if (getScoutObject().isToggleAction() && getScoutObject() instanceof IViewButton && getScoutObject().isSelected()) {
          // reset UI selection
          updateSelectedFromScout();
        }
        else {
          //run inputVerifier since there might not be a focus lost event
          if (SwtUtility.runSwtInputVerifier()) {
            if (!m_handleSelectionPending) {
              m_handleSelectionPending = true;
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  try {
                    getScoutObject().getUIFacade().fireActionFromUI();
                  }
                  finally {
                    m_handleSelectionPending = false;
                  }
                }
              };
              getEnvironment().invokeScoutLater(t, 0);
            }
          }
        }
      }
    }
    finally {
      getUpdateSwtFromScoutLock().release();
    }
  }

  /**
   * @return the lock used in the Swt thread when applying scout changes
   */
  public OptimisticLock getUpdateSwtFromScoutLock() {
    return m_updateSwtFromScoutLock;
  }

  protected void handleScoutPropertyChange(String propertyName, Object newValue) {
    if (IAction.PROP_ENABLED.equals(propertyName)) {
      updateEnabledFromScout();
    }
    else if (IAction.PROP_ICON_ID.equals(propertyName)) {
      updateIconFromScout();
    }
    else if (IAction.PROP_KEYSTROKE.equals(propertyName)) {
      updateKeystrokeFromScout();
    }
    else if (IAction.PROP_SELECTED.equals(propertyName)) {
      updateSelectedFromScout();
    }
    else if (IAction.PROP_TEXT.equals(propertyName)) {
      updateTextFromScout();
    }
    else if (IAction.PROP_TOOLTIP_TEXT.equals(propertyName)) {
      updateTooltipTextFromScout();
    }
    else if (IAction.PROP_VISIBLE.equals(propertyName)) {
      updateVisibleFromScout();
    }

  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            getUpdateSwtFromScoutLock().acquire();
            //
            handleScoutPropertyChange(evt.getPropertyName(), evt.getNewValue());
          }
          finally {
            getUpdateSwtFromScoutLock().release();
          }
        }

      };
      getEnvironment().invokeSwtLater(t);

    }
  }

}
