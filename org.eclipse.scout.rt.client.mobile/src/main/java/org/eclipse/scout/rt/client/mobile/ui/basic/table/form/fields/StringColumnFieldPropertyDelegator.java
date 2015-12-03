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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;

/**
 * @since 3.9.0
 */
public class StringColumnFieldPropertyDelegator extends ColumnFieldPropertyDelegator<IStringColumn, IStringField> {

  public StringColumnFieldPropertyDelegator(IStringColumn sender, IStringField receiver) {
    super(sender, receiver);
  }

  @Override
  public void init() {
    super.init();

    getReceiver().setInputMasked(getSender().isInputMasked());
    getReceiver().setFormat(getSender().getDisplayFormat());
    getReceiver().setWrapText(getSender().isTextWrap());
    if (getSender().isTextWrap()) {
      //Text wrap typically only works if multiline is enabled
      getReceiver().setMultilineText(true);
    }

    if (getReceiver().isMultilineText()) {
      //Make the field bigger in case of multiline text so the user can read / edit the data easier
      GridData gd = getReceiver().getGridDataHints();
      gd.h = 2;
      getReceiver().setGridDataHints(gd);
    }
  }

}
