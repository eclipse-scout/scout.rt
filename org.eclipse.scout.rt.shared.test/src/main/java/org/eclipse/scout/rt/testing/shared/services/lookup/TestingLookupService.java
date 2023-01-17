/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.shared.services.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Lookup service for testing purposes (Implementation of {@link ILookupService}). Rows can be set dynamically with
 * {@link #setRows(LookupRow[])}.
 *
 * @since 3.9.0
 */
public class TestingLookupService implements ILookupService<Long> {
  private List<ILookupRow<Long>> m_rows = new ArrayList<>();

  public List<ILookupRow<Long>> getRows() {
    return CollectionUtility.arrayList(m_rows);
  }

  public void setRows(List<ILookupRow<Long>> rows) {
    m_rows = CollectionUtility.arrayList(rows);
  }

  @Override
  public List<ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) {
    List<ILookupRow<Long>> list = new ArrayList<>();
    Object key = call.getKey();
    if (key != null) {
      for (ILookupRow<Long> row : getRows()) {
        if (key.equals(row.getKey())) {
          list.add(row);
        }
      }
    }
    return list;
  }

  @Override
  public List<ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) {
    List<ILookupRow<Long>> list = new ArrayList<>();
    Object parentKey = call.getRec();
    if (parentKey == null) {
      for (ILookupRow<Long> row : getRows()) {
        if (row.getParentKey() == null) {
          list.add(row);
        }
      }
    }
    else {
      for (ILookupRow<Long> row : getRows()) {
        if (row.getParentKey() == parentKey) {
          list.add(row);
        }
      }
    }
    return list;
  }

  @Override
  public List<ILookupRow<Long>> getDataByText(ILookupCall<Long> call) {
    List<ILookupRow<Long>> list = new ArrayList<>();
    Pattern p = createLowerCaseSearchPattern(call.getText());
    for (ILookupRow<Long> row : getRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list;
  }

  @Override
  public List<ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) {
    List<ILookupRow<Long>> list = new ArrayList<>();
    Pattern p = createLowerCaseSearchPattern(call.getAll());
    for (ILookupRow<Long> row : getRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list;
  }

  public static Pattern createLowerCaseSearchPattern(String s) {
    if (s == null) {
      s = "";
    }
    s = s.toLowerCase();
    if (s.indexOf('*') < 0) {
      s = s + "*";
    }
    return Pattern.compile(StringUtility.toRegExPattern(s), Pattern.DOTALL);
  }

}
