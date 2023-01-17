/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.lookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.MatrixUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * <h4>AbstractLookupService</h4>
 */
@SuppressWarnings("squid:S1118")
public abstract class AbstractLookupService<LOOKUP_ROW_KEY_TYPE> implements ILookupService<LOOKUP_ROW_KEY_TYPE> {

  /**
   * Convenience function to sort data for later call to {@link #createLookupRowArray(Object[][], LookupCall, Class)}.
   * <br>
   * The sort indices are 0-based.
   */
  public static void sortData(Object[][] data, int... sortColumns) {
    MatrixUtility.sort(data, sortColumns);
  }

  /**
   * @see #createLookupRowArray(Object[][], int, ILookupCall, Class)
   */
  public static <KEY_TYPE> List<ILookupRow<KEY_TYPE>> createLookupRowArray(Object[][] data, ILookupCall<KEY_TYPE> call, Class<?> keyClass) {
    int maxColumnIndex = data.length > 0 ? data[0].length - 1 : 0;
    return createLookupRowArray(data, maxColumnIndex, call, keyClass);
  }

  /**
   * Convenience function to transform Object[][] data into LookupRow[]
   *
   * @param maxColumnIndex
   *          the maximum column index to be used to create the code rows, all column indexes >= columnCount are ignored
   * @param data
   *          The Object[][] must contain rows with the elements in the following order:
   *          <ul>
   *          <li>Object key
   *          <li>String text
   *          <li>String iconId
   *          <li>String tooltip
   *          <li>String background color
   *          <li>String foreground color
   *          <li>String font
   *          <li>Boolean enabled
   *          <li>Object parentKey used in hierarchical structures to point to the parents primary key
   *          <li>Boolean active (0,1) see {@link TriState#parse(Object)}
   *          </ul>
   * @param call
   * @param keyClass
   *          Class describing the type of the key column in the data. Usually corresponds to the LOOKUP_ROW_KEY_TYPE
   *          type parameter of the lookup service.
   */
  public static <KEY_TYPE> List<ILookupRow<KEY_TYPE>> createLookupRowArray(Object[][] data, int maxColumnIndex, ILookupCall<KEY_TYPE> call, Class<?> keyClass) {
    List<ILookupRow<KEY_TYPE>> list = new ArrayList<>(data.length);
    Boolean active = call.getActive().getBooleanValue();
    for (Object[] aData : data) {
      LookupRow<KEY_TYPE> row = new LookupRow<>(aData, maxColumnIndex, keyClass);
      // check active flag
      if (active == null || active.booleanValue() == row.isActive()) {
        list.add(row);
      }
    }
    return list;
  }
}
