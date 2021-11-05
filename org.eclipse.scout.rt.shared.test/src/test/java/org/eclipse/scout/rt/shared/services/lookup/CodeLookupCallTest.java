/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.services.lookup;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CodeRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
import org.eclipse.scout.rt.shared.services.lookup.fixture.LegacyCodeLookupCall;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link CodeLookupCall}. Test that the implementation of Bug 364312 does not affect the lookup call's
 * behaviour (no side effects).
 */
@RunWith(PlatformTestRunner.class)
public class CodeLookupCallTest {
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
  public void testGetDataByAll() {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    List<ILookupRow<Integer>> oldRows = oldLc.getDataByAll();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    List<ILookupRow<Integer>> newRows = newLc.getDataByAll();

    assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByText() {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    List<ILookupRow<Integer>> oldRows = oldLc.getDataByText();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    List<ILookupRow<Integer>> newRows = newLc.getDataByText();

    assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByTextFiltered() {
    runGetDataByTextFiltered(null);
    runGetDataByTextFiltered("*or*");
    runGetDataByTextFiltered("ip");
    runGetDataByTextFiltered("foo");
    runGetDataByTextFiltered("ipi");
    runGetDataByTextFiltered("*");
    runGetDataByTextFiltered("");
  }

  private void runGetDataByTextFiltered(String text) {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setText(text);
    List<ILookupRow<Integer>> oldRows = oldLc.getDataByText();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setText(text);
    List<ILookupRow<Integer>> newRows = newLc.getDataByText();

    assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByKey() {
    runGetDataByKey(null);
    runGetDataByKey(ROW10_KEY);
    runGetDataByKey(ROW31_KEY);
    runGetDataByKey(Integer.valueOf(0));
  }

  private void runGetDataByKey(Integer key) {

    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setKey(key);
    List<ILookupRow<Integer>> oldRows = oldLc.getDataByKey();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setKey(key);
    List<ILookupRow<Integer>> newRows = newLc.getDataByKey();

    assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByRec() {
    runGetDataByRec(null);
    runGetDataByRec(ROW10_KEY);
    runGetDataByRec(ROW20_KEY);
    runGetDataByRec(ROW30_KEY);
    runGetDataByRec(ROW11_KEY);
  }

  private void runGetDataByRec(Integer parent) {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setRec(parent);
    List<ILookupRow<Integer>> oldRows = oldLc.getDataByRec();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setRec(parent);
    List<ILookupRow<Integer>> newRows = newLc.getDataByRec();

    assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByAllWithMaxRowCount() {
    int numSkippedRows = 3;
    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    List<ILookupRow<Integer>> allRows = newLc.getDataByAll();
    assertTrue(allRows.size() > numSkippedRows);

    newLc.setMaxRowCount(allRows.size() - numSkippedRows);
    List<ILookupRow<Integer>> firstRows = newLc.getDataByAll();
    assertEquals(allRows.size() - numSkippedRows + 1 /* if max row count=3, actually return 4 rows so that the UI recognizes that there are more rows */, firstRows.size());
  }

  @Test
  public void testGetFilteredData() {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setFilter((call, code, treeLevel) -> true);
    List<ILookupRow<Integer>> oldRows = oldLc.getDataByAll();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setFilter((call, code, treeLevel) -> true);
    List<ILookupRow<Integer>> newRows = newLc.getDataByAll();

    assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  private static boolean equals(List<? extends ILookupRow<?>> rows1, List<? extends ILookupRow<?>> rows2) {
    if (rows1 == null && rows2 == null) {
      return true;
    }
    if (rows1 == null || rows2 == null) {
      return false;
    }
    if (rows1.size() != rows2.size()) {
      return false;
    }
    Iterator<? extends ILookupRow<?>> it1 = rows1.iterator();
    Iterator<? extends ILookupRow<?>> it2 = rows2.iterator();
    while (it1.hasNext() && it2.hasNext()) {
      if (!equals(it1.next(), it2.next())) {
        return false;
      }
    }
    return it1.hasNext() == it2.hasNext();
  }

  private static boolean equals(ILookupRow<?> row1, ILookupRow<?> row2) {
    return ObjectUtility.equals(row1.getBackgroundColor(), row2.getBackgroundColor()) && ObjectUtility.equals(row1.getFont(), row2.getFont()) && ObjectUtility.equals(row1.getForegroundColor(), row2.getForegroundColor())
        && ObjectUtility.equals(row1.getIconId(), row2.getIconId()) && ObjectUtility.equals(row1.getKey(), row2.getKey()) && ObjectUtility.equals(row1.getParentKey(), row2.getParentKey())
        && ObjectUtility.equals(row1.getText(), row2.getText()) && ObjectUtility.equals(row1.getTooltipText(), row2.getTooltipText());
  }

  private class P_NewCodeLookupCall extends CodeLookupCall<Integer> {
    public P_NewCodeLookupCall() {
      super(CodeLookupCallTestCodeType.class);
    }

    private static final long serialVersionUID = 1L;
  }

  private class P_LegacyCodeLookupCall extends LegacyCodeLookupCall<Integer> {
    public P_LegacyCodeLookupCall() {
      super(CodeLookupCallTestCodeType.class);
    }

    private static final long serialVersionUID = 1L;
  }

  public static class CodeLookupCallTestCodeType extends AbstractCodeType<Long, Integer> {
    private static final long serialVersionUID = 1L;
    public static final Long ID = Long.valueOf(22);

    public static final String ICON = "configuredIcon";
    public static final String TOOLTIP = "configuredTooltip";
    public static final String BACKGROUND_COLOR = "configuredBackgroundColor";
    public static final String FOREGROUND_COLOR = "configuredForegroundColor";
    public static final String FONT = "null-ITALIC-0";
    public static final String CSS_CLASS = "configuredCssClass";
    public static final boolean ENABLED = false;
    public static final boolean ACTIVE = false;
    public static final String EXT_KEY = "configuredExtKey";
    public static final double VALUE = 42d;

    @Override
    public Long getId() {
      return ID;
    }

    @Override
    protected List<ICodeRow<Integer>> execLoadCodes(Class<? extends ICodeRow<Integer>> codeRowType) {
      List<ICodeRow<Integer>> result = new ArrayList<>();
      result.add(createTestCodeRow(ROW10_KEY, null, ROW10_TEXT));
      result.add(createTestCodeRow(ROW11_KEY, ROW10_KEY, ROW11_TEXT));
      result.add(createTestCodeRow(ROW12_KEY, ROW10_KEY, ROW12_TEXT));
      result.add(createTestCodeRow(ROW20_KEY, null, ROW20_TEXT));
      result.add(createTestCodeRow(ROW30_KEY, null, ROW30_TEXT));
      result.add(createTestCodeRow(ROW31_KEY, ROW30_KEY, ROW31_TEXT));
      return result;
    }
  }

  private static CodeRow<Integer> createTestCodeRow(Integer key, Integer parentKey, String text) {
    return new CodeRow<>(key,
        text,
        CodeLookupCallTestCodeType.ICON,
        CodeLookupCallTestCodeType.TOOLTIP,
        CodeLookupCallTestCodeType.BACKGROUND_COLOR,
        CodeLookupCallTestCodeType.FOREGROUND_COLOR,
        FontSpec.parse(CodeLookupCallTestCodeType.FONT),
        CodeLookupCallTestCodeType.CSS_CLASS,
        CodeLookupCallTestCodeType.ENABLED,
        parentKey,
        CodeLookupCallTestCodeType.ACTIVE,
        CodeLookupCallTestCodeType.EXT_KEY,
        CodeLookupCallTestCodeType.VALUE,
        0);
  }
}
