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
package org.eclipse.scout.rt.ui.swing.action;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;

public class SwingScoutAction<T extends IAction> extends SwingScoutComposite<T> implements ISwingScoutAction<T> {
  private Action m_swingAction;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  public static void registerCompositeOnAction(Action a, ISwingScoutComposite ui) {
    if (a != null) {
      a.putValue(CLIENT_PROP_SWING_SCOUT_COMPOSITE, new WeakReference<ISwingScoutComposite>(ui));
    }
  }

  /**
   * @return the scout model used by this action or null if this {@link Action} has no client property with a scout
   *         model reference.
   */
  @SuppressWarnings("unchecked")
  public static ISwingScoutComposite getCompositeOnAction(Action a) {
    if (a != null) {
      WeakReference<ISwingScoutComposite> ref = (WeakReference<ISwingScoutComposite>) a.getValue(CLIENT_PROP_SWING_SCOUT_COMPOSITE);
      return ref != null ? ref.get() : null;
    }
    else {
      return null;
    }
  }

  public static IPropertyObserver getScoutModelOnAction(Action a) {
    ISwingScoutComposite ui = getCompositeOnAction(a);
    if (ui != null) {
      return ui.getScoutObject();
    }
    else {
      return null;
    }
  }

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    IAction scoutAction = getScoutObject();
    m_swingAction = new P_SwingAction(cleanupString(scoutAction.getText()));
  }

  public void reInitializeAction() {
    connectToScout();
  }

  public void disposeAction() {
    disconnectFromScout();
  }

  @Override
  public Action getSwingAction() {
    return m_swingAction;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IAction scoutAction = getScoutObject();
    setEnabledFromScout(scoutAction.isEnabled());
    setVisibleFromScout(scoutAction.isVisible());
    setTextFromScout(scoutAction.getText());
    setTooltipTextFromScout(scoutAction.getTooltipText());
    setMnemonicFromScout(scoutAction.getMnemonic());
    setKeyStrokeFromScout(scoutAction.getKeyStroke());
    setIconFromScout(scoutAction.getIconId());
  }

  private void setEnabledFromScout(boolean b) {
    getSwingAction().setEnabled(b);
  }

  private void setVisibleFromScout(boolean b) {
    getSwingAction().putValue("visible", b ? Boolean.TRUE : Boolean.FALSE);
  }

  private void setTextFromScout(String s) {
    getSwingAction().putValue(Action.NAME, cleanupString(s));
  }

  private void setKeyStrokeFromScout(String s) {
    if (s != null) {
      getSwingAction().putValue(Action.ACCELERATOR_KEY, SwingUtility.createKeystroke(new org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke(s)));
    }
    else {
      getSwingAction().putValue(Action.ACCELERATOR_KEY, null);
    }
  }

  private void setTooltipTextFromScout(String s) {
    getSwingAction().putValue(Action.SHORT_DESCRIPTION, s);
  }

  private void setMnemonicFromScout(char ch) {
    if (ch != 0x0) {
      ch = Character.toUpperCase(ch); //mnemonics in Swing must be upper case
      getSwingAction().putValue(Action.MNEMONIC_KEY, new Integer(ch));
    }
  }

  private void setIconFromScout(String id) {
    getSwingAction().putValue(Action.SMALL_ICON, getSwingEnvironment().getIcon(id));
  }

  // remove all newlines, tabs etc.
  private static String cleanupString(String s) {
    if (s != null) {
      return s.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
    }
    return s;
  }

  private void handleSwingAction() {
    if (SwingUtility.runInputVerifier()) {
      if (!m_handleActionPending) {
        m_handleActionPending = true;
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getScoutObject().getUIFacade().fireActionFromUI();
            }
            finally {
              m_handleActionPending = false;
            }
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
      }
    }
  }

  /**
   * in swing thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IAction.PROP_ENABLED)) {
      setEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_VISIBLE)) {
      setVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_TEXT)) {
      setTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_TOOLTIP_TEXT)) {
      setTooltipTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_MNEMONIC)) {
      setMnemonicFromScout(((Character) newValue).charValue());
    }
    else if (name.equals(IAction.PROP_KEYSTROKE)) {
      setKeyStrokeFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_ICON_ID)) {
      setIconFromScout((String) newValue);
    }
  }

  private class P_SwingAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public P_SwingAction(String name) {
      super(name);
      registerCompositeOnAction(this, SwingScoutAction.this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      handleSwingAction();
    }
  }// end private class
}
