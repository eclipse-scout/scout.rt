/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import static org.junit.Assert.*;

import java.util.Locale;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceValidationHelperTest.SequenceValidationTestForm.MainBox.GroupBox.FromField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceValidationHelperTest.SequenceValidationTestForm.MainBox.GroupBox.ToField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SequenceValidationHelperTest {

  protected SequenceValidationHelper m_helper = BEANS.get(SequenceValidationHelper.class);

  @Before
  public void setUp() {
    NlsLocale.set(new Locale("en", "CH"));
  }

  @After
  public void tearDown() {
    NlsLocale.set(null);
  }

  @Test
  public void testCheckFromTo() {
    // check standard case
    SequenceValidationTestForm f = new SequenceValidationTestForm();
    f.setModal(false);

    FromField from = f.getFromField();
    ToField to = f.getToField();

    // init -> no error
    m_helper.checkFromTo(from, to);
    assertNull(from.getErrorStatus());
    assertNull(to.getErrorStatus());

    // end>start -> no error
    from.setValue(1L);
    to.setValue(2L);
    m_helper.checkFromTo(from, to);
    assertNull(from.getErrorStatus());
    assertNull(to.getErrorStatus());

    // start>end -> error
    from.setValue(3L);
    m_helper.checkFromTo(from, to);
    assertEquals(InvalidSequenceStatus.ERROR, from.getErrorStatus().getSeverity());
    assertEquals("'from-label' must be greater than or equal to 'to-label'", from.getErrorStatus().getMessage());
    assertNull(to.getErrorStatus());

    // start null -> no error
    from.setValue(null);
    m_helper.checkFromTo(from, to);
    assertNull(from.getErrorStatus());
    assertNull(to.getErrorStatus());

    // end null -> no error
    from.setValue(1L);
    to.setValue(0L);
    m_helper.checkFromTo(from, to);
    assertEquals(InvalidSequenceStatus.ERROR, from.getErrorStatus().getSeverity());
    assertNull(to.getErrorStatus());

    to.setValue(null);
    m_helper.checkFromTo(from, to);
    assertNull(from.getErrorStatus());
    assertNull(to.getErrorStatus());
    f.doClose();
  }

  /**
   * Form with some sequence boxes for testing.
   */
  public static class SequenceValidationTestForm extends AbstractForm {

    public SequenceValidationTestForm() {
      super();
    }

    public FromField getFromField() {
      return getFieldByClass(FromField.class);
    }

    public ToField getToField() {
      return getFieldByClass(ToField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class GroupBox extends AbstractGroupBox {

        @Order(10)
        public class FromField extends AbstractLongField {

          @Override
          protected String getConfiguredLabel() {
            return "from-label";
          }
        }

        @Order(20)
        public class ToField extends AbstractLongField {

          @Override
          protected String getConfiguredLabel() {
            return "to-label";
          }
        }
      }
    }
  }
}
