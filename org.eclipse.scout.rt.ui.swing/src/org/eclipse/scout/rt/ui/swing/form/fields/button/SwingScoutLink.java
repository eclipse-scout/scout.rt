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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JHyperlink;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

/**
 * Composition between a scout IButton and a swing
 * JButton/JToggleButton/JRadioButton
 */
public class SwingScoutLink extends SwingScoutFieldComposite<IButton> implements ISwingScoutLink {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutLink.class);

  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  public SwingScoutLink() {
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    container.setName(getScoutObject().getClass().getSimpleName() + ".container");
    //
    JHyperlink swingLink = new JHyperlink();
    swingLink.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleSwingAction();
      }
    });
    //
    container.add(swingLink);
    setSwingLabel(null);
    setSwingField(swingLink);
    LogicalGridData gd = (LogicalGridData) swingLink.getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    //set default button height
    if (getScoutObject().isProcessButton() && !gd.useUiHeight) {
      gd.useUiHeight = true;
      gd.heightHint = getSwingEnvironment().getProcessButtonHeight();
    }
    setSwingContainer(container);
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 0, 0));
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    getSwingField().setEnabled(b);
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    getSwingField().setAlignmentX(SwingUtility.createAlignmentX(scoutAlign));
    if (getSwingContainer().isShowing()) {
      getSwingContainer().revalidate();
    }
  }

  @Override
  protected void setVerticalAlignmentFromScout(int scoutAlign) {
    getSwingField().setAlignmentY(SwingUtility.createAlignmentY(scoutAlign));
    if (getSwingContainer().isShowing()) {
      getSwingContainer().revalidate();
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    String label = StringUtility.removeMnemonic(s);
    if (getSwingField() instanceof JTextComponent) {
      JTextComponent comp = (JTextComponent) getSwingField();
      comp.setText(label);
    }
    else if (getSwingField() instanceof JLabel) {
      JLabel comp = (JLabel) getSwingField();
      comp.setText(label);
    }
    SwingLayoutUtility.invalidateAncestors(getSwingField());
  }

  private void handleSwingAction() {
    if (!m_handleActionPending) {
      m_handleActionPending = true;
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            getScoutObject().getUIFacade().fireButtonClickedFromUI();
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
