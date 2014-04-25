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
package org.eclipse.scout.rt.ui.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.ui.swing.SwingEnvironmentUiTest.TestForm.MainBox.StringField;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.form.ISwingScoutForm;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.icons.CheckboxWithMarginIcon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link AbstractSwingEnvironment}.
 * 
 * @author awe
 * @since 3.10.0-M3
 */
public class SwingEnvironmentUiTest {

  private TestForm m_testForm;
  private static final String TEST_CLASS_ID = "testClassId";
  private static final String TEST_MAIN_BOX_CLASS_ID = "mainBoxId";

  @Before
  public void setUp() throws ProcessingException {
    m_testForm = new TestForm();
    m_testForm.setClassId(TEST_CLASS_ID);
  }

  @After
  public void tearDown() {
    System.clearProperty(AbstractSwingEnvironment.PROP_TEST_IDS_ENABLED);
  }

  /**
   * Test for {@link AbstractSwtEnvironment#assignTestId}
   */
  @Test
  public void testClassIdAssignedForm() throws Exception {
    System.setProperty(AbstractSwingEnvironment.PROP_TEST_IDS_ENABLED, "true");
    AbstractSwingEnvironment env = createEnvironment();
    ISwingScoutForm f = env.createForm((JComponent) null, m_testForm);
    assertEquals(TEST_CLASS_ID, getTestId(f));
  }

  /**
   * Test for {@link AbstractSwtEnvironment#assignTestId}
   * 
   * @throws ExecutionException
   * @throws InvocationTargetException
   * @throws InterruptedException
   */
  @Test
  public void testClassIdAssignedField() throws Exception {
    System.setProperty(AbstractSwingEnvironment.PROP_TEST_IDS_ENABLED, "true");
    AbstractSwingEnvironment env = createEnvironment();
    ISwingScoutFormField f = env.createFormField((JComponent) null, m_testForm.getStringField());
    assertEquals(TEST_CLASS_ID, getTestId(f));
  }

  /**
   * Test for {@link AbstractSwtEnvironment#assignTestId}
   */
  @Test
  public void testClassIdAssignedGroupBox() throws Exception {
    System.setProperty(AbstractSwingEnvironment.PROP_TEST_IDS_ENABLED, "true");
    AbstractSwingEnvironment env = createEnvironment();
    ISwingScoutFormField f = env.createFormField((JComponent) null, m_testForm.getRootGroupBox());
    assertEquals(TEST_MAIN_BOX_CLASS_ID, getTestId(f));
  }

  /**
   * Test for {@link AbstractSwtEnvironment#assignTestId}
   */
  @Test
  public void testClassIdNotAssignedDisabled() throws Exception {
    System.setProperty(AbstractSwingEnvironment.PROP_TEST_IDS_ENABLED, "false");
    AbstractSwingEnvironment env = createEnvironment();
    ISwingScoutForm f = env.createForm((JComponent) null, m_testForm);
    assertNull(getTestId(f));
  }

  /**
   * This test must be executed in Swing thread, because of checkThread() in the constructor of the
   * AbstractSwingEnvironment.
   */
  @Test
  public void testCreateCheckboxWithMarginIcon() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        Icon icon = new P_Env().createCheckboxWithMarginIcon(new Insets(0, 0, 0, 0));
        assertTrue("Default impl. should return CheckboxWithMarginIcon", icon instanceof CheckboxWithMarginIcon);
      }
    });
  }

  static class P_Env extends AbstractSwingEnvironment {

    @Override
    public void init() {
      // NOP - avoid access to Activator.getDefault() in unit-test.
    }
  }

  private Object getTestId(ISwingScoutComposite c) {
    return c.getSwingField().getClientProperty(AbstractSwingEnvironment.COMPONENT_TEST_KEY);
  }

  /**
   * @return
   * @throws InvocationTargetException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private AbstractSwingEnvironment createEnvironment() throws InterruptedException, InvocationTargetException, ExecutionException {
    Callable<AbstractSwingEnvironment> c = new Callable<AbstractSwingEnvironment>() {

      @Override
      public AbstractSwingEnvironment call() throws Exception {
        return new AbstractSwingEnvironment() {

          @Override
          public Icon getIcon(String name) {
            return null;
          }
        };
      }
    };
    FutureTask<AbstractSwingEnvironment> t = new FutureTask<AbstractSwingEnvironment>(c);
    SwingUtilities.invokeAndWait(t);
    return t.get();
  }

  /**
   * Simple test form
   */
  class TestForm extends AbstractForm {

    public StringField getStringField() {
      return getFieldByClass(StringField.class);
    }

    public TestForm() throws ProcessingException {
      super();
    }

    @Order(10.0)
    @ClassId(TEST_MAIN_BOX_CLASS_ID)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      @ClassId(TEST_CLASS_ID)
      public class StringField extends AbstractStringField {
      }
    }
  }

}
