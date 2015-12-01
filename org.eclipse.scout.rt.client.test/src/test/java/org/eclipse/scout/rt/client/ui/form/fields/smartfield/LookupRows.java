/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

class LookupRows {

  static final LookupRow<Long> ROW_1 = new LookupRow<Long>(1L, "aName");

  static final LookupRow<Long> ROW_2 = new LookupRow<Long>(2L, "bName1");

  static final LookupRow<Long> ROW_3 = new LookupRow<Long>(3L, "bName2");

  static final LookupRow<Long> ROW_MULTI_LINE = new LookupRow<Long>(4L, "Line1\nLine2");

  static final List<LookupRow<Long>> lookupRows = Arrays.asList(ROW_1, ROW_2, ROW_3, ROW_MULTI_LINE);

  static final List<? extends ILookupRow<Long>> allRows() {
    return lookupRows;
  }

  static final List<? extends ILookupRow<Long>> getRowsByText(String searchText) {
    List<LookupRow<Long>> results = new ArrayList<LookupRow<Long>>();
    searchText = searchText.replace("*", ".*");
    for (LookupRow<Long> lookupRow : lookupRows) {
      if (lookupRow.getText().toLowerCase().matches(searchText)) {
        results.add(lookupRow);
      }
    }
    return results;
  }

  static final List<? extends ILookupRow<Long>> getRowsByKey(Long key) {
    List<LookupRow<Long>> results = new ArrayList<LookupRow<Long>>();
    for (LookupRow<Long> lookupRow : lookupRows) {
      if (lookupRow.getKey().equals(key)) {
        results.add(lookupRow);
      }
    }
    return results;
  }

  /**
   * Returns a single multi line row.
   */
  static final List<? extends ILookupRow<Long>> multiLineRow() {
    return CollectionUtility.arrayList(ROW_MULTI_LINE);
  }

  /**
   * Returns a single row.
   */
  static final List<? extends ILookupRow<Long>> firstRow() {
    return CollectionUtility.arrayList(ROW_1);
  }

}
