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
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceBoxTest.SequenceTestForm.MainBox.GroupBox.TwoElementSequence;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceBoxTest.SequenceTestForm.MainBox.GroupBox.TwoElementSequence.EndField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceBoxTest.SequenceTestForm.MainBox.GroupBox.TwoElementSequence.StartField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests from to check in Sequence Box
 * {@link org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SequenceBoxTest {

  @Before
  public void setUp() {
    NlsLocale.set(new Locale("de", "CH"));
  }

  @After
  public void tearDown() {
    NlsLocale.set(null);
  }

  private static final int ONE_MINUTE = 60000;

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox#execCheckFromTo(org.eclipse.scout.rt.
   * client.ui.form.fields.IValueField<T>[], int)}.
   */
  @Test
  public void testExecCheckFromTo() throws Exception {
    //check standard case
    SequenceTestForm f = new SequenceTestForm();
    f.setModal(false);

    TwoElementSequence box = f.getTwoElementSequence();
    StartField start = f.getStartField();
    EndField end = f.getEndField();

    //init -> no error
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    //invalid end -> no error
    start.setValue(new Date(2 * ONE_MINUTE));
    end.setValue(new Date(ONE_MINUTE));

    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertEquals(InvalidSequenceStatus.ERROR, end.getErrorStatus().getSeverity());

    //end>start -> no error
    end.setValue(new Date(3 * ONE_MINUTE));
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    //start>end -> error
    start.setValue(new Date(4 * ONE_MINUTE));
    assertNull(box.getErrorStatus());
    assertEquals(InvalidSequenceStatus.ERROR, start.getErrorStatus().getSeverity());
    assertNull(end.getErrorStatus());

    //start null ->no error
    start.setValue(null);
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());

    //end null -> no error
    start.setValue(new Date(ONE_MINUTE));
    end.setValue(new Date(0));
    assertEquals(InvalidSequenceStatus.ERROR, end.getErrorStatus().getSeverity());
    end.setValue(null);
    assertNull(box.getErrorStatus());
    assertNull(start.getErrorStatus());
    assertNull(end.getErrorStatus());
    f.doClose();
  }

  /**
   * Test that searchFilter's getDisplayText does not contain 'null' as String if a SequenceBox's child has its label
   * configured to 'null'. See bugzilla 432481
   */
  @Test
  public void testSearchFilterText() {
    Calendar cal = Calendar.getInstance();
    cal.set(2014, Calendar.JANUARY, 1, 8, 0);

    SequenceTestForm f = new SequenceTestForm();
    f.getStartField().setValue(cal.getTime());

    f.resetSearchFilter();
    String searchFilterText = f.getSearchFilter().getDisplayTextsPlain();
    assertEquals("baseLabel from = " + DateUtility.formatTime(cal.getTime()), searchFilterText);

    f.getStartField().setLabel(null);
    f.resetSearchFilter();
    searchFilterText = f.getSearchFilter().getDisplayTextsPlain();
    assertEquals("baseLabel = " + DateUtility.formatTime(cal.getTime()), searchFilterText);
  }

  @Test
  public void testRightNeighbor() throws Exception {
    ThreeElementSequence sb = new ThreeElementSequence();
    sb.clearErrorStatus();
    sb.getFirstField().setValue(3);
    sb.getSecondField().setValue(2);
    sb.getThirdField().setValue(1);
    sb.execCheckFromTo(0);
    assertFalse(sb.getFirstField().isContentValid());
    assertEquals(getLessThanText(sb.getFirstField(), sb.getSecondField()), sb.getFirstField().getErrorStatus().getMessage());
  }

  @Test
  public void testRightNeighborNull() throws Exception {
    ThreeElementSequence sb = new ThreeElementSequence();
    sb.clearErrorStatus();
    sb.getFirstField().setValue(3);
    sb.getSecondField().setValue(null);
    sb.getThirdField().setValue(1);
    sb.execCheckFromTo(0);
    assertFalse(sb.getFirstField().isContentValid());
    assertEquals(getLessThanText(sb.getFirstField(), sb.getThirdField()), sb.getFirstField().getErrorStatus().getMessage());
  }

  @Test
  public void testLeftNeighbor() throws Exception {
    ThreeElementSequence sb = new ThreeElementSequence();
    sb.clearErrorStatus();
    sb.getFirstField().setValue(3);
    sb.getSecondField().setValue(2);
    sb.getThirdField().setValue(1);
    sb.execCheckFromTo(1);
    assertFalse(sb.getSecondField().isContentValid());
    assertEquals(getGreaterThanText(sb.getSecondField(), sb.getFirstField()), sb.getSecondField().getErrorStatus().getMessage());
  }

  @Test
  public void testLeftNeighborNull() throws Exception {
    ThreeElementSequence sb = new ThreeElementSequence();
    sb.clearErrorStatus();
    sb.getFirstField().setValue(null);
    sb.getSecondField().setValue(2);
    sb.getThirdField().setValue(1);
    sb.execCheckFromTo(1);
    assertFalse(sb.getSecondField().isContentValid());
    assertEquals(getLessThanText(sb.getSecondField(), sb.getThirdField()), sb.getSecondField().getErrorStatus().getMessage());
  }

  @Test
  public void testAllEmpty() throws Exception {
    ThreeElementSequence sb = new ThreeElementSequence();
    sb.addErrorStatus(new InvalidSequenceStatus(""));
    sb.getFirstField().setValue(null);
    sb.getSecondField().setValue(null);
    sb.getThirdField().setValue(null);
    sb.execCheckFromTo(0);
    assertTrue(sb.isContentValid());
  }

  private String getLessThanText(IFormField f1, IFormField f2) {
    return TEXTS.get("XMustBeLessThanOrEqualY", f1.getLabel(), f2.getLabel());
  }

  private String getGreaterThanText(IFormField f1, IFormField f2) {
    return TEXTS.get("XMustBeGreaterThanOrEqualY", f1.getLabel(), f2.getLabel());
  }

  @Test
  public void testThreeFieldsValid() {
    ThreeElementSequence sb = new ThreeElementSequence();
    sb.clearErrorStatus();
    sb.getFirstField().setValue(1);
    sb.getSecondField().setValue(2);
    sb.getThirdField().setValue(3);
    sb.execCheckFromTo(0);
    assertTrue(sb.isContentValid());
    assertTrue(sb.getFirstField().isContentValid());
    assertTrue(sb.getSecondField().isContentValid());
    assertTrue(sb.getThirdField().isContentValid());
  }

  @Test
  public void testStatusVisible() throws Exception {
    ThreeElementSequence sb = new ThreeElementSequence();
    assertFalse(sb.getFirstField().isStatusVisible());
    assertFalse(sb.getSecondField().isStatusVisible());
    assertFalse(sb.getThirdField().isStatusVisible());
  }

  /**
   * Form with some sequence boxes for testing.
   */
  public static class SequenceTestForm extends AbstractForm {

    public SequenceTestForm() {
      super();
    }

    public TwoElementSequence getTwoElementSequence() {
      return getFieldByClass(TwoElementSequence.class);
    }

    public StartField getStartField() {
      return getFieldByClass(StartField.class);
    }

    public EndField getEndField() {
      return getFieldByClass(EndField.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class GroupBox extends AbstractGroupBox {
        @Order(10)
        public class TwoElementSequence extends AbstractSequenceBox {

          /**
           * force field check
           */
          protected void execCheckFromTo(int changedIndex) {
            super.execCheckFromTo(new AbstractDateField[]{getStartField(), getEndField()}, changedIndex);
          }

          @Override
          protected String getConfiguredLabel() {
            return "baseLabel";
          }

          @Order(10)
          public class StartField extends AbstractTimeField {
            @Override
            protected String getConfiguredLabel() {
              return "from";
            }
          }

          @Order(20)
          public class EndField extends AbstractTimeField {
            @Override
            protected String getConfiguredLabel() {
              return "to";
            }
          }
        }
      }

      @Order(20)
      public class CloseButton extends AbstractCloseButton {
        @Override
        protected String getConfiguredLabel() {
          return "Close";
        }
      }
    }
  }

  @Order(10)
  public class ThreeElementSequence extends AbstractSequenceBox {

    @Override
    protected String getConfiguredLabel() {
      return "baseLabel";
    }

    public FirstField getFirstField() {
      return getFieldByClass(FirstField.class);
    }

    public SecondField getSecondField() {
      return getFieldByClass(SecondField.class);
    }

    public ThirdField getThirdField() {
      return getFieldByClass(ThirdField.class);
    }

    /**
     * force field check
     */
    public void execCheckFromTo(int changedIndex) {
      super.execCheckFromTo(new AbstractIntegerField[]{getFirstField(), getSecondField(), getThirdField()}, changedIndex);
    }

    @Order(10)
    public class FirstField extends AbstractIntegerField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }

    @Order(20)
    public class SecondField extends AbstractIntegerField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }

    @Order(30)
    public class ThirdField extends AbstractIntegerField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }
  }

  @Order(10)
  public class ThreeElementLastInvisibleSequence extends AbstractSequenceBox {

    @Override
    protected String getConfiguredLabel() {
      return "baseLabel";
    }

    public FirstField getFirstField() {
      return getFieldByClass(FirstField.class);
    }

    public SecondField getSecondField() {
      return getFieldByClass(SecondField.class);
    }

    public ThirdField getThirdField() {
      return getFieldByClass(ThirdField.class);
    }

    /**
     * force field check
     */
    public void execCheckFromTo(int changedIndex) {
      super.execCheckFromTo(new AbstractIntegerField[]{getFirstField(), getSecondField(), getThirdField()}, changedIndex);
    }

    @Order(10)
    public class FirstField extends AbstractIntegerField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }

    @Order(20)
    public class SecondField extends AbstractIntegerField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }

    @Order(30)
    public class ThirdField extends AbstractIntegerField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }
    }
  }
}
