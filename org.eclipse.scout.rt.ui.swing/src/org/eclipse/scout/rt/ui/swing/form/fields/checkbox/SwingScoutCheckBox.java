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
package org.eclipse.scout.rt.ui.swing.form.fields.checkbox;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingScoutTable;
import org.eclipse.scout.rt.ui.swing.ext.JCheckBoxEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.icons.CheckboxIcon;

public class SwingScoutCheckBox extends SwingScoutValueFieldComposite<IBooleanField> implements ISwingScoutCheckBox {
  private static final long serialVersionUID = 1L;

  private boolean m_mandatoryCached;

  /**
   * To indicate that this checkbox is used inline within table cell
   */
  private boolean m_tableCellContext;

  /**
   * If {@link SwingScoutCheckBox#isTableCellContext()} is applicable, this member holds the insets of the parent table
   * cell
   */
  private Insets m_tableCellInsets;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JCheckBox swingCheckBox = createCheckBox(container);
    container.add(swingCheckBox);
    swingCheckBox.setVerifyInputWhenFocusTarget(true);
    swingCheckBox.setAlignmentX(0);
    swingCheckBox.setVerticalAlignment(SwingConstants.TOP);
    // attach swing listeners
    swingCheckBox.addActionListener(new P_SwingActionListener());

    setSwingLabel(label);
    setSwingField(swingCheckBox);
    setSwingContainer(container);

    LogicalGridData gd = (LogicalGridData) swingCheckBox.getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    gd.fillHorizontal = false; // must be false to be only as wide as the label. This avoids that clicking in white space area toggles the value (BSI ticket 101344)

    // If being used inline as table cell, change icons and UI constraints
    if (isTableCellContext()) {
      // icons
      CheckboxIcon iconNormalState = new CheckboxIcon(); // normal
      CheckboxIcon iconSelectedState = new CheckboxIcon(); // selected
      iconSelectedState.setSelected(true);
      iconSelectedState.setEnabled(true);
      CheckboxIcon iconDisabledState = new CheckboxIcon(); // disabled
      iconDisabledState.setEnabled(false);
      CheckboxIcon iconDisabledSelectedState = new CheckboxIcon(); // disabled and selected
      iconDisabledSelectedState.setEnabled(false);
      iconDisabledSelectedState.setSelected(true);

      swingCheckBox.setIcon(iconNormalState);
      swingCheckBox.setSelectedIcon(iconSelectedState);
      swingCheckBox.setDisabledIcon(iconDisabledState);
      swingCheckBox.setDisabledSelectedIcon(iconDisabledSelectedState);
      swingCheckBox.setRolloverEnabled(false);
      swingCheckBox.setRolloverIcon(null); // must be unset if set by L&F
      swingCheckBox.setPressedIcon(null); // must be unset if set by L&F

      // UI constraints
      // install same insets as of the parent table cell to ensure the editable checkbox to be positioned the at same location as the checkbox icon in table cell
      Insets cellInsets = new Insets(0, 0, 0, 0);
      if (getTableCellInsets() != null) {
        cellInsets = (Insets) getTableCellInsets().clone();
      }
      // correct top inset to ensure the checkbox to be positioned on the same y-location as the label texts of other columns
      if (getScoutObject().getGridDataHints().verticalAlignment == -1) {
        cellInsets.top += SwingScoutTable.FONT_PADDING_TOP;
      }
      swingCheckBox.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0))); // must be an empty border as insets are set in container insets
      container.setBorder(new EmptyBorder(cellInsets));

      gd.fillVertical = false; // must be false to allow vertical alignment
      gd.horizontalAlignment = getScoutObject().getGridDataHints().horizontalAlignment; // set alignment constraints (those constraints where injected by IBooleanColumn in @{link SwingScoutTableCellEditor})
      gd.verticalAlignment = getScoutObject().getGridDataHints().verticalAlignment; // set alignment constraints (those constraints where injected by IBooleanColumn in @{link SwingScoutTableCellEditor})
      gd.weighty = 1.0; // must be greater than 0 to allow vertical alignment
    }

    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected JCheckBox createCheckBox(JComponent container) {
    JCheckBoxEx swingCheckBox = new JCheckBoxEx();
    swingCheckBox.setOpaque(false);
    return swingCheckBox;
  }

  @Override
  public JCheckBoxEx getSwingCheckBox() {
    return (JCheckBoxEx) getSwingField();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    if (getSwingCheckBox() != null) {
      getSwingCheckBox().setHorizontalAlignment(SwingUtility.createHorizontalAlignment(scoutAlign));
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    getSwingCheckBox().setText(s);
  }

  @Override
  protected void setValueFromScout(Object o) {
    getSwingCheckBox().setSelected(((Boolean) o).booleanValue());
  }

  @Override
  protected void setMandatoryFromScout(boolean b) {
    if (b != m_mandatoryCached) {
      m_mandatoryCached = b;
      getSwingCheckBox().setMandatory(b);
      getSwingLabel().setMandatory(b); // bsh 2010-10-01: inform the label - some GUIs (e.g. Rayo) might use this information
    }
  }

  public boolean isTableCellContext() {
    return m_tableCellContext;
  }

  /**
   * To indicate that this checkbox is used inline within a table cell
   * 
   * @param tableCellContext
   */
  public void setTableCellContext(boolean tableCellContext) {
    m_tableCellContext = tableCellContext;
  }

  public Insets getTableCellInsets() {
    return m_tableCellInsets;
  }

  /**
   * If {@link SwingScoutCheckBox#isTableCellContext()} is applicable, this member holds the insets of the parent table
   * cell
   * 
   * @param insets
   */
  public void setTableCellInsets(Insets insets) {
    m_tableCellInsets = insets;
  }

  protected void handleSwingAction(ActionEvent e) {
    if (!getSwingCheckBox().isEnabled()) {
      return;
    }
    //notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        final boolean oldSelection = getScoutObject().isChecked();
        final boolean newSelection = getScoutObject().getUIFacade().setSelectedFromUI();
        if (oldSelection == newSelection) {
          // ensure that the UI has the same value as the Scout model
          // oldSelection != newSelection case is handled by the value property change listener.
          Runnable r = new Runnable() {
            @Override
            public void run() {
              getSwingCheckBox().setSelected(newSelection);
            }
          };
          getSwingEnvironment().invokeSwingLater(r);
        }
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 0);
    //end notify
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
}
