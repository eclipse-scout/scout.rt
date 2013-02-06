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
package org.eclipse.scout.rt.client.ui.form.fields.treebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * Filter panel normally displayed in the ui (swing, swt) below the treebox
 * table. Showing 2 radio button groups, one for checked filter, one for active
 * filter. o show checked o show all o show active o show inactive o show all
 */
public abstract class AbstractTreeBoxFilterBox extends AbstractGroupBox {
  private final OptimisticLock m_treeBoxSyncLock;
  private PropertyChangeListener m_treeBoxPropertyListener;

  public AbstractTreeBoxFilterBox() {
    this(true);
  }

  public AbstractTreeBoxFilterBox(boolean callInitializer) {
    super(callInitializer);
    m_treeBoxSyncLock = new OptimisticLock();
  }

  protected abstract ITreeBox getTreeBox();

  @Override
  protected boolean getConfiguredBorderVisible() {
    return false;
  }

  @Override
  protected int getConfiguredGridW() {
    return 1;
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  protected int getConfiguredGridColumnCount() {
    return 1;
  }

  @Override
  protected void execInitField() throws ProcessingException {
    if (m_treeBoxPropertyListener == null) {
      m_treeBoxPropertyListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
          String name = e.getPropertyName();
          if (ITreeBox.PROP_FILTER_CHECKED_NODES.equals(name)) {
            updateVisibilities();
          }
          else if (ITreeBox.PROP_FILTER_ACTIVE_NODES.equals(name)) {
            updateVisibilities();
          }
          else if (ITreeBox.PROP_FILTER_CHECKED_NODES_VALUE.equals(name)) {
            try {
              if (m_treeBoxSyncLock.acquire()) {
                getCheckedStateRadioButtonGroup().setValue(getTreeBox().getFilterCheckedNodesValue());
              }
            }
            finally {
              m_treeBoxSyncLock.release();
            }
          }
          else if (ITreeBox.PROP_FILTER_ACTIVE_NODES_VALUE.equals(name)) {
            try {
              if (m_treeBoxSyncLock.acquire()) {
                getActiveStateRadioButtonGroup().setValue(getTreeBox().getFilterActiveNodesValue());
              }
            }
            finally {
              m_treeBoxSyncLock.release();
            }
          }
        }
      };
      getTreeBox().addPropertyChangeListener(m_treeBoxPropertyListener);
    }
    try {
      m_treeBoxSyncLock.acquire();
      //
      updateVisibilities();
      getCheckedStateRadioButtonGroup().setValue(getTreeBox().getFilterCheckedNodesValue());
      getActiveStateRadioButtonGroup().setValue(getTreeBox().getFilterActiveNodesValue());
    }
    finally {
      m_treeBoxSyncLock.release();
    }
  }

  protected void updateVisibilities() {
    this.setVisible(getTreeBox().isFilterCheckedNodes() || getTreeBox().isFilterActiveNodes());
    getCheckedStateRadioButtonGroup().setVisible(getTreeBox().isFilterCheckedNodes());
    getActiveStateRadioButtonGroup().setVisible(getTreeBox().isFilterActiveNodes());
  }

  @Override
  protected void execDisposeField() throws ProcessingException {
    if (m_treeBoxPropertyListener != null) {
      getTreeBox().removePropertyChangeListener(m_treeBoxPropertyListener);
      m_treeBoxPropertyListener = null;
    }
  }

  @Order(10)
  public class CheckedStateRadioButtonGroup extends AbstractRadioButtonGroup<Boolean> {

    @Override
    protected void execAddSearchTerms(SearchFilter search) {
      //nop
    }

    @Override
    protected boolean getConfiguredGridUseUiHeight() {
      return true;
    }

    @Override
    protected boolean getConfiguredGridUseUiWidth() {
      return true;
    }

    @Override
    protected double getConfiguredGridWeightX() {
      return 1;
    }

    @Override
    protected boolean getConfiguredLabelVisible() {
      return false;
    }

    @Override
    protected boolean execIsEmpty() throws ProcessingException {
      return true;
    }

    @Override
    protected boolean execIsSaveNeeded() throws ProcessingException {
      return false;
    }

    @Override
    protected void execChangedValue() throws ProcessingException {
      try {
        if (m_treeBoxSyncLock.acquire()) {
          Boolean b = getCheckedStateRadioButtonGroup().getValue();
          getTreeBox().setFilterCheckedNodesValue(b != null && b);
        }
      }
      finally {
        m_treeBoxSyncLock.release();
      }
    }

    @Order(10)
    public class CheckedButton extends AbstractButton {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_RADIO;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("SelectedStates");
      }

      @Override
      protected Object getConfiguredRadioValue() {
        return true;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    @Order(20)
    public class AllButton extends AbstractButton {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_RADIO;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("AllStates");
      }

      @Override
      protected Object getConfiguredRadioValue() {
        return false;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

  }

  @Order(20)
  public class ActiveStateRadioButtonGroup extends AbstractRadioButtonGroup<TriState> {

    @Override
    protected void execAddSearchTerms(SearchFilter search) {
      //nop
    }

    @Override
    protected boolean getConfiguredGridUseUiHeight() {
      return true;
    }

    @Override
    protected boolean getConfiguredGridUseUiWidth() {
      return true;
    }

    @Override
    protected double getConfiguredGridWeightX() {
      return 1;
    }

    @Override
    protected boolean getConfiguredLabelVisible() {
      return false;
    }

    @Override
    protected boolean execIsEmpty() throws ProcessingException {
      return true;
    }

    @Override
    protected boolean execIsSaveNeeded() throws ProcessingException {
      return false;
    }

    @Override
    protected void execChangedValue() throws ProcessingException {
      try {
        if (m_treeBoxSyncLock.acquire()) {
          getTreeBox().setFilterActiveNodesValue(getActiveStateRadioButtonGroup().getValue());
        }
      }
      finally {
        m_treeBoxSyncLock.release();
      }
    }

    @Order(10)
    public class ActiveButton extends AbstractButton {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_RADIO;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ActiveStates");
      }

      @Override
      protected Object getConfiguredRadioValue() {
        return TriState.TRUE;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    @Order(20)
    public class InactiveButton extends AbstractButton {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_RADIO;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("InactiveStates");
      }

      @Override
      protected Object getConfiguredRadioValue() {
        return TriState.FALSE;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    @Order(30)
    public class ActiveAndInactiveButton extends AbstractButton {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected int getConfiguredDisplayStyle() {
        return DISPLAY_STYLE_RADIO;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ActiveAndInactiveStates");
      }

      @Override
      protected Object getConfiguredRadioValue() {
        return TriState.UNDEFINED;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }
  }

  public ActiveStateRadioButtonGroup getActiveStateRadioButtonGroup() {
    return getFieldByClass(ActiveStateRadioButtonGroup.class);
  }

  public CheckedStateRadioButtonGroup getCheckedStateRadioButtonGroup() {
    return getFieldByClass(CheckedStateRadioButtonGroup.class);
  }

}
