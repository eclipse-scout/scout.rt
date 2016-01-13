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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * LookupCall for cases where no backend service exists.<br>
 * Data is directly provided by {@link #execCreateLookupRows()}
 * <p>
 * Does not implements serializable, since this special subclass is not intended to be exchanged between gui and server.
 *
 * @see LookupCall
 */
public class LocalLookupCall<T> extends LookupCall<T> {
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
  protected List<? extends ILookupRow<T>> execCreateLookupRows() {
    return null;
  }

  protected Pattern createSearchPattern(String s) {
    if (s == null) {
      s = "";
    }
    s = s.replace(getWildcard(), "@wildcard@");
    s = s.toLowerCase();
    s = StringUtility.escapeRegexMetachars(s);
    s = s.replace("@wildcard@", ".*");
    if (!s.contains(".*")) {
      s = s + ".*";
    }
    return Pattern.compile(s, Pattern.DOTALL);
  }

  /**
   * @deprecated: Will be removed in the neon release.
   */
  @Deprecated
  public static Pattern createLowerCaseSearchPattern(String s) {
    return StringUtility.toRegEx(s, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  }

  @Override
  protected final Class<? extends ILookupService<T>> getConfiguredService() {
    return null;
  }

  /**
   * Complete override using local data
   */
  @Override
  public List<? extends ILookupRow<T>> getDataByKey() {
    if (getKey() == null) {
      return CollectionUtility.emptyArrayList();
    }
    Object key = getKey();
    List<? extends ILookupRow<T>> rows = execCreateLookupRows();
    if (rows == null) {
      return CollectionUtility.emptyArrayList();
    }
    ArrayList<ILookupRow<T>> list = new ArrayList<ILookupRow<T>>(rows.size());
    for (ILookupRow<T> row : rows) {
      if (key.equals(row.getKey())) {
        list.add(row);
      }
    }
    return list;
  }

  /**
   * Complete override using local data
   */
  @Override
  public List<? extends ILookupRow<T>> getDataByText() {
    List<ILookupRow<T>> list = new ArrayList<ILookupRow<T>>();
    Pattern p = createSearchPattern(getText());
    for (ILookupRow<T> row : execCreateLookupRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list;
  }

  /**
   * Complete override using local data
   */
  @Override
  public List<? extends ILookupRow<T>> getDataByAll() {
    List<ILookupRow<T>> list = new ArrayList<ILookupRow<T>>();
    Pattern p = createSearchPattern(getAll());
    for (ILookupRow<T> row : execCreateLookupRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list;
  }

  /**
   * Complete override using local data
   */
  @Override
  public List<? extends ILookupRow<T>> getDataByRec() {
    List<ILookupRow<T>> list = new ArrayList<ILookupRow<T>>();
    Object parentKey = getRec();
    if (parentKey == null) {
      for (ILookupRow<T> row : execCreateLookupRows()) {
        if (row.getParentKey() == null) {
          list.add(row);
        }
      }
    }
    else {
      for (ILookupRow<T> row : execCreateLookupRows()) {
        if (parentKey.equals(row.getParentKey())) {
          list.add(row);
        }
      }
    }
    return list;
  }
}
