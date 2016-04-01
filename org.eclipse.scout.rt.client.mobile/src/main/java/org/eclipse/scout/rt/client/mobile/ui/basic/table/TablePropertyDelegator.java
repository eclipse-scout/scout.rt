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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.util.Set;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.PropertyDelegator;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;

public class TablePropertyDelegator<SENDER extends ITable, RECEIVER extends ITable> extends PropertyDelegator<SENDER, RECEIVER> {

  public TablePropertyDelegator(SENDER sender, RECEIVER receiver) {
    super(sender, receiver);
  }

  public TablePropertyDelegator(SENDER sender, RECEIVER receiver, Set<String> filteredPropertyNames) {
    super(sender, receiver, filteredPropertyNames);
  }

  @Override
  public void init() {
    super.init();

    getReceiver().setAutoDiscardOnDelete(getSender().isAutoDiscardOnDelete());
    getReceiver().setAutoResizeColumns(getSender().isAutoResizeColumns());
    getReceiver().setCheckable(getSender().isCheckable());
    getReceiver().setDefaultIconId(getSender().getDefaultIconId());
    getReceiver().setEnabled(getSender().isEnabled());
    getReceiver().setHeaderVisible(getSender().isHeaderVisible());
    getReceiver().setInitialMultilineText(getSender().isInitialMultilineText());
    getReceiver().setKeyboardNavigation(getSender().hasKeyboardNavigation());
    getReceiver().setMultiCheck(getSender().isMultiCheck());
    getReceiver().setMultilineText(getSender().isMultilineText());
    getReceiver().setMultiSelect(getSender().isMultiSelect());
    getReceiver().setScrollToSelection(getSender().isScrollToSelection());
    getReceiver().setSortEnabled(getSender().isSortEnabled());
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    super.handlePropertyChange(name, newValue);

    if (name.equals(ITable.PROP_AUTO_RESIZE_COLUMNS)) {
      getReceiver().setAutoResizeColumns(getSender().isAutoResizeColumns());
    }
    else if (name.equals(ITable.PROP_CHECKABLE)) {
      getReceiver().setCheckable(getSender().isCheckable());
    }
    else if (name.equals(ITable.PROP_DEFAULT_ICON)) {
      getReceiver().setDefaultIconId(getSender().getDefaultIconId());
    }
    else if (name.equals(ITable.PROP_ENABLED)) {
      getReceiver().setEnabled(getSender().isEnabled());
    }
    else if (name.equals(ITable.PROP_HEADER_VISIBLE)) {
      getReceiver().setHeaderVisible(getSender().isHeaderVisible());
    }
    else if (name.equals(ITable.PROP_KEYBOARD_NAVIGATION)) {
      getReceiver().setKeyboardNavigation(getSender().hasKeyboardNavigation());
    }
    else if (name.equals(ITable.PROP_MULTI_CHECK)) {
      getReceiver().setMultiCheck(getSender().isMultiCheck());
    }
    else if (name.equals(ITable.PROP_MULTILINE_TEXT)) {
      getReceiver().setMultilineText(getSender().isMultilineText());
    }
    else if (name.equals(ITable.PROP_MULTI_SELECT)) {
      getReceiver().setMultiSelect(getSender().isMultiSelect());
    }
    else if (name.equals(ITable.PROP_SCROLL_TO_SELECTION)) {
      getReceiver().setScrollToSelection(getSender().isScrollToSelection());
    }
  }
}
