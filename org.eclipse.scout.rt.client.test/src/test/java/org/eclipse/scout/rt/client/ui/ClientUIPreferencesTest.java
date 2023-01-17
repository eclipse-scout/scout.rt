/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.fixture.TestCustomClientPreferenceFixtureDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.prefs.CustomClientPreferenceId;
import org.eclipse.scout.rt.shared.services.common.prefs.AbstractUserPreferencesStorageService;
import org.eclipse.scout.rt.shared.services.common.prefs.IPreferences;
import org.eclipse.scout.rt.shared.services.common.prefs.IUserPreferencesStorageService;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Junit test for {@link ClientUIPreferences}
 *
 * @since 5.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(value = TestEnvironmentClientSession.class, provider = ClientSessionProvider.class) // ensure a new session is created for this test
public class ClientUIPreferencesTest {

  private IBean<?> m_registeredUserPrefsStorageService;

  @Before
  public void setup() {

    m_registeredUserPrefsStorageService = BeanTestingHelper.get().registerBean(
        new BeanMetaData(IUserPreferencesStorageService.class)
            .withInitialInstance(new TestingUserPreferencesStorageService())
            .withApplicationScoped(true));
  }

  @After
  public void tearDown() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    prefs.m_prefs.clear();
    BeanTestingHelper.get().unregisterBean(m_registeredUserPrefsStorageService);
  }

  @Test
  public void testAllTableConfigsPrefixed() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    String configName = "config1";
    TestTable testTable = createTestConfig(prefs, configName);
    for (String key : prefs.m_prefs.keys()) {
      assertTrue(key.startsWith(configName) || key.startsWith(ClientUIPreferences.TABLE_COLUMNS_CONFIGS));
    }
    Set<String> allTableColumnsConfigs = prefs.getAllTableColumnsConfigs(testTable);
    assertEquals(1, allTableColumnsConfigs.size());
    assertTrue(allTableColumnsConfigs.containsAll(CollectionUtility.arrayList(configName)));
  }

  private TestTable createTestConfig(ClientUIPreferences prefs, String... configNames) {
    TestTable table = new TestTable();
    for (String configName : configNames) {
      prefs.addTableColumnsConfig(table, configName);
      prefs.setAllTableColumnPreferences(table, configName);
      prefs.setTableCustomizerData(table.getTableCustomizer(), configName);
    }
    return table;
  }

  @Test
  public void testRemoveTableConfig() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    String config1 = "config1";
    String config2 = "config2";
    int config1Count = 0;
    int config2Count = 0;
    TestTable testTable = createTestConfig(prefs, config1, config2);
    for (String key : prefs.m_prefs.keys()) {
      assertTrue(key.startsWith(config1) || key.startsWith(config2) || key.startsWith(ClientUIPreferences.TABLE_COLUMNS_CONFIGS));
      if (key.startsWith(config1)) {
        ++config1Count;
      }
      else if (key.startsWith(config2)) {
        ++config2Count;
      }
    }
    assertEquals(config1Count, config2Count);
    Set<String> allTableColumnsConfigs = prefs.getAllTableColumnsConfigs(testTable);
    assertEquals(2, allTableColumnsConfigs.size());
    assertTrue(allTableColumnsConfigs.containsAll(CollectionUtility.arrayList(config1, config2)));

    prefs.removeTableColumnsConfig(testTable, config1);
    for (String key : prefs.m_prefs.keys()) {
      assertTrue("Entry should be removed: " + key, key.startsWith(config2) || key.startsWith(ClientUIPreferences.TABLE_COLUMNS_CONFIGS));
    }
    allTableColumnsConfigs = prefs.getAllTableColumnsConfigs(testTable);
    assertEquals(1, allTableColumnsConfigs.size());
    assertTrue(allTableColumnsConfigs.contains(config2));
    assertEquals(config1Count, prefs.m_prefs.keys().size() - 1);
  }

  @Test
  public void testRenameTableConfig() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    String config1 = "config1";
    String config2 = "config2";
    int config2Count = 0;
    TestTable testTable = createTestConfig(prefs, config1, config2);
    for (String key : prefs.m_prefs.keys()) {
      if (key.startsWith(config2)) {
        ++config2Count;
      }
    }

    String renamedConfig = "renamedConfig";
    int renamedConfigCount = 0;
    int config2CountAfterRename = 0;
    prefs.renameTableColumnsConfig(testTable, config1, renamedConfig);
    for (String key : prefs.m_prefs.keys()) {
      assertTrue(key.startsWith(renamedConfig) || key.startsWith(config2) || key.startsWith(ClientUIPreferences.TABLE_COLUMNS_CONFIGS));
      if (key.startsWith(config2)) {
        ++config2CountAfterRename;
      }
      else if (key.startsWith(renamedConfig)) {
        ++renamedConfigCount;
      }
    }
    assertEquals(config2Count, config2CountAfterRename);
    assertEquals(renamedConfigCount, config2CountAfterRename);
  }

  @Test
  public void testCustomClientPreference() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();

    CustomClientPreferenceId id1 = CustomClientPreferenceId.of("scoutTestFixture.Test1");
    CustomClientPreferenceId id2 = CustomClientPreferenceId.of("scoutTestFixture.Test2");

    assertNull(prefs.getCustomClientPreference(id1, TestCustomClientPreferenceFixtureDo.class));
    assertNull(prefs.getCustomClientPreference(id2, TestCustomClientPreferenceFixtureDo.class));

    prefs.setCustomClientPreference(id1, BEANS.get(TestCustomClientPreferenceFixtureDo.class).withName("one"));
    assertEquals("one", prefs.getCustomClientPreference(id1, TestCustomClientPreferenceFixtureDo.class).getName());
    assertNull(prefs.getCustomClientPreference(id2, TestCustomClientPreferenceFixtureDo.class));

    prefs.setCustomClientPreference(id2, BEANS.get(TestCustomClientPreferenceFixtureDo.class).withName("two"));
    assertEquals("one", prefs.getCustomClientPreference(id1, TestCustomClientPreferenceFixtureDo.class).getName());
    assertEquals("two", prefs.getCustomClientPreference(id2, TestCustomClientPreferenceFixtureDo.class).getName());

    prefs.setCustomClientPreference(id1, null);
    assertNull(prefs.getCustomClientPreference(id1, TestCustomClientPreferenceFixtureDo.class));
    assertEquals("two", prefs.getCustomClientPreference(id2, TestCustomClientPreferenceFixtureDo.class).getName());

    prefs.setCustomClientPreference(id2, null);
    assertNull(prefs.getCustomClientPreference(id1, TestCustomClientPreferenceFixtureDo.class));
    assertNull(prefs.getCustomClientPreference(id2, TestCustomClientPreferenceFixtureDo.class));
  }

  public static void printOut(IPreferences prefs) {
    for (String key : prefs.keys()) {
      System.out.println(key + "=" + prefs.get(key, ""));
    }
  }

  public static class TestTable extends AbstractTable {

    public class StringColumn extends AbstractStringColumn {
    }

    @Override
    public ITableCustomizer getTableCustomizer() {
      ITableCustomizer mock = Mockito.mock(ITableCustomizer.class);
      Mockito.when(mock.getPreferencesKey()).thenReturn(getClass().getName());
      Mockito.when(mock.getSerializedData()).thenReturn("DummyData".getBytes());
      return mock;
    }

    public class IntColumn extends AbstractIntegerColumn {

      @Override
      protected String getConfiguredBackgroundEffect() {
        return INumberColumn.BackgroundEffect.COLOR_GRADIENT_2;
      }
    }
  }

  private static final class TestingUserPreferencesStorageService extends AbstractUserPreferencesStorageService {

    @Override
    public void flush(IPreferences prefs) {
    }

    @Override
    protected void load(String userScope, String nodeId, IPreferences prefsToFill) {
    }

  }
}
