/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * Filter panel normally displayed in the of the UI renderer. Showing 2 radio button groups, one for checked filter, one
 * for active filter. o show checked o show all o show active o show inactive o show all
 */
@ClassId("3c7ec46e-1f1b-4e1f-a65e-cdf24505c12a")
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
  protected void execInitField() {
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
  protected void execDisposeField() {
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
    protected boolean execIsEmpty() {
      return true;
    }

    @Override
    protected boolean execIsSaveNeeded() {
      return false;
    }

    @Override
    protected void execChangedValue() {
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
    public class CheckedButton extends AbstractRadioButton<Boolean> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("SelectedStates");
      }

      @Override
      protected Boolean getConfiguredRadioValue() {
        return true;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    @Order(20)
    public class AllButton extends AbstractRadioButton<Boolean> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("AllStates");
      }

      @Override
      protected Boolean getConfiguredRadioValue() {
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
    protected boolean execIsEmpty() {
      return true;
    }

    @Override
    protected boolean execIsSaveNeeded() {
      return false;
    }

    @Override
    protected void execChangedValue() {
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
    public class ActiveButton extends AbstractRadioButton<TriState> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ActiveStates");
      }

      @Override
      protected TriState getConfiguredRadioValue() {
        return TriState.TRUE;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    @Order(20)
    public class InactiveButton extends AbstractRadioButton<TriState> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("InactiveStates");
      }

      @Override
      protected TriState getConfiguredRadioValue() {
        return TriState.FALSE;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    @Order(30)
    public class ActiveAndInactiveButton extends AbstractRadioButton<TriState> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ActiveAndInactiveStates");
      }

      @Override
      protected TriState getConfiguredRadioValue() {
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
