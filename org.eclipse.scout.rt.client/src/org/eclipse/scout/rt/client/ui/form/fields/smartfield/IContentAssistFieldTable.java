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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 *
 */
public interface IContentAssistFieldTable<KEY_TYPE> extends ITable {

  void setLookupRows(List<? extends ILookupRow<KEY_TYPE>> lookupRows) throws ProcessingException;

  List<ILookupRow<KEY_TYPE>> getLookupRows();

  ILookupRow<KEY_TYPE> getSelectedLookupRow();

  ILookupRow<KEY_TYPE> getCheckedLookupRow();

  boolean select(ILookupRow<KEY_TYPE> lookupRow) throws ProcessingException;

  boolean select(KEY_TYPE key) throws ProcessingException;

}
