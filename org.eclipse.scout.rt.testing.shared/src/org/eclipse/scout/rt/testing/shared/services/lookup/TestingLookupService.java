/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared.services.lookup;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Lookup service for testing purposes (Implementation of {@link ILookupService}).
 * Rows can be set dynamically with {@link #setRows(LookupRow[])}.
 * 
 * @since 3.9.0
 */
public class TestingLookupService implements ILookupService {
  private LookupRow[] m_rows = new LookupRow[0];

  public TestingLookupService() {
  }

  @Override
  public void initializeService() {
  }

  public LookupRow[] getRows() {
    return m_rows;
  }

  public void setRows(LookupRow[] rows) {
    m_rows = rows;
  }

  @Override
  public LookupRow[] getDataByKey(LookupCall call) throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Object key = call.getKey();
    if (key != null) {
      for (LookupRow row : getRows()) {
        if (key.equals(row.getKey())) {
          list.add(row);
        }
      }
    }
    return list.toArray(new LookupRow[0]);
  }

  @Override
  public LookupRow[] getDataByRec(LookupCall call) throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Object parentKey = call.getRec();
    if (parentKey == null) {
      for (LookupRow row : getRows()) {
        if (row.getParentKey() == null) {
          list.add(row);
        }
      }
    }
    else {
      for (LookupRow row : getRows()) {
        if (row.getParentKey() == parentKey) {
          list.add(row);
        }
      }
    }
    return list.toArray(new LookupRow[0]);
  }

  @Override
  public LookupRow[] getDataByText(LookupCall call) throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Pattern p = createLowerCaseSearchPattern(call.getText());
    for (LookupRow row : getRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list.toArray(new LookupRow[0]);
  }

  @Override
  public LookupRow[] getDataByAll(LookupCall call) throws ProcessingException {
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    Pattern p = createLowerCaseSearchPattern(call.getAll());
    for (LookupRow row : getRows()) {
      if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
        list.add(row);
      }
    }
    return list.toArray(new LookupRow[0]);
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
