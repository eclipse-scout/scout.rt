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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.PropertyDelegator;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @since 3.9.0
 */
public class ColumnFieldPropertyDelegator<SENDER extends IColumn<?>, RECEIVER extends IFormField> extends PropertyDelegator<SENDER, RECEIVER> {

  public ColumnFieldPropertyDelegator(SENDER sender, RECEIVER receiver) {
    super(sender, receiver);
  }

  @Override
  public void init() {
    getReceiver().setVisible(getSender().isVisible());
    if (!getSender().isVisible()) {
      getReceiver().setVisibleGranted(getSender().isVisibleGranted());
    }
    getReceiver().setLabel(getSender().getHeaderCell().getText());
    getReceiver().setTooltipText(getSender().getHeaderCell().getTooltipText());
    getReceiver().setEnabled(false);
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

}
