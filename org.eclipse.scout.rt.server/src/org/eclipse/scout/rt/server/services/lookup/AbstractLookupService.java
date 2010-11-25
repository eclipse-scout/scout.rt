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
package org.eclipse.scout.rt.server.services.lookup;

import java.util.ArrayList;

import org.eclipse.scout.commons.MatrixUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.AbstractService;

/**
 * <h4>AbstractLookupService</h4>
 */
public abstract class AbstractLookupService extends AbstractService implements ILookupService {

  /**
   * Convenience function to sort data for later call to {@link #createLookupRowArray(Object[][], LookupCall)}.<br>
   * The sort indices are 0-based.
   */
  public static void sortData(Object[][] data, int... sortColumns) {
    MatrixUtility.sort(data, sortColumns);
  }

  /**
   * @see #createLookupRowArray(Object[][], int, LookupCall)
   */
  public static LookupRow[] createLookupRowArray(Object[][] data, LookupCall call) throws ProcessingException {
    return createLookupRowArray(data, data != null && data.length > 0 ? data[0].length : 0, call);
  }

  /**
   * Convenience function to transform Object[][] data into LookupRow[]
   * 
   * @param maxColumnIndex
   *          the maximum column index to be used to create the code rows, all
   *          column indexes >= columnCount are ignored
   *          <p>
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
   *          <li>Boolean active (0,1) see {@link TriState#parseTriState(Object)}
   *          </ul>
   */
  public static LookupRow[] createLookupRowArray(Object[][] data, int maxColumnIndex, LookupCall call) throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>(data.length);
    Boolean active = call.getActive().getBooleanValue();
    for (int i = 0; i < data.length; i++) {
      LookupRow row = new LookupRow(data[i], maxColumnIndex);
      // check active flag
      if (active == null || active.booleanValue() == row.isActive()) {
        list.add(row);
      }
    }
    return list.toArray(new LookupRow[list.size()]);
  }

}
