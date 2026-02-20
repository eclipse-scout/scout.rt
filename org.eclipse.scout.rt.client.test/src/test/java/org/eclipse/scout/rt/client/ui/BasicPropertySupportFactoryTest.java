/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.reflect.DefaultValueMap;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.testing.platform.mock.RegisterBeanTestRule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test that the {@link BasicPropertySupportFactory} more or less equal the actual defaults (e.g. changed defaults in the actual class should be reflected in this factory as well to avoid additional values pollution).
 */
@RunWith(PlatformTestRunner.class)
public class BasicPropertySupportFactoryTest {

  public static final String ASSERTION_MESSAGE = "Found additional initially set property, check factory method, see " + BasicPropertySupportFactory.class.getName();

  protected P_TestBasicPropertySupportFactory m_propertySupportFactory = new P_TestBasicPropertySupportFactory();

  @Rule
  public RegisterBeanTestRule<BasicPropertySupportFactory> m_propertySupportFactoryRule = new RegisterBeanTestRule<BasicPropertySupportFactory>(BasicPropertySupportFactory.class, () -> m_propertySupportFactory);

  @Test
  public void testAbstractMenu() {
    new AbstractMenu() {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IMenu.PROP_MENU_TYPES // Set is always copied in setMenuTypes, improvement would be to just store the original Set (it is also copied on get operation)
        ), values.keySet());
  }

  @Test
  public void testAbstractColumn() {
    new AbstractColumn() {
    };
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(), values.keySet());
  }

  @Test
  public void testAbstractButton() {
    new AbstractButton() {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IButton.PROP_GRID_DATA,
            IButton.PROP_GRID_DATA_HINTS,
            IButton.PROP_STATUS_MENU_MAPPINGS, // maybe use static empty list to create a new default (change in behavior)?
            IButton.PROP_KEY_STROKES, // maybe use static empty list to create a new default (change in behavior)?
            IButton.PROP_CONTEXT_MENU
        ), values.keySet());
  }

  @Test
  public void testAbstractGroupBox() {
    new AbstractGroupBox() {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IGroupBox.PROP_GRID_DATA,
            IGroupBox.PROP_GRID_DATA_HINTS,
            IGroupBox.PROP_STATUS_MENU_MAPPINGS, // maybe use static empty list to create a new default (change in behavior)?
            IGroupBox.PROP_KEY_STROKES, // maybe use static empty list to create a new default (change in behavior)?
            IGroupBox.PROP_CONTEXT_MENU,
            IGroupBox.PROP_EMPTY, // usually group boxes are not empty, however ours here is
            IGroupBox.PROP_HAS_VISIBLE_FIELDS, // usually group boxes should have fields, however ours does not
            IGroupBox.PROP_GRID_COLUMN_COUNT,
            IGroupBox.PROP_FIELDS, // maybe use static empty list to create a new default (change in behavior)? re-create on change of fields.
            IGroupBox.PROP_BODY_LAYOUT_CONFIG,
            IGroupBox.PROP_VISIBLE // usually group boxes are visible, ours is not
        ), values.keySet());
  }

  @Test
  public void testAbstractBasicField() {
    new AbstractBasicField(true) {
      @Override
      public IBasicFieldUIFacade getUIFacade() {
        return null;
      }
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IBasicField.PROP_GRID_DATA,
            IBasicField.PROP_GRID_DATA_HINTS,
            IBasicField.PROP_STATUS_MENU_MAPPINGS, // maybe use static empty list to create a new default (change in behavior)?
            IBasicField.PROP_KEY_STROKES, // maybe use static empty list to create a new default (change in behavior)?
            IBasicField.PROP_CONTEXT_MENU
        ), values.keySet());
  }

  @Test
  public void testAbstractValueField() {
    new AbstractValueField(true) {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IValueField.PROP_GRID_DATA,
            IValueField.PROP_GRID_DATA_HINTS,
            IValueField.PROP_STATUS_MENU_MAPPINGS, // maybe use static empty list to create a new default (change in behavior)?
            IValueField.PROP_KEY_STROKES, // maybe use static empty list to create a new default (change in behavior)?
            IValueField.PROP_CONTEXT_MENU
        ), values.keySet());
  }

  @Test
  public void testAbstractFormField() {
    new AbstractFormField(true) {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IFormField.PROP_GRID_DATA,
            IFormField.PROP_GRID_DATA_HINTS,
            IFormField.PROP_STATUS_MENU_MAPPINGS, // maybe use static empty list to create a new default (change in behavior)?
            IFormField.PROP_KEY_STROKES, // maybe use static empty list to create a new default (change in behavior)?
            IFormField.PROP_CONTEXT_MENU
        ), values.keySet());
  }

  @Test
  public void testAbstractAction() {
    new AbstractAction() {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(), values.keySet());
  }

  @Test
  public void testAbstractTable() {
    new AbstractTable() {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            ITable.PROP_KEY_STROKES, // maybe use static empty list to create a new default (change in behavior)?
            ITable.PROP_CONTEXT_MENU,
            ITable.PROP_TABLE_ORGANIZER,
            ITable.PROP_USER_FILTER_MANAGER
        ), values.keySet());
  }

  @Test
  public void testAbstractWidget() {
    new AbstractWidget() {
    }.init();
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getFirst();
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(), values.keySet());
  }

  @Test
  public void testHeaderCell() {
    new AbstractColumn() {
    };
    P_DefaultValueMap<String, Object> lastDefaultValueMap = m_propertySupportFactory.getLastDefaultValueMaps().getLast(); // HeaderCell (created by column)
    Map<String, Object> values = lastDefaultValueMap.getValues();
    assertEquals(ASSERTION_MESSAGE,
        Set.of(
            IHeaderCell.PROP_COLUMN_INDEX
        ), values.keySet());
  }

  @Test
  public void testNull() {
    BasicPropertySupport basicPropertySupport = BEANS.get(BasicPropertySupportFactory.class).createFor(mock(AbstractPropertyObserver.class)); // this tests the default case for the createFor switch statement
    assertTrue("No defaultValueMap expected for switch-default (null) case", m_propertySupportFactory.getLastDefaultValueMaps(true).isEmpty());
    assertNotNull(basicPropertySupport);
    assertNull(basicPropertySupport.getProperty("lorem"));
    basicPropertySupport.setProperty("lorem", "ipsum");
    assertEquals("ipsum", basicPropertySupport.getProperty("lorem"));
  }

  @Test
  public void testCompleteness() {
    Set<String> testMethods = Arrays.stream(BasicPropertySupportFactoryTest.class.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet());
    Arrays.stream(BasicPropertySupportFactory.class.getDeclaredMethods()).filter(Predicate.not(Method::isSynthetic)).forEach(m -> {
      String name = m.getName();
      if ((name.startsWith("createDefaultValueMapFor") && testMethods.contains("test" + name.substring(24))) // method is tested
          || ObjectUtility.isOneOf(name, "createDefaultValueMap", "createFor") // tested implicitly by all test methods
      ) {
        return;
      }
      fail("Shouldn't there be a test for " + m);
    });
  }

  @IgnoreBean
  protected class P_TestBasicPropertySupportFactory extends BasicPropertySupportFactory {

    private List<P_DefaultValueMap<String, Object>> m_lastDefaultValueMaps = new ArrayList<>();

    @Override
    protected Map<String, Object> createDefaultValueMap(Map<String, Object> defaultValues) {
      P_DefaultValueMap<String, Object> defaultValueMap = new P_DefaultValueMap<>(defaultValues, true);
      m_lastDefaultValueMaps.add(defaultValueMap);
      return defaultValueMap;
    }

    public List<P_DefaultValueMap<String, Object>> getLastDefaultValueMaps() {
      return getLastDefaultValueMaps(false);
    }

    public List<P_DefaultValueMap<String, Object>> getLastDefaultValueMaps(boolean allowNone) {
      if (!allowNone && m_lastDefaultValueMaps.isEmpty()) {
        fail("BasicPropertySupport was not created by BasicPropertySupportFactory");
      }
      return m_lastDefaultValueMaps;
    }
  }

  protected class P_DefaultValueMap<K, V> extends DefaultValueMap<K, V> {

    public P_DefaultValueMap(Map<K, V> defaultValues, boolean startEmpty) {
      super(defaultValues, startEmpty);
    }

    protected Map<K, Object> getValues() {
      return m_values;
    }
  }
}
