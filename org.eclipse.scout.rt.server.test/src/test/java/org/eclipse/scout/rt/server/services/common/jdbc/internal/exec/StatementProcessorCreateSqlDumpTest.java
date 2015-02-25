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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.exec;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.rt.server.services.common.jdbc.ISqlService;
import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.junit.Test;

/**
 * Tests for {@link StatementProcessor#createSqlDump(boolean, boolean)}
 * 
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=371963
 */
public class StatementProcessorCreateSqlDumpTest {

  private static final String NL = "\n";
  private static final String SQL_PLAIN_LINE = "SQL PLAIN Log:" + NL;
  private static final String SQL_WITH_BINDS = "SQL with binds:" + NL;
  private static final String SELECT = "SELECT    ";
  private static final String SELECT_SPACE = "          ";
  private static final String IN = "IN  ";
  private static final String FROM_DUAL_LINE = "FROM      DUAL";

  @Test
  public void testSimpleSelectPlainText() throws Exception {
    runSimpleSelectPlainText(Collections.singletonList("1"), "Select 1 from dual");
    runSimpleSelectPlainText(Collections.singletonList("23"), "Select :myKey from dual");
    runSimpleSelectPlainText(Collections.singletonList("'lorem'"), "Select :myText from dual");

    ArrayList<String> vals1 = new ArrayList<String>();
    vals1.add("1");
    vals1.add("'ipsum'");
    runSimpleSelectPlainText(vals1, "select 1, 'ipsum' from dual");

    ArrayList<String> vals2 = new ArrayList<String>();
    vals2.add("23");
    vals2.add("'lorem'");
    runSimpleSelectPlainText(vals2, "select :myKey, :myText from dual");
  }

  /**
   * This test illustrate the bug 371963.
   * The first selected value '?' is not a place holder for :myText
   */
  @Test
  public void testSelectPlainText() throws Exception {
    ArrayList<String> vals = new ArrayList<String>();
    vals.add("'?'");
    vals.add("'lorem'");
    runSimpleSelectPlainText(vals, "select '?', :myText from dual");
  }

  private void runSimpleSelectPlainText(List<String> expectedValues, String statement) throws ProcessingException {
    String expected = concatValues(SQL_PLAIN_LINE, expectedValues, SELECT, "," + NL + SELECT_SPACE, NL + FROM_DUAL_LINE);
    runDump(expected, StatementType.PLAIN_TEXT, statement);
  }

  @Test
  public void testSimpleSelectWithBinds() throws Exception {
    runSimpleSelectWithBinds(Collections.singletonList("1"), Collections.<String> emptyList(), "Select 1 from dual");
    runSimpleSelectWithBinds(Collections.singletonList(":myKey"), Collections.singletonList(":myKey => ? [INTEGER 23]"), "Select :myKey from dual");
    runSimpleSelectWithBinds(Collections.singletonList(":myText"), Collections.singletonList(":myText => ? [CHAR lorem]"), "Select :myText from dual");

    ArrayList<String> vals1 = new ArrayList<String>();
    vals1.add("1");
    vals1.add("'ipsum'");
    runSimpleSelectWithBinds(vals1, Collections.<String> emptyList(), "select 1, 'ipsum' from dual");

    ArrayList<String> selectValues = new ArrayList<String>();
    selectValues.add(":myKey");
    selectValues.add(":myText");
    ArrayList<String> bindsValues = new ArrayList<String>();
    bindsValues.add(":myKey => ? [INTEGER 23]");
    bindsValues.add(":myText => ? [CHAR lorem]");
    runSimpleSelectWithBinds(selectValues, bindsValues, "select :myKey, :myText from dual");
  }

  private void runSimpleSelectWithBinds(List<String> expectedValues, List<String> expectedBinds, String statement) throws ProcessingException {
    String expected = concatValues(SQL_WITH_BINDS, expectedValues, SELECT, "," + NL + SELECT_SPACE, NL + FROM_DUAL_LINE);
    expected = concatValues(expected, expectedBinds, NL + IN, NL + IN, "");
    runDump(expected, StatementType.WITH_BINDS, statement);
  }

  private String concatValues(String start, List<String> values, String firstPrefix, String otherPrefix, String end) {
    boolean isFirst = true;
    String expected = start;
    for (String v : values) {
      if (isFirst) {
        expected += firstPrefix + v;
        isFirst = false;
      }
      else {
        expected += otherPrefix + v;
      }
    }
    expected += end;
    return expected;
  }

  private void runDump(String expected, StatementType type, String statement) throws ProcessingException {
    ISqlService callerService = EasyMock.createNiceMock(ISqlService.class);

    ISqlStyle style = EasyMock.createNiceMock(ISqlStyle.class);
    style.buildBindFor(23, null);
    EasyMock.expectLastCall().andReturn(new SqlBind(4, 23));
    style.toPlainText(23);
    EasyMock.expectLastCall().andReturn("23");

    style.buildBindFor("lorem", null);
    EasyMock.expectLastCall().andReturn(new SqlBind(1, "lorem"));
    style.toPlainText("lorem");
    EasyMock.expectLastCall().andReturn("'lorem'");

    callerService.getSqlStyle();
    EasyMock.expectLastCall().andReturn(style).anyTimes();

    EasyMock.replay(callerService, style);

    Object[] bindBases = new Object[]{new NVPair("myKey", 23), new NVPair("myText", "lorem")};
    P_StatementProcessor_UnderTest statementProcessor = new P_StatementProcessor_UnderTest(callerService, statement, bindBases);
    String dump = statementProcessor.getDump(type);

    assertEquals(type.name() + " dump", expected, dump);
  }

  public enum StatementType {
    PLAIN_TEXT,
    WITH_BINDS
  }

  public static class P_StatementProcessor_UnderTest extends StatementProcessor {

    public P_StatementProcessor_UnderTest(ISqlService callerService, String stm, Object[] bindBases) throws ProcessingException {
      super(callerService, stm, bindBases);
    }

    public P_StatementProcessor_UnderTest(ISqlService callerService, String stm, Object[] bindBases, int maxRowCount) throws ProcessingException {
      super(callerService, stm, bindBases, maxRowCount);
    }

    public String getDump(StatementType type) {
      switch (type) {
        case PLAIN_TEXT:
          return createSqlDump(false, true);
        case WITH_BINDS:
          return createSqlDump(true, false);
        default:
          throw new IllegalStateException("unexpected StatementType:" + type);
      }
    }
  }
}
