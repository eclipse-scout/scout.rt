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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

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
 * Filter panel normally displayed below the listbox table. Showing 2 radio button groups, one for checked filter, one
 * for active filter. o show checked o show all o show active o show inactive o show all
 */
@ClassId("ebca1411-044f-425f-b63c-4920cde6bb1b")
public abstract class AbstractListBoxFilterBox extends AbstractGroupBox {
  private final OptimisticLock m_listBoxSyncLock;
  private PropertyChangeListener m_listBoxPropertyListener;

  public AbstractListBoxFilterBox() {
    this(true);
  }

  public AbstractListBoxFilterBox(boolean callInitializer) {
    super(callInitializer);
    m_listBoxSyncLock = new OptimisticLock();
  }

  protected abstract IListBox getListBox();

  @Override
  protected boolean getConfiguredBorderVisible() {
    return false;
  }

  @Override
  protected int getConfiguredGridColumnCount() {
    return 1;
  }

  @Override
  protected void execInitField() {
    if (m_listBoxPropertyListener == null) {
      m_listBoxPropertyListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
          String name = e.getPropertyName();
          if (IListBox.PROP_FILTER_CHECKED_ROWS.equals(name)) {
            updateVisibilities();
          }
          else if (IListBox.PROP_FILTER_ACTIVE_ROWS.equals(name)) {
            updateVisibilities();
          }
          else if (IListBox.PROP_FILTER_CHECKED_ROWS_VALUE.equals(name)) {
            try {
              if (m_listBoxSyncLock.acquire()) {
                getCheckedStateRadioButtonGroup().setValue(getListBox().getFilterCheckedRowsValue());
              }
            }
            finally {
              m_listBoxSyncLock.release();
            }
          }
          else if (IListBox.PROP_FILTER_ACTIVE_ROWS_VALUE.equals(name)) {
            try {
              if (m_listBoxSyncLock.acquire()) {
                getActiveStateRadioButtonGroup().setValue(getListBox().getFilterActiveRowsValue());
              }
            }
            finally {
              m_listBoxSyncLock.release();
            }
          }
        }
      };
      getListBox().addPropertyChangeListener(m_listBoxPropertyListener);
    }
    try {
      m_listBoxSyncLock.acquire();
      //
      updateVisibilities();
      getCheckedStateRadioButtonGroup().setValue(getListBox().getFilterCheckedRowsValue());
      getActiveStateRadioButtonGroup().setValue(getListBox().getFilterActiveRowsValue());
    }
    finally {
      m_listBoxSyncLock.release();
    }
  }

  protected void updateVisibilities() {
    this.setVisible(getListBox().isFilterCheckedRows() || getListBox().isFilterActiveRows());
    getCheckedStateRadioButtonGroup().setVisible(getListBox().isFilterCheckedRows());
    getActiveStateRadioButtonGroup().setVisible(getListBox().isFilterActiveRows());
  }

  @Override
  protected void execDisposeField() {
    if (m_listBoxPropertyListener != null) {
      getListBox().removePropertyChangeListener(m_listBoxPropertyListener);
      m_listBoxPropertyListener = null;
    }
  }

  @Order(10)
  public class CheckedStateRadioButtonGroup extends AbstractRadioButtonGroup<Boolean> {

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
    protected void execAddSearchTerms(SearchFilter search) {
      //nop
    }

    @Override
    protected void execChangedValue() {
      try {
        if (m_listBoxSyncLock.acquire()) {
          Boolean b = getCheckedStateRadioButtonGroup().getValue();
          getListBox().setFilterCheckedRowsValue(b != null && b);
        }
      }
      finally {
        m_listBoxSyncLock.release();
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
    protected void execAddSearchTerms(SearchFilter search) {
      //nop
    }

    @Override
    protected void execChangedValue() {
      try {
        if (m_listBoxSyncLock.acquire()) {
          getListBox().setFilterActiveRowsValue(getActiveStateRadioButtonGroup().getValue());
        }
      }
      finally {
        m_listBoxSyncLock.release();
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
