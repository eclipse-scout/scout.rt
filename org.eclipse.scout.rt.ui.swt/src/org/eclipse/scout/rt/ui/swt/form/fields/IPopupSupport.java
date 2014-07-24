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
package org.eclipse.scout.rt.ui.swt.form.fields;

/**
 * Fields that open popup dialogs to edit their value (e.g.
 * {@link org.eclipse.scout.rt.ui.swt.form.fields.datefield.SwtScoutDateField SwtScoutDateField},
 * {@link org.eclipse.scout.rt.ui.swt.form.fields.datefield.SwtScoutTimeField SwtScoutTimeField},
 * {@link org.eclipse.scout.rt.ui.swt.form.fields.smartfield.SwtScoutSmartField SwtScoutSmartField}) should implement
 * this interface. This allows listeners to be
 * notified about the popup state. Especially, this is crucial if the field is used inline within an editable table
 * to handle focus-lost events properly (see
 * {@link org.eclipse.scout.rt.ui.swt.basic.table.celleditor.SwtScoutTableCellEditor.P_FocusLostListener
 * SwtScoutTableCellEditor.P_FocusLostListener}).
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
    int TYPE_OPENING = 1 << 1;
    /**
     * the popup is closed
     */
    int TYPE_CLOSED = 1 << 2;

    void handleEvent(int eventType);
  }
}
