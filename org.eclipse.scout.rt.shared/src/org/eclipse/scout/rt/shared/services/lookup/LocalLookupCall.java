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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * LookupCall for cases where no backend service exists.<br>
 * Data is directly provided by {@link #execCreateLookupRows()}
 * <p>
 * Does not implements serializable, since this special subclass is not intended to be exchanged between gui and server.
 * 
 * @see LookupCall
 */
public class LocalLookupCall extends LookupCall {
  private static final long serialVersionUID = 0L;

  public LocalLookupCall() {
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @ConfigOperation
  @Order(30)
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    return null;
  }

  /**
   * @param humanReadbleFilterPattern
   *          is not a regex and may contain *,%,? as wildcards for searching
   *          override this method for custom filter pattern creation
   */
  protected Pattern createSearchPattern(String humanReadbleFilterPattern) {
    return createLowerCaseSearchPattern(humanReadbleFilterPattern);
  }

  /**
   * alias for {@link StringUtility#toRegEx(String, int)}
   */
  public static Pattern createLowerCaseSearchPattern(String s) {
    return StringUtility.toRegEx(s, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  }

  @Override
  protected final Class<? extends ILookupService> getConfiguredService() {
    return null;
  }

  /**
   * Complete override using local data
   */
  @Override
  public LookupRow[] getDataByKey() throws ProcessingException {
    if (getKey() == null) {
      return LookupRow.EMPTY_ARRAY;
    }
    Object key = getKey();
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    for (LookupRow row : execCreateLookupRows()) {
      if (key.equals(row.getKey())) {
        list.add(row);
      }
    }
    return list.toArray(new LookupRow[list.size()]);
  }

  /**
   * Complete override using local data
   */
  @Override
  public LookupRow[] getDataByText() throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Pattern p = createSearchPattern(getText());
    for (LookupRow row : execCreateLookupRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list.toArray(new LookupRow[list.size()]);
  }

  /**
   * Complete override using local data
   */
  @Override
  public LookupRow[] getDataByAll() throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Pattern p = createSearchPattern(getAll());
    for (LookupRow row : execCreateLookupRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list.toArray(new LookupRow[list.size()]);
  }

  /**
   * Complete override using local data
   */
  @Override
  public LookupRow[] getDataByRec() throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Object parentKey = getRec();
    if (parentKey == null) {
      for (LookupRow row : execCreateLookupRows()) {
        if (row.getParentKey() == null) {
          list.add(row);
        }
      }
    }
    else {
      for (LookupRow row : execCreateLookupRows()) {
        if (parentKey.equals(row.getParentKey())) {
          list.add(row);
        }
      }
    }
    return list.toArray(new LookupRow[list.size()]);
  }

}
