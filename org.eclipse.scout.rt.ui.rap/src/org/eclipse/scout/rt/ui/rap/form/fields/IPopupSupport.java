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
package org.eclipse.scout.rt.ui.rap.form.fields;

import org.eclipse.scout.rt.ui.rap.basic.table.celleditor.RwtScoutTableCellEditor;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutTimeField;
import org.eclipse.scout.rt.ui.rap.form.fields.smartfield.RwtScoutSmartField;

/**
 * Fields that open popup dialogs to edit their value (e.g. {@link RwtScoutDateField}, {@link RwtScoutTimeField},
 * {@link RwtScoutSmartField}) should implement this interface. This allows listeners to be
 * notified about the popup state. Especially, this is crucial if the field is used inline within an editable table
 * to handle focus-lost events properly (see {@link RwtScoutTableCellEditor.P_FocusLostListener}).
 */
public interface IPopupSupport {

  /**
   * To register a listener to receive events about the popup state
   * 
   * @param listener
   */
  void addPopupEventListener(IPopupSupportListener listener);

  void removePopupEventListener(IPopupSupportListener listener);

  interface IPopupSupportListener {

    /**
     * the popup is opening but not yet open
     */
    public static final int TYPE_OPENING = 1 << 1;
    /**
     * the popup is closed
     */
    public static final int TYPE_CLOSED = 1 << 2;

    void handleEvent(int eventType);
  }
}
