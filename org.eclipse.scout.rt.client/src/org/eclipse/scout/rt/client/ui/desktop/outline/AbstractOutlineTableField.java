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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

/**
 * This field can be used to place the default outline table (which changes on
 * every outline tree change) inside a custom form.<br>
 * The default outline table form makes use of this field.
 */
public abstract class AbstractOutlineTableField extends AbstractTableField<ITable> {
  private DesktopListener m_desktopListener;
  private PropertyChangeListener m_tablePropertyListener;

  public AbstractOutlineTableField() {
    this(true);
  }

  public AbstractOutlineTableField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  /**
   * called whenever the table title changed
   */
  @ConfigOperation
  @Order(1000)
  protected void execTableTitleChanged() {
    if (getTable() != null) {
      setLabel(getTable().getTitle());
    }
    else {
      setLabel(null);
    }
  }

  @Override
  protected void execInitField() throws ProcessingException {
    m_tablePropertyListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(ITable.PROP_TITLE)) {
          execTableTitleChanged();
        }
      }
    };
  }

  @Override
  protected void execDisposeField() throws ProcessingException {
    super.execDisposeField();
    ClientSyncJob.getCurrentSession().getDesktop().removeDesktopListener(m_desktopListener);
    m_desktopListener = null;
  }

  public void installTable(ITable table) {
    if (getTable() == table) {
      return;
    }
    //
    if (getTable() != null) {
      getTable().removePropertyChangeListener(m_tablePropertyListener);
    }
    setTable(table, true);
    if (getTable() != null) {
      getTable().addPropertyChangeListener(m_tablePropertyListener);
    }
    execTableTitleChanged();
  }
}
