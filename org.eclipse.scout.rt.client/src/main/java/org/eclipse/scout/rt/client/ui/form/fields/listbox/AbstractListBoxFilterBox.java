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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.shared.TEXTS;
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
      m_listBoxPropertyListener = e -> {
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
  @ClassId("e62c300f-f49d-4318-95ce-44a60558cfbf")
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
    @ClassId("3e47a9b5-aa90-4e2d-8901-23047d689a59")
    public class CheckedButton extends AbstractRadioButton<Boolean> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("SelectedStates");
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
    @ClassId("b37f25c6-ab52-4ff2-a167-7980d0f4f71b")
    public class AllButton extends AbstractRadioButton<Boolean> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("AllStates");
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
  @ClassId("2c4e4cf0-7bcf-46f1-a00f-19ecd2719fff")
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
    @ClassId("ad7425e4-9fa6-4dd6-944b-1f138586eff2")
    public class ActiveButton extends AbstractRadioButton<TriState> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("ActiveStates");
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
    @ClassId("dd6c5a35-ea0e-459d-919f-9155d46a63eb")
    public class InactiveButton extends AbstractRadioButton<TriState> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("InactiveStates");
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
    @ClassId("526bc98f-9b10-4285-bf3a-3feb6d560e22")
    public class ActiveAndInactiveButton extends AbstractRadioButton<TriState> {

      @Override
      protected void execAddSearchTerms(SearchFilter search) {
        //nop
      }

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("ActiveAndInactiveStates");
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
