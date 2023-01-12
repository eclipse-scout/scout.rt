/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineTableFieldChains.OutlineTableFieldTableTitleChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.AbstractTableFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineTableField;

public abstract class AbstractOutlineTableFieldExtension<OWNER extends AbstractOutlineTableField> extends AbstractTableFieldExtension<ITable, OWNER> implements IOutlineTableFieldExtension<OWNER> {

  public AbstractOutlineTableFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execTableTitleChanged(OutlineTableFieldTableTitleChangedChain chain) {
    chain.execTableTitleChanged();
  }
}
