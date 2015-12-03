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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface IContentAssistFieldTable<LOOKUP_KEY> extends ITable {

  void setLookupRows(List<? extends ILookupRow<LOOKUP_KEY>> lookupRows);

  List<ILookupRow<LOOKUP_KEY>> getLookupRows();

  ILookupRow<LOOKUP_KEY> getSelectedLookupRow();

  ILookupRow<LOOKUP_KEY> getCheckedLookupRow();

  boolean select(ILookupRow<LOOKUP_KEY> lookupRow);

  boolean select(LOOKUP_KEY key);

}
