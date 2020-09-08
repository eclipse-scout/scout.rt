/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;
import org.eclipse.scout.rt.dataobject.lookup.LookupResponse;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.client.services.lookup.AbstractRestLookupCall;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.client.services.lookup.FixtureUuIdLookupRestrictionDo;
import org.eclipse.scout.rt.client.services.lookup.FixtureUuIdLookupRowDo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractRestLookupSmartColumnTest.P_Table.Column;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractRestLookupSmartColumnTest {

  public static final FixtureUuId TEST_FIXTURE_ID_1 = FixtureUuId.of("bc47c139-994f-4c9f-a1a0-0489fdc10ee7");
  public static final FixtureUuId TEST_FIXTURE_ID_2 = FixtureUuId.of("779b73b4-6c49-4aa7-b7f5-ad06f6ecfa6b");
  public static final FixtureUuId TEST_FIXTURE_ID_3 = FixtureUuId.of("032d2759-1164-415f-af19-fabd100b4229");
  public static final FixtureUuId TEST_FIXTURE_ID_4 = FixtureUuId.of("19dc4190-0e0f-4d6f-bf73-92f7656dde24");

  private static final List<IBean<?>> TEST_BEANS = new ArrayList<>();
  private static final P_RemoteService REMOTE_SERVICE = new P_RemoteService();
  private static final Set<P_FixtureUuIdLookupCall> USED_LOOKUP_CALL_INSTANCES = new HashSet<>();

  private P_Table m_table;

  @BeforeClass
  public static void setUpClass() {
    TEST_BEANS.add(BeanTestingHelper.get().registerBean(new BeanMetaData(P_FixtureUuIdLookupCall.class)));
  }

  @AfterClass
  public static void tearDownClass() {
    BeanTestingHelper.get().unregisterBeans(TEST_BEANS);
  }

  @Before
  public void setUp() {
    REMOTE_SERVICE.resetCalls();
    USED_LOOKUP_CALL_INSTANCES.clear();
    m_table = new P_Table();
  }

  @Test
  public void testCallRemoteServiceOnlyOnce() {
    m_table.addRowsByArray(new FixtureUuId[]{TEST_FIXTURE_ID_3, TEST_FIXTURE_ID_1});

    assertEquals(2, m_table.getRowCount());
    assertEquals("Test-Fixture #3", m_table.getColumn().getDisplayText(m_table.getRow(0)));
    assertEquals("Test-Fixture #1", m_table.getColumn().getDisplayText(m_table.getRow(1)));
    assertEquals(1, REMOTE_SERVICE.getTotalCalls());
  }

  @Test
  public void testEditCell() {
    m_table.addRowsByArray(new FixtureUuId[]{null, null});
    assertEquals(2, m_table.getRowCount());
    assertNull(m_table.getColumn().getDisplayText(m_table.getRow(0)));
    assertNull(m_table.getColumn().getDisplayText(m_table.getRow(1)));
    assertEquals(0, REMOTE_SERVICE.getTotalCalls());
    assertEquals(0, USED_LOOKUP_CALL_INSTANCES.size());

    parseAndSetValue(0, "Test-Fixture #4");
    parseAndSetValue(1, "Test-Fixture #2");

    assertEquals(2, m_table.getRowCount());
    // The following tests fail when the REST lookup call accumulates keys.
    // When calling setKey(), the previous key should be cleared.
    assertEquals("Test-Fixture #4", m_table.getColumn().getDisplayText(m_table.getRow(0)));
    assertEquals("Test-Fixture #2", m_table.getColumn().getDisplayText(m_table.getRow(1)));
    assertEquals(6, REMOTE_SERVICE.getTotalCalls());
    assertEquals(4, REMOTE_SERVICE.getCallsByKey());
    assertEquals(2, REMOTE_SERVICE.getCallsByText());
    // The following test fails when the batch lookup did not create a clone of the prototype lookup call.
    assertEquals(6, USED_LOOKUP_CALL_INSTANCES.size());
  }

  private void parseAndSetValue(int rowIndex, String text) {
    ITableRow row = m_table.getRow(rowIndex);
    Column column = m_table.getColumn();

    ISmartField<?> cellEditor = (ISmartField<?>) column.prepareEdit(row);
    cellEditor.parseAndSetValue(text);
    column.completeEdit(row, cellEditor);
  }

  static class P_Table extends AbstractTable {

    public Column getColumn() {
      return getColumnSet().getColumnByClass(Column.class);
    }

    public static class Column extends AbstractRestLookupSmartColumn<FixtureUuId> {

      @Override
      protected Class<? extends AbstractRestLookupCall<?, FixtureUuId>> getConfiguredLookupCall() {
        return P_FixtureUuIdLookupCall.class;
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }
    }
  }

  @IgnoreBean
  static class P_FixtureUuIdLookupCall extends AbstractRestLookupCall<FixtureUuIdLookupRestrictionDo, FixtureUuId> {
    private static final long serialVersionUID = 1L;

    @Override
    protected Function<FixtureUuIdLookupRestrictionDo, LookupResponse<? extends AbstractLookupRowDo<?, FixtureUuId>>> remoteCall() {
      USED_LOOKUP_CALL_INSTANCES.add(this); // cloning an object does not necessarily call the constructor
      return REMOTE_SERVICE::lookupRows;
    }
  }

  static class P_RemoteService {

    private int m_callsByKey = 0;
    private int m_callsByText = 0;
    private int m_callsByAll = 0;
    private final List<FixtureUuIdLookupRowDo> m_allRows;

    public P_RemoteService() {
      m_allRows = Arrays.asList(
          BEANS.get(FixtureUuIdLookupRowDo.class).withId(TEST_FIXTURE_ID_1).withText("Test-Fixture #1"),
          BEANS.get(FixtureUuIdLookupRowDo.class).withId(TEST_FIXTURE_ID_2).withText("Test-Fixture #2"),
          BEANS.get(FixtureUuIdLookupRowDo.class).withId(TEST_FIXTURE_ID_3).withText("Test-Fixture #3"),
          BEANS.get(FixtureUuIdLookupRowDo.class).withId(TEST_FIXTURE_ID_4).withText("Test-Fixture #4"));
    }

    public LookupResponse<FixtureUuIdLookupRowDo> lookupRows(FixtureUuIdLookupRestrictionDo restriction) {
      if (restriction.getIds().size() > 0) {
        m_callsByKey++;
        return LookupResponse.create(m_allRows.stream()
            .filter(row -> restriction.getIds().contains(row.getId()))
            .collect(Collectors.toList()));
      }
      else if (StringUtility.hasText(restriction.getText())) {
        m_callsByText++;
        final String filterText = restriction.getText().replaceAll("\\*", "").toLowerCase();
        return LookupResponse.create(m_allRows.stream()
            .filter(row -> row.getText().toLowerCase().contains(filterText))
            .collect(Collectors.toList()));
      }
      else {
        m_callsByAll++;
        return LookupResponse.create(m_allRows);
      }
    }

    public int getTotalCalls() {
      return m_callsByKey + m_callsByText + m_callsByAll;
    }

    public int getCallsByKey() {
      return m_callsByKey;
    }

    public int getCallsByText() {
      return m_callsByText;
    }

    public int getCallsByAll() {
      return m_callsByAll;
    }

    public void resetCalls() {
      m_callsByKey = 0;
      m_callsByText = 0;
      m_callsByAll = 0;
    }
  }
}
