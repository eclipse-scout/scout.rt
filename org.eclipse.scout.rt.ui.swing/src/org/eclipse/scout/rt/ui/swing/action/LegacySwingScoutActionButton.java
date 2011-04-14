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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingConstants;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JToggleButtonEx;

/**
 * Composition between a scout IButton and a swing
 * JButton/JToggleButton/JRadioButton
 */
public class LegacySwingScoutActionButton<T extends IAction> extends SwingScoutComposite<T> implements ISwingScoutAction<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LegacySwingScoutActionButton.class);

  // locks
  private OptimisticLock m_selectionLock;

  public LegacySwingScoutActionButton() {
    super();
    m_selectionLock = new OptimisticLock();
  }

  @Override
  protected void initializeSwing() {
    AbstractButton swingButton;
    if (getScoutObject().isToggleAction()) {
      swingButton = new JToggleButtonEx();
      swingButton.addItemListener(new P_SwingSelectionListener());
    }
    else {
      swingButton = new JButtonEx();
    }
    SwingUtility.installDefaultFocusHandling(swingButton);
    swingButton.setMargin(new Insets(8, 4, 8, 4));
    swingButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    swingButton.setVerifyInputWhenFocusTarget(true);
    swingButton.setRequestFocusEnabled(true);
    setSwingField(swingButton);
    // attach swing listeners
    swingButton.addActionListener(new P_SwingActionListener());
  }

  @Override
  public Action getSwingAction() {
    return null;
  }

  @Override
  public AbstractButton getSwingField() {
    return (AbstractButton) super.getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IAction b = getScoutObject();
    setVisibleFromScout(b.isVisible());
    setEnabledFromScout(b.isEnabled());
    setTextFromScout(b.getText());
    setTooltipTextFromScout(b.getTooltipText());
    setIconIdFromScout(b.getIconId());
    setSelectionFromScout(b.isSelected());
  }

  protected void setIconIdFromScout(String s) {
    if (s != null) {
      AbstractButton b = getSwingField();
      Icon icon = getSwingEnvironment().getIcon(s);
      b.setIcon(icon);
      // when all 3 icons are defined, then the button border is deactivated
      Icon pressedIcon = getSwingEnvironment().getIcon(s + "_pressed");
      Icon rolloverIcon = getSwingEnvironment().getIcon(s + "_rollover");
      Icon disabledIcon = getSwingEnvironment().getIcon(s + "_disabled");
      if (pressedIcon != null && rolloverIcon != null && disabledIcon != null) {
        b.setRolloverEnabled(true);
        b.setMargin(null);
        b.setBorder(null);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPressedIcon(pressedIcon);
        b.setSelectedIcon(pressedIcon);
        b.setRolloverIcon(rolloverIcon);
        b.setDisabledIcon(disabledIcon);
      }
    }
  }

  protected void setTextFromScout(String s) {
    AbstractButton b = getSwingField();
    String label = StringUtility.removeMnemonic(s);
    b.setText(label);
    if (StringUtility.getMnemonic(s) != 0x0) {
      b.setMnemonic(StringUtility.getMnemonic(s));
    }
  }

  protected void setVisibleFromScout(boolean b) {
    getSwingField().setVisible(b);
  }

  protected void setEnabledFromScout(boolean b) {
    getSwingField().setEnabled(b);
  }

  protected void setSelectionFromScout(boolean b) {
    try {
      if (m_selectionLock.acquire()) {
        getSwingField().setSelected(b);
      }
    }
    finally {
      m_selectionLock.release();
    }
  }

  protected void setTooltipTextFromScout(String s) {
    getSwingField().setToolTipText(s);
  }

  protected void setSelectionFromSwing(final boolean b) {
    try {
      if (m_selectionLock.acquire()) {
        if (getScoutObject().isSelected() != b) {
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().setSelectedFromUI(b);
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
    }
    finally {
      m_selectionLock.release();
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
    else if (name.equals(IAction.PROP_TEXT)) {
      setTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_TOOLTIP_TEXT)) {
      setTooltipTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_VISIBLE)) {
      setVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_ICON_ID)) {
      setIconIdFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_SELECTED)) {
      setSelectionFromScout(((Boolean) newValue).booleanValue());
    }
  }

  protected void handleSwingAction(ActionEvent e) {
    // check domain parent
    if ((!getSwingField().isFocusable()) || getSwingField().isFocusOwner()) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireActionFromUI();
        }
      };

      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  /*
   * Listeners
   */
  private class P_SwingActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      handleSwingAction(e);
    }
  }// end class

  private class P_SwingSelectionListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      setSelectionFromSwing(getSwingField().isSelected());
    }
  }// end private class

}
