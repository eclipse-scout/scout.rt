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

import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.Activator;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CodeRow;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.lookup.fixture.ILegacyCodeLookupCallVisitor;
import org.eclipse.scout.rt.shared.services.lookup.fixture.LegacyCodeLookupCall;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.code.TestingCodeService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

/**
 * JUnit test for {@link CodeLookupCall}.
 * Test that the implementation of Bug 364312 does not affect the lookup call's behaviour (no side effects).
 */
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

  private static List<ServiceRegistration> s_services;

  @BeforeClass
  public static void beforeClass() throws Exception {
    s_services = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, new TestingCodeService(new CodeLookupCallTestCodeType()));
  }

  @AfterClass
  public static void afterClass() throws Exception {
    TestingUtility.unregisterServices(s_services);
  }

  @Test
  public void testGetDataByAll() throws Exception {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    LookupRow[] oldRows = oldLc.getDataByAll();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    LookupRow[] newRows = newLc.getDataByAll();

    Assert.assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByText() throws Exception {
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    LookupRow[] oldRows = oldLc.getDataByText();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    LookupRow[] newRows = newLc.getDataByText();

    Assert.assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetDataByTextFiltered() throws Exception {
    runGetDataByTextFiltered(null);
    runGetDataByTextFiltered("*or*");
    runGetDataByTextFiltered("*or");
    runGetDataByTextFiltered("ip");
    runGetDataByTextFiltered("foo");
    runGetDataByTextFiltered("ipi");
    runGetDataByTextFiltered("*");
    runGetDataByTextFiltered("");
  }

  private void runGetDataByTextFiltered(String text) throws ProcessingException {

    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setText(text);
    LookupRow[] oldRows = oldLc.getDataByText();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setText(text);
    LookupRow[] newRows = newLc.getDataByText();

    Assert.assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
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

    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setKey(key);
    LookupRow[] oldRows = oldLc.getDataByKey();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setKey(key);
    LookupRow[] newRows = newLc.getDataByKey();

    Assert.assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
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
    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setRec(parent);
    LookupRow[] oldRows = oldLc.getDataByRec();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setRec(parent);
    LookupRow[] newRows = newLc.getDataByRec();

    Assert.assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  @Test
  public void testGetFilteredData() throws Exception {

    P_LegacyCodeLookupCall oldLc = new P_LegacyCodeLookupCall();
    oldLc.setFilter(new ILegacyCodeLookupCallVisitor() {
      @Override
      public boolean visit(LegacyCodeLookupCall call, ICode code, int treeLevel) {
        return true;
      }
    });
    LookupRow[] oldRows = oldLc.getDataByAll();

    P_NewCodeLookupCall newLc = new P_NewCodeLookupCall();
    newLc.setFilter(new ICodeLookupCallVisitor() {

      @Override
      public boolean visit(CodeLookupCall call, ICode code, int treeLevel) {
        return true;
      }
    });
    LookupRow[] newRows = newLc.getDataByAll();

    Assert.assertTrue("identical rows for old and new lookup call", equals(oldRows, newRows));
  }

  private static boolean equals(LookupRow[] rows1, LookupRow[] rows2) {
    if ((rows1 == null && rows2 != null) || (rows1 != null && rows2 == null)) {
      return false;
    }

    if (rows1 != null) {
      if (rows1.length != rows2.length) {
        return false;
      }
      for (int i = 0; i < rows1.length; i++) {
        if (!equals(rows1[i], rows2[i])) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean equals(LookupRow row1, LookupRow row2) {
    return CompareUtility.equals(row1.getBackgroundColor(), row2.getBackgroundColor()) && CompareUtility.equals(row1.getFont(), row2.getFont()) && CompareUtility.equals(row1.getForegroundColor(), row2.getForegroundColor()) && CompareUtility.equals(row1.getIconId(), row2.getIconId()) && CompareUtility.equals(row1.getKey(), row2.getKey()) && CompareUtility.equals(row1.getParentKey(), row2.getParentKey()) && CompareUtility.equals(row1.getText(), row2.getText()) && CompareUtility.equals(row1.getTooltipText(), row2.getTooltipText());
  }

  private class P_NewCodeLookupCall extends CodeLookupCall {
    public P_NewCodeLookupCall() {
      super(CodeLookupCallTestCodeType.class);
    }

    private static final long serialVersionUID = 1L;
  }

  private class P_LegacyCodeLookupCall extends LegacyCodeLookupCall {
    public P_LegacyCodeLookupCall() {
      super(CodeLookupCallTestCodeType.class);
    }

    private static final long serialVersionUID = 1L;
  }

  private static class CodeLookupCallTestCodeType extends AbstractCodeType<String> {
    private static final long serialVersionUID = 1L;
    public static final String ID = "CodeLookupCallTestCodeId";

    public static final String ICON = "configuredIcon";
    public static final String TOOLTIP = "configuredTooltip";
    public static final String BACKGROUND_COLOR = "configuredBackgroundColor";
    public static final String FOREGROUND_COLOR = "configuredForegroundColor";
    public static final String FONT = "null-ITALIC-0";
    public static final boolean ENABLED = false;
    public static final boolean ACTIVE = false;
    public static final String EXT_KEY = "configuredExtKey";
    public static final double VALUE = 42d;

    @Override
    public String getId() {
      return ID;
    }

    @Override
    protected CodeRow[] execLoadCodes() throws ProcessingException {
      CodeRow[] result = new CodeRow[]{
          createTestCodeRow(ROW10_KEY, null, ROW10_TEXT),
          createTestCodeRow(ROW11_KEY, ROW10_KEY, ROW11_TEXT),
          createTestCodeRow(ROW12_KEY, ROW10_KEY, ROW12_TEXT),
          createTestCodeRow(ROW20_KEY, null, ROW20_TEXT),
          createTestCodeRow(ROW30_KEY, null, ROW30_TEXT),
          createTestCodeRow(ROW31_KEY, ROW30_KEY, ROW31_TEXT)
      };
      return result;
    }
  }

  private static CodeRow createTestCodeRow(Integer key, Integer parentKey, String text) {
    return new CodeRow(key,
        text,
        CodeLookupCallTestCodeType.ICON,
        CodeLookupCallTestCodeType.TOOLTIP,
        CodeLookupCallTestCodeType.BACKGROUND_COLOR,
        CodeLookupCallTestCodeType.FOREGROUND_COLOR,
        FontSpec.parse(CodeLookupCallTestCodeType.FONT),
        CodeLookupCallTestCodeType.ENABLED,
        parentKey,
        CodeLookupCallTestCodeType.ACTIVE,
        CodeLookupCallTestCodeType.EXT_KEY,
        CodeLookupCallTestCodeType.VALUE,
        0);
  }
}
