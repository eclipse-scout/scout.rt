/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import static org.junit.Assert.*;

import java.util.Locale;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractFromToSequenceBoxTest.FromToSequenceTestForm.MainBox.GroupBox.FromToSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractFromToSequenceBoxTest.FromToSequenceTestForm.MainBox.GroupBox.FromToSequenceBox.EndField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractFromToSequenceBoxTest.FromToSequenceTestForm.MainBox.GroupBox.FromToSequenceBox.StartField;
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
public class AbstractFromToSequenceBoxTest {

  @Before
  public void setUp() {
    NlsLocale.set(new Locale("en", "CH"));
  }

  @After
  public void tearDown() {
    NlsLocale.set(null);
  }

  @Test
  public void testExecCheckFromTo() {
    // check standard case
    FromToSequenceTestForm f = new FromToSequenceTestForm();
    f.setModal(false);

    FromToSequenceBox box = f.getFromToSequenceBox();
    StartField start = f.getStartField();
    EndField end = f.getEndField();

    // init -> no error
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    // end>start -> no error
    start.setValue(1L);
    end.setValue(2L);
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    // start>end -> error
    start.setValue(3L);
    assertEquals(InvalidSequenceStatus.ERROR, box.getErrorStatus().getSeverity());
    assertEquals("'to' must be greater than or equal to 'from'", box.getErrorStatus().getMessage());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    // start null -> no error
    start.setValue(null);
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    // end null -> no error
    start.setValue(1L);
    end.setValue(0L);
    assertEquals(InvalidSequenceStatus.ERROR, box.getErrorStatus().getSeverity());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    end.setValue(null);
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());
    f.doClose();
  }

  @Test
  public void testExecAddSearchTerms_from() {
    FromToSequenceTestForm f = new FromToSequenceTestForm();
    f.setModal(false);
    f.getStartField().setValue(1L);
    assertEquals("from-to-label from 1", f.getSearchFilter().getDisplayTexts()[0]);
    f.doClose();
  }

  @Test
  public void testExecAddSearchTerms_to() {
    FromToSequenceTestForm f = new FromToSequenceTestForm();
    f.setModal(false);
    f.getEndField().setValue(2L);
    assertEquals("from-to-label to 2", f.getSearchFilter().getDisplayTexts()[0]);
    f.doClose();
  }

  @Test
  public void testExecAddSearchTerms_fromTo() {
    FromToSequenceTestForm f = new FromToSequenceTestForm();
    f.setModal(false);
    f.getStartField().setValue(1L);
    f.getEndField().setValue(2L);
    assertEquals("from-to-label from 1 to 2", f.getSearchFilter().getDisplayTexts()[0]);
    f.doClose();
  }

  @Test
  public void testCompoundLabel() {
    FromToSequenceTestForm f = new FromToSequenceTestForm();

    assertLabel(f.getFromToSequenceBox(), "from-to-label", true);
    assertLabel(f.getStartField(), null, false);
    assertLabel(f.getEndField(), "-", true);

    f.getStartField().setLabel("foo");
    assertLabel(f.getFromToSequenceBox(), "from-to-label foo", true);
    assertLabel(f.getStartField(), "foo", false);
    assertLabel(f.getEndField(), "-", true);

    f.getStartField().setLabelVisible(false);
    f.getStartField().setLabel("bar");
    assertLabel(f.getFromToSequenceBox(), "from-to-label", true);
    assertLabel(f.getStartField(), "bar", false);
    assertLabel(f.getEndField(), "-", true);

    f.getFromToSequenceBox().setLabel(null);
    assertLabel(f.getFromToSequenceBox(), "", true);
    assertLabel(f.getStartField(), "bar", false);
    assertLabel(f.getEndField(), "-", true);

    f.doClose();
  }

  protected void assertLabel(IFormField field, String expectedLabel, boolean expectedLabelVisible) {
    assertEquals(expectedLabel, field.getLabel());
    assertEquals(expectedLabelVisible, field.isLabelVisible());
  }

  /**
   * Form with some sequence boxes for testing.
   */
  public static class FromToSequenceTestForm extends AbstractForm {

    public FromToSequenceTestForm() {
      super();
    }

    public StartField getStartField() {
      return getFieldByClass(StartField.class);
    }

    public EndField getEndField() {
      return getFieldByClass(EndField.class);
    }

    public FromToSequenceBox getFromToSequenceBox() {
      return getFieldByClass(FromToSequenceBox.class);
    }

    public SequenceBoxTest.SequenceTestForm.MainBox getMainBox() {
      return getFieldByClass(SequenceBoxTest.SequenceTestForm.MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class GroupBox extends AbstractGroupBox {

        @Order(10)
        public class FromToSequenceBox extends AbstractFromToSequenceBox {

          @Override
          protected String getConfiguredLabel() {
            return "from-to-label";
          }

          @Order(10)
          public class StartField extends AbstractLongField {
          }

          @Order(20)
          public class EndField extends AbstractLongField {
            @Override
            protected String getConfiguredLabel() {
              return "-";
            }
          }
        }
      }
    }
  }
}
