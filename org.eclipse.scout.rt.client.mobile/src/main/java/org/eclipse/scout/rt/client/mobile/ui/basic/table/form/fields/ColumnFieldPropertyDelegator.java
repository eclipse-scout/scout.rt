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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.PropertyDelegator;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * @since 3.9.0
 */
public class ColumnFieldPropertyDelegator<SENDER extends IColumn<?>, RECEIVER extends IFormField> extends PropertyDelegator<SENDER, RECEIVER> {
  private P_TableListener m_tableListener;

  public ColumnFieldPropertyDelegator(SENDER sender, RECEIVER receiver) {
    super(sender, receiver);

    m_tableListener = new P_TableListener();
    getSender().getTable().addTableListener(m_tableListener);
  }

  @Override
  public void dispose() {
    super.dispose();
    getSender().getTable().removeTableListener(m_tableListener);
    m_tableListener = null;
  }

  @Override
  public void init() {
    getReceiver().setVisible(getSender().isVisible());
    if (!getSender().isVisible()) {
      getReceiver().setVisibleGranted(getSender().isVisibleGranted());
    }

    String label = getSender().getHeaderCell().getText();
    if (isRemoveLabelLineBreaksEnabled()) {
      label = StringUtility.removeNewLines(label);
    }
    getReceiver().setLabel(label);
    getReceiver().setTooltipText(getSender().getHeaderCell().getTooltipText());
    getReceiver().setEnabled(getSender().isCellEditable(getSender().getTable().getSelectedRow()));
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    if (name.equals(IColumn.PROP_VISIBLE)) {
      getReceiver().setVisible(((Boolean) newValue).booleanValue());
    }
    if (name.equals(IColumn.PROP_EDITABLE)) {
      getReceiver().setEnabled(((Boolean) newValue).booleanValue());
    }
  }

  protected boolean isRemoveLabelLineBreaksEnabled() {
    return true;
  }

  protected void handleColumnHeaderChanged(IColumn<?> column) {
    String label = getSender().getHeaderCell().getText();
    if (isRemoveLabelLineBreaksEnabled()) {
      label = StringUtility.removeNewLines(label);
    }
    getReceiver().setLabel(label);
    getReceiver().setTooltipText(column.getHeaderCell().getTooltipText());
  }

  protected void handleTableEvent(TableEvent event) {
    if (TableEvent.TYPE_COLUMN_HEADERS_UPDATED == event.getType()) {
      for (IColumn column : event.getColumns()) {
        if (column.equals(getSender())) {
          handleColumnHeaderChanged(column);
        }
      }
    }
  }

  private class P_TableListener extends TableAdapter {

    @Override
    public void tableChanged(TableEvent e) {
      handleTableEvent(e);
    }

  }

}
