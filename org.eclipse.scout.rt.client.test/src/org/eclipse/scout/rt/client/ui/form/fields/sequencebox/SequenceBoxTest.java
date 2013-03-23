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

import java.util.Date;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceBoxTest.SequenceTestForm.MainBox.GroupBox.TwoElementSequence;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceBoxTest.SequenceTestForm.MainBox.GroupBox.TwoElementSequence.EndField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.SequenceBoxTest.SequenceTestForm.MainBox.GroupBox.TwoElementSequence.StartField;
import org.eclipse.scout.rt.testing.client.form.FormHandler;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests from to check in Sequence Box
 * {@link org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox}
 */
@RunWith(ScoutClientTestRunner.class)
public class SequenceBoxTest {

  private static int ONE_MINUTE = 60000;

  /**
   * Test method for {@link
   * org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox#execCheckFromTo(org.eclipse.scout.rt.
   * client.ui.form.fields.IValueField<T>[], int)}.
   */
  @Test
  public void testExecCheckFromTo() throws Exception {
    //check standard case
    SequenceTestForm f = new SequenceTestForm();
    f.setModal(false);
    f.start(new FormHandler());

    TwoElementSequence box = f.getTwoElementSequence();
    StartField start = f.getStartField();
    EndField end = f.getEndField();

    //init -> no error
    Assert.assertNull(box.getErrorStatus());
    Assert.assertNull(start.getErrorStatus());
    Assert.assertNull(end.getErrorStatus());

    //invalid end -> no error
    start.setValue(new Date(2 * ONE_MINUTE));
    end.setValue(new Date(ONE_MINUTE));

    Assert.assertNull(box.getErrorStatus());
    Assert.assertNull(start.getErrorStatus());
    Assert.assertEquals(InvalidSequenceStatus.ERROR, end.getErrorStatus().getSeverity());

    //end>start -> no error
    end.setValue(new Date(3 * ONE_MINUTE));
    Assert.assertNull(box.getErrorStatus());
    Assert.assertNull(start.getErrorStatus());
    Assert.assertNull(end.getErrorStatus());

    //start>end -> error (on box instead of first label, because label is merged)
    start.setValue(new Date(4 * ONE_MINUTE));
    Assert.assertEquals(InvalidSequenceStatus.ERROR, box.getErrorStatus().getSeverity());
    Assert.assertNull(start.getErrorStatus());
    Assert.assertNull(end.getErrorStatus());

    //start null ->no error
    start.setValue(null);
    Assert.assertNull(box.getErrorStatus());
    Assert.assertNull(start.getErrorStatus());
    Assert.assertNull(end.getErrorStatus());

    //end null -> no error
    start.setValue(new Date(ONE_MINUTE));
    end.setValue(new Date(0));
    Assert.assertEquals(InvalidSequenceStatus.ERROR, end.getErrorStatus().getSeverity());
    end.setValue(null);
    Assert.assertNull(box.getErrorStatus());
    Assert.assertNull(start.getErrorStatus());
    Assert.assertNull(end.getErrorStatus());
    f.doClose();
  }

  /**
   * Form with some sequence boxes for testing.
   */
  public static class SequenceTestForm extends AbstractForm {

    public void start(IFormHandler handler) throws ProcessingException {
      startInternal(handler);
    }

    public SequenceTestForm() throws ProcessingException {
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
          public void execCheckFromTo(int changedIndex) throws ProcessingException {
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
}
