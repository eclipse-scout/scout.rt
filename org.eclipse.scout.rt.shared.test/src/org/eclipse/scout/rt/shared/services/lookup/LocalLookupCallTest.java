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
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

/**
 * JUnit test for {@link LocalLookupCall}
 */
public class LocalLookupCallTest {
  private static final Integer ROW10_KEY = Integer.valueOf(10);
  private static final String ROW10_TEXT = "lorem";
  private static final Integer ROW11_KEY = Integer.valueOf(21);
  private static final String ROW11_TEXT = "for";
  private static final Integer ROW12_KEY = Integer.valueOf(22);
  private static final String ROW12_TEXT = "ipi";
  private static final Integer ROW20_KEY = Integer.valueOf(12);
  private static final String ROW20_TEXT = "ipsum";
  private static final Integer ROW30_KEY = Integer.valueOf(13);
  private static final String ROW30_TEXT = "dolor";
  private static final Integer ROW31_KEY = Integer.valueOf(43);
  private static final String ROW31_TEXT = "mor";

  @Test
  public void testGetDataByAll() throws Exception {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    LookupRow[] rows = lc.getDataByAll();

    assertEquals("rows lengh", 6, rows.length);
  }

  @Test
  public void testGetDataByText() throws Exception {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    LookupRow[] rows = lc.getDataByText();

    assertEquals("rows length", 6, rows.length);
  }

  @Test
  public void testGetDataByTextFiltered() throws Exception {
    runGetDataByTextFiltered(6, null);
    runGetDataByTextFiltered(4, "*or*");
    runGetDataByTextFiltered(3, "*or");
    runGetDataByTextFiltered(2, "ip");
    runGetDataByTextFiltered(0, "foo");
    runGetDataByTextFiltered(1, "ipi");
    runGetDataByTextFiltered(6, "*");
    runGetDataByTextFiltered(6, "");
  }

  private void runGetDataByTextFiltered(int expectedLength, String text) throws ProcessingException {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setText(text);
    LookupRow[] rows = lc.getDataByText();

    assertEquals("rows length", expectedLength, rows.length);
  }

  @Test
  public void testGetDataByKey() throws Exception {
    runGetDataByKey(0, null);
    runGetDataByKey(1, ROW10_KEY);
    runGetDataByKey(1, ROW31_KEY);
    runGetDataByKey(0, "x");
    runGetDataByKey(0, Integer.valueOf(0));
  }

  private void runGetDataByKey(int expectedLength, Object key) throws ProcessingException {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setKey(key);
    LookupRow[] rows = lc.getDataByKey();

    assertEquals("rows length", expectedLength, rows.length);
  }

  @Test
  public void testGetDataByRec() throws Exception {
    runGetDataByRec(3, null);
    runGetDataByRec(2, ROW10_KEY);
    runGetDataByRec(0, ROW20_KEY);
    runGetDataByRec(1, ROW30_KEY);
    runGetDataByRec(0, ROW11_KEY);
    runGetDataByRec(0, "x");

  }

  private void runGetDataByRec(int expectedLength, Object parent) throws ProcessingException {
    P_LocalLookupCall lc = new P_LocalLookupCall();
    lc.setRec(parent);
    LookupRow[] rows = lc.getDataByRec();

    assertEquals("rows length", expectedLength, rows.length);
  }

  private class P_LocalLookupCall extends LocalLookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
      List<LookupRow> list = new ArrayList<LookupRow>();
      list.add(new LookupRow(ROW10_KEY, ROW10_TEXT));
      list.add(new LookupRow(ROW20_KEY, ROW20_TEXT));
      list.add(new LookupRow(ROW30_KEY, ROW30_TEXT));
      list.add(new LookupRow(ROW11_KEY, ROW11_TEXT, null, null, null, null, null, true, ROW10_KEY));
      list.add(new LookupRow(ROW12_KEY, ROW12_TEXT, null, null, null, null, null, true, ROW10_KEY));
      list.add(new LookupRow(ROW31_KEY, ROW31_TEXT, null, null, null, null, null, true, ROW30_KEY));
      return list;
    }
  }
}
