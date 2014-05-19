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
package org.eclipse.scout.rt.ui.swing.form.fields.button;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonEvent;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonListener;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup.IconState;
import org.eclipse.scout.rt.ui.swing.ext.JButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JDropDownButton;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JRadioButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JToggleButtonEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

/**
 * Composition between a scout IButton and a swing
 * JButton/JToggleButton/JRadioButton
 */
public class SwingScoutButton<T extends IButton> extends SwingScoutFieldComposite<T> implements ISwingScoutButton<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutButton.class);

  private ButtonListener m_scoutButtonListener;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  public SwingScoutButton() {
  }

  @Override
  protected void initializeSwing() {
    JComponent container;
    AbstractButton swingFieldAsButton = null;
    switch (getScoutButton().getDisplayStyle()) {
      case IButton.DISPLAY_STYLE_RADIO: {
        swingFieldAsButton = createSwingRadioButton();
        break;
      }
      case IButton.DISPLAY_STYLE_TOGGLE: {
        swingFieldAsButton = createSwingToggleButton();
        break;
      }
      default: {
        swingFieldAsButton = createSwingPushButton();
      }
    }
    //
    swingFieldAsButton.setName(getScoutButton().getClass().getSimpleName());
    SwingUtility.installDefaultFocusHandling(swingFieldAsButton);
    swingFieldAsButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    swingFieldAsButton.setVerifyInputWhenFocusTarget(true);
    swingFieldAsButton.setRequestFocusEnabled(true);
    // attach swing listeners
    swingFieldAsButton.addActionListener(new P_SwingActionListener());
    // check if button has menus
    if (getScoutObject().getContextMenu().hasChildActions()) {
      JDropDownButton dropDownButton = new JDropDownButton(swingFieldAsButton);
      dropDownButton.getMenuButton().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          handleSwingPopup((Component) e.getSource());
        }
      });
      container = new JPanelEx();
      container.add(dropDownButton);
    }
    else {
      container = new JPanelEx();
      container.add(swingFieldAsButton);
    }
    //
    container.setName(getScoutButton().getClass().getSimpleName() + ".container");
    setSwingLabel(null);
    setSwingField(swingFieldAsButton);

    //in case the button is inside a dropdowncomposite, copy the griddata to the drop down composite
    LogicalGridData gd = (LogicalGridData) swingFieldAsButton.getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    adaptButtonLayoutData(gd);
    ((JComponent) container.getComponent(0)).putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, gd);
    setSwingContainer(container);
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 0, 0));
  }

  /**
   * Create the gridData for the Button
   * 
   * @since 4.0.0-M7
   */
  protected void adaptButtonLayoutData(LogicalGridData gd) {
    if (getScoutObject().isProcessButton() && !gd.useUiHeight) {
      //set default button height
      gd.useUiHeight = true;
      gd.heightHint = getSwingEnvironment().getProcessButtonHeight();
    }
  }

  /**
   * @since 4.0.0-M7
   */
  protected JRadioButton createSwingRadioButton() {
    JRadioButton swingButton = new JRadioButtonEx();
    swingButton.setOpaque(false);
    swingButton.setRolloverEnabled(false);
    swingButton.addItemListener(new P_SwingSelectionListener());
    swingButton.setAlignmentX(0);
    swingButton.setVerticalAlignment(SwingConstants.TOP);
    return swingButton;
  }

  /**
   * @since 4.0.0-M7
   */
  protected JToggleButtonEx createSwingToggleButton() {
    JToggleButtonEx swingButton = new JToggleButtonEx();
    swingButton.addItemListener(new P_SwingSelectionListener());
    swingButton.setAlignmentX(0.5f);
    return swingButton;
  }

  /**
   * @since 4.0.0-M7
   */
  protected JButtonEx createSwingPushButton() {
    JButtonEx swingButton = new JButtonEx();
    swingButton.setAlignmentX(0.5f);
    return swingButton;
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutButtonListener != null) {
      getScoutButton().removeButtonListener(m_scoutButtonListener);
      m_scoutButtonListener = null;
    }
  }

  public T getScoutButton() {
    return getScoutObject();
  }

  @Override
  public AbstractButton getSwingButton() {
    return (AbstractButton) getSwingField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutButtonListener == null) {
      m_scoutButtonListener = new P_ScoutButtonListener();
      getScoutButton().addButtonListener(m_scoutButtonListener);
    }
    T b = getScoutButton();
    setIconIdFromScout(b.getIconId());
    setImageFromScout(b.getImage());
    setSelectionFromScout(b.isSelected());
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    if (getScoutButton().getDisplayStyle() == IButton.DISPLAY_STYLE_LINK && scoutColor == null) {
      scoutColor = "445599";
    }
    super.setForegroundFromScout(scoutColor);
  }

  protected void setIconIdFromScout(String s) {
    if (s != null) {
      JComponent comp = getSwingButton();
      if (comp instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) comp;
        IconGroup iconGroup = new IconGroup(getSwingEnvironment(), s);
        b.setIcon(iconGroup.getIcon(IconState.NORMAL));
        if (iconGroup.hasIcon(IconState.DISABLED)) {
          b.setDisabledIcon(iconGroup.getIcon(IconState.DISABLED));
        }
        if (iconGroup.hasIcon(IconState.ROLLOVER)) {
          b.setRolloverIcon(iconGroup.getIcon(IconState.ROLLOVER));
        }
        if (iconGroup.hasIcon(IconState.SELECTED)) {
          b.setPressedIcon(iconGroup.getIcon(IconState.SELECTED));
          b.setSelectedIcon(iconGroup.getIcon(IconState.SELECTED));
        }
      }
    }
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    getSwingButton().setAlignmentX(SwingUtility.createAlignmentX(scoutAlign));
    if (getSwingContainer().isShowing()) {
      getSwingContainer().revalidate();
    }
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    getSwingButton().setAlignmentY(SwingUtility.createAlignmentY(scoutAlign));
    if (getSwingContainer().isShowing()) {
      getSwingContainer().revalidate();
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    String label = StringUtility.removeMnemonic(s);
    JComponent comp = getSwingButton();
    if (comp instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) comp;
      b.setText(label);
      if (StringUtility.getMnemonic(s) != 0x0) {
        b.setMnemonic(StringUtility.getMnemonic(s));
      }
    }
    SwingLayoutUtility.invalidateAncestors(comp);
  }

  protected void setSelectionFromScout(boolean b) {
    if (getSwingButton() instanceof JToggleButton) {
      ((JToggleButton) getSwingButton()).setSelected(b);
    }
    else if (getSwingButton() instanceof JRadioButton) {
      if (b) {
        ((JRadioButton) getSwingButton()).setSelected(b);
      }
    }
  }

  protected void setSelectionFromSwing(final boolean b) {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    if (getScoutButton().isSelected() != b) {
      // radio button behavior since swing fires deselections
      if (getSwingButton() instanceof JRadioButton && !b) {
        // avoid deselection
        ((JRadioButton) getSwingButton()).setSelected(getScoutButton().isSelected());
      }
      else {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutButton().getUIFacade().setSelectedFromUI(b);
          }
        };

        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void setImageFromScout(Object img) {
    if (img instanceof Image) {
      JComponent comp = getSwingButton();
      if (comp instanceof AbstractButton) {
        AbstractButton b = (AbstractButton) comp;
        b.setIcon(new ImageIcon((Image) img));
        b.setHorizontalAlignment(SwingConstants.LEADING);
      }
    }
  }

  protected void disarmButtonFromScout() {
    JComponent comp = getSwingButton();
    if (comp instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) comp;
      b.getModel().setArmed(false);
    }
  }

  protected void requestPopupFromScout() {
    handleSwingPopup(getSwingButton());
  }

  /**
   * in swing thread
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IButton.PROP_ICON_ID)) {
      setIconIdFromScout((String) newValue);
    }
    else if (name.equals(IButton.PROP_IMAGE)) {
      setImageFromScout(newValue);
    }
    else if (name.equals(IButton.PROP_SELECTED)) {
      setSelectionFromScout(((Boolean) newValue).booleanValue());
    }
  }

  protected void handleSwingAction() {
    if (!m_handleActionPending) {
      m_handleActionPending = true;
      //notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            getScoutButton().getUIFacade().fireButtonClickedFromUI();
          }
          finally {
            m_handleActionPending = false;
          }
        }
      };
      switch (getScoutButton().getDisplayStyle()) {
        case IButton.DISPLAY_STYLE_RADIO:
        case IButton.DISPLAY_STYLE_TOGGLE: {

          break;
        }
      }
      getSwingEnvironment().invokeScoutLater(t, 0);
      //end notify
    }
  }

  protected void handleSwingPopup(final Component source) {
    final Point point = new Point(0, source.getHeight());
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), source, point, getScoutButton().getContextMenu()).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  /*
   * Listeners
   */
  private class P_SwingActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      handleSwingAction();
    }
  }// end class

  private class P_SwingSelectionListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {

      setSelectionFromSwing((getSwingButton()).isSelected());
    }
  }// end private class

  private class P_ScoutButtonListener implements ButtonListener, WeakEventListener {
    @Override
    public void buttonChanged(ButtonEvent e) {
      if (isIgnoredScoutEvent(ButtonEvent.class, "" + e.getType())) {
        return;
      }
      //
      switch (e.getType()) {
        case ButtonEvent.TYPE_DISARM: {
          getSwingEnvironment().invokeSwingLater(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    getUpdateSwingFromScoutLock().acquire();
                    //
                    disarmButtonFromScout();
                  }
                  finally {
                    getUpdateSwingFromScoutLock().release();
                  }
                }
              });
          break;
        }
        case ButtonEvent.TYPE_REQUEST_POPUP: {
          getSwingEnvironment().invokeSwingLater(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    getUpdateSwingFromScoutLock().acquire();
                    //
                    requestPopupFromScout();
                  }
                  finally {
                    getUpdateSwingFromScoutLock().release();
                  }
                }
              });
          break;
        }
      }
    }
  }

}
