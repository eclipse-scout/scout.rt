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
package org.eclipse.scout.rt.client.extension.ui.form.fields.listbox;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxLoadTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxPopulateTableChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface IListBoxExtension<KEY, OWNER extends AbstractListBox<KEY>> extends IValueFieldExtension<Set<KEY>, OWNER> {

  void execPopulateTable(ListBoxPopulateTableChain<KEY> chain);

  List<? extends ILookupRow<KEY>> execLoadTableData(ListBoxLoadTableDataChain<KEY> chain);

  void execFilterLookupResult(ListBoxFilterLookupResultChain<KEY> chain, ILookupCall<KEY> call, List<ILookupRow<KEY>> result);

  void execPrepareLookup(ListBoxPrepareLookupChain<KEY> chain, ILookupCall<KEY> call);
}
