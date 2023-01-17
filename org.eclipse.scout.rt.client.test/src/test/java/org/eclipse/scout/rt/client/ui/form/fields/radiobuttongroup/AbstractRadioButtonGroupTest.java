/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.internal.RadioButtonGroupGrid;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.DynamicStringField;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractRadioButtonGroup}
 *
 * @since 4.0.0-M7
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractRadioButtonGroupTest {

  private static List<IBean<?>> m_beans;

  private P_StandardRadioButtonGroup m_group;

  @BeforeClass
  public static void beforeClass() {
    m_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(P_CompanyLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
  }

  @Before
  public void setUp() {
    m_group = new P_StandardRadioButtonGroup();
    m_group.init();
  }

  @Test
  public void testInitialized() {
    assertEquals("RadioButtonGroup", m_group.getLabel());
    assertEquals(4, m_group.getFieldCount());
    assertEquals(3, m_group.getButtons().size());
    assertNull(m_group.getSelectedButton());
    assertNull(m_group.getSelectedKey());

    assertEquals(m_group.getRadioButton1(), m_group.getButtonFor(1L));
    assertEquals(m_group.getRadioButton2(), m_group.getButtonFor(2L));
    assertEquals(m_group.getRadioButton3(), m_group.getButtonFor(3L));
    assertNull(m_group.getButtonFor(4L));

    assertNull(m_group.getErrorStatus());

    assertEquals(Long.valueOf(1L), m_group.getRadioButton1().getRadioValue());
    assertEquals(Long.valueOf(2L), m_group.getRadioButton2().getRadioValue());
    assertEquals(Long.valueOf(3L), m_group.getRadioButton3().getRadioValue());
  }

  @Test
  public void testSelectionViaButton() {
    m_group.selectButton(m_group.getRadioButton2());
    assertEquals(m_group.getSelectedButton(), m_group.getRadioButton2());

    m_group.selectButton(m_group.getRadioButton3());
    assertEquals(m_group.getSelectedButton(), m_group.getRadioButton3());
  }

  @Test
  public void testInvalidSelectionViaButton() {
    m_group.selectButton(new AbstractRadioButton<Long>() {
    });
    assertNull(m_group.getSelectedButton());
  }

  @Test
  public void testGridColumnCount() {
    RadioButtonGroupGrid grid = new RadioButtonGroupGrid();
    IRadioButtonGroup<?> group = new P_StandardRadioButtonGroup();
    int numFieldsInGroup = group.getFieldCount();
    int moreThanFields = numFieldsInGroup + 10;
    assertEquals(IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT, group.getGridColumnCount());
    assertFalse(group.setGridColumnCount(IRadioButtonGroup.DEFAULT_GRID_COLUMN_COUNT));

    grid.validate(group);
    assertEquals(numFieldsInGroup, grid.getGridColumnCount());
    assertEquals(1, grid.getGridRowCount());

    GridData gridDataHints = group.getGridDataHints().copy();
    gridDataHints.h = 2;
    group.setGridDataInternal(gridDataHints);
    grid.validate(group);
    assertEquals(2, grid.getGridColumnCount());
    assertEquals(2, grid.getGridRowCount());

    gridDataHints = group.getGridDataHints().copy();
    gridDataHints.h = numFieldsInGroup;
    group.setGridDataInternal(gridDataHints);
    grid.validate(group);
    assertEquals(1, grid.getGridColumnCount());
    assertEquals(numFieldsInGroup, grid.getGridRowCount());

    gridDataHints = group.getGridDataHints().copy();
    gridDataHints.h = moreThanFields;
    group.setGridDataInternal(gridDataHints);
    grid.validate(group);
    assertEquals(1, grid.getGridColumnCount());
    assertEquals(numFieldsInGroup, grid.getGridRowCount());

    assertTrue(group.setGridColumnCount(3));
    assertEquals(3, group.getGridColumnCount());
    grid.validate(group);
    assertEquals(3, grid.getGridColumnCount());
    assertEquals(2, grid.getGridRowCount());

    assertTrue(group.setGridColumnCount(moreThanFields));
    assertEquals(moreThanFields, group.getGridColumnCount());
    grid.validate(group);
    assertEquals(numFieldsInGroup, grid.getGridColumnCount());
    assertEquals(1, grid.getGridRowCount());
  }

  @Test
  public void testSelectionViaKey() {
    m_group.selectKey(Long.valueOf(1L));
    assertEquals(Long.valueOf(1L), m_group.getSelectedKey());
    m_group.selectKey(Long.valueOf(3L));
    assertEquals(Long.valueOf(3L), m_group.getSelectedKey());
    m_group.selectKey(Long.valueOf(2L));
    assertEquals(Long.valueOf(2L), m_group.getSelectedKey());
  }

  @Test
  public void testInvalidSelectionViaKey() {
    //set an invalid value: ()
    m_group.selectKey(Long.valueOf(4L));
    assertNotNull(m_group.getErrorStatus());

    //select valid value again
    m_group.selectKey(1L);
    assertNull(m_group.getErrorStatus());
  }

  @Test
  public void testEnabledDisabled() {
    assertAllButtonsEnabled(true, m_group);

    m_group.getRadioButton2().setEnabled(false, true, true);
    assertTrue(!m_group.getRadioButton2().isEnabled());

    m_group.setEnabled(false, true, true);
    assertAllButtonsEnabled(false, m_group);

    m_group.setEnabled(true, false, true);
    assertAllButtonsEnabled(true, m_group);
  }

  @Test
  public void testGetFieldById() {
    assertEquals(m_group.getRadioButton1(), m_group.getFieldById(m_group.getRadioButton1().getClass().getSimpleName()));
    assertNull(m_group.getFieldById("nonExisting"));
  }

  @Test
  public void testGetFieldIndex() {
    assertNotNull(m_group.getFieldIndex(m_group.getRadioButton2()));
    assertEquals(-1, m_group.getFieldIndex(new AbstractRadioButton<Integer>() {
    }));
  }

  @Test
  public void testGetButtons() {
    assertEquals(3, m_group.getButtons().size());

    AbstractRadioButtonGroup<Integer> emptyGroup = new AbstractRadioButtonGroup<Integer>() {
    };
    assertEquals(0, emptyGroup.getButtons().size());
  }

  @Test
  public void testLookupCall() {
    AbstractRadioButtonGroup<Long> lookupGroup = new P_RadioButtonGroupWithLookupCall();
    lookupGroup.initConfig();
    assertEquals(new P_CompanyLookupCall(), lookupGroup.getLookupCall());
    assertEquals(3, lookupGroup.getButtons().size());
    assertNull(lookupGroup.getSelectedButton());
  }

  @Test
  public void testDisplayTextInitial() {
    AbstractRadioButtonGroup<Long> lookupGroup = new P_RadioButtonGroupWithLookupCall();
    lookupGroup.initConfig();
    assertEquals("", lookupGroup.getDisplayText());
  }

  @Test
  public void testDisplayTextSelectKey() {
    AbstractRadioButtonGroup<Long> lookupGroup = new P_RadioButtonGroupWithLookupCall();
    lookupGroup.initConfig();
    lookupGroup.selectKey(1L);
    assertEquals("Business Systems Integration AG", lookupGroup.getDisplayText());
  }

  @Test
  public void testDisplayTextSelectNull() {
    AbstractRadioButtonGroup<Long> lookupGroup = new P_RadioButtonGroupWithLookupCall();
    lookupGroup.initConfig();
    lookupGroup.selectKey(null);
    assertEquals("", lookupGroup.getDisplayText());
  }

  @Test
  public void testGetForm() {
    IForm formMock = mock(IForm.class);
    m_group.setFormInternal(formMock);
    assertEquals(formMock, m_group.getForm());
    for (IRadioButton<Long> radioButton : m_group.getButtons()) {
      assertEquals(formMock, radioButton.getForm());
    }
  }

  @Test
  public void testAddField() {
    DynamicStringField field = new DynamicStringField("id", "test-field");
    assertNull(getFieldPropertyChangeListener(field));
    m_group.addField(field);
    assertNotNull(getFieldPropertyChangeListener(field));
  }

  @Test
  public void testRemoveField() {
    P_StandardRadioButtonGroup.LabelField labelField = m_group.getFieldByClass(P_StandardRadioButtonGroup.LabelField.class);
    assertNotNull(labelField);
    assertSame(m_group, labelField.getParentField());
    assertNotNull(getFieldPropertyChangeListener(labelField));

    m_group.removeField(labelField);
    assertNull(labelField.getParentField());
    assertNull(getFieldPropertyChangeListener(labelField));
    assertNull(m_group.getFieldByClass(P_StandardRadioButtonGroup.LabelField.class));
  }

  private PropertyChangeListener getFieldPropertyChangeListener(AbstractPropertyObserver observer) {
    List<PropertyChangeListener> listeners = observer.getPropertyChangeListeners();
    for (PropertyChangeListener listener : listeners) {
      if (listener.getClass().getEnclosingClass() == AbstractRadioButtonGroup.class) {
        return listener;
      }
    }
    return null;
  }

  private void assertAllButtonsEnabled(boolean enabled, IRadioButtonGroup<?> group) {
    for (IRadioButton<?> btn : m_group.getButtons()) {
      assertEquals(enabled, btn.isEnabledIncludingParents());
    }
  }

  private class P_StandardRadioButtonGroup extends AbstractRadioButtonGroup<Long> {

    public RadioButton1 getRadioButton1() {
      return getFieldByClass(RadioButton1.class);
    }

    public RadioButton2 getRadioButton2() {
      return getFieldByClass(RadioButton2.class);
    }

    public RadioButton3 getRadioButton3() {
      return getFieldByClass(RadioButton3.class);
    }

    @Override
    protected String getConfiguredLabel() {
      return "RadioButtonGroup";
    }

    @Order(10)
    public class RadioButton1 extends AbstractRadioButton<Long> {
      @Override
      public Long getRadioValue() {
        return Long.valueOf(1L);
      }
    }

    @Order(15)
    public class LabelField extends AbstractLabelField {
      @Override
      protected String getConfiguredLabel() {
        return "Label";
      }
    }

    @Order(20)
    public class RadioButton2 extends AbstractRadioButton<Long> {
      @Override
      public Long getRadioValue() {
        return Long.valueOf(2L);
      }
    }

    @Order(30)
    public class RadioButton3 extends AbstractRadioButton<Long> {
      @Override
      public Long getRadioValue() {
        return Long.valueOf(3L);
      }
    }

  }

  private class P_RadioButtonGroupWithLookupCall extends AbstractRadioButtonGroup<Long> {
    @Override
    protected String getConfiguredLabel() {
      return "CodeTypeRadioButtonGroup";
    }

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_CompanyLookupCall.class;
    }
  }

  public static class P_CompanyLookupCall extends LocalLookupCall<Long> {

    private static final long serialVersionUID = 1L;

    @Override
    protected List<ILookupRow<Long>> execCreateLookupRows() {
      ArrayList<ILookupRow<Long>> rows = new ArrayList<>();
      rows.add(new LookupRow<>(1L, "Business Systems Integration AG"));
      rows.add(new LookupRow<>(2L, "Eclipse"));
      rows.add(new LookupRow<>(3L, "Google"));
      rows.add(new LookupRow<>(null, "null value"));
      return rows;
    }
  }
}
