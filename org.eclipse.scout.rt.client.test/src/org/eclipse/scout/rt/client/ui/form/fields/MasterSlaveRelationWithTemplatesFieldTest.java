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
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.MasterSlaveRelationWithTemplatesFieldTest.MyForm.MainBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.testing.client.form.FormHandler;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class MasterSlaveRelationWithTemplatesFieldTest {

  @Test
  public void testTemplateFields() throws Exception {
    MyForm f = new MyForm();
    try {
      f.start();

      checkMasterSlaveBox(f.getFieldByClass(MainBox.MasterSlave1Box.class));
      checkMasterSlaveBox(f.getFieldByClass(MainBox.MasterSlave2Box.class));
      checkMasterSlaveBox(f.getFieldByClass(MainBox.MasterSlave3Box.class));
      checkInnerMasterSlaveBox(f.getFieldByClass(MainBox.GroupBox1.class));
      checkInnerMasterSlaveBox(f.getFieldByClass(MainBox.GroupBox2.class));
    }
    finally {
      f.doClose();
    }
  }

  @Test
  public void testNonExistingMasterField() throws Exception {
    NonExistingMasterFieldTestForm f = new NonExistingMasterFieldTestForm();
    try {
      f.start();
      NonExistingMasterFieldTestForm.MainBox.StringField field = f.getFieldByClass(NonExistingMasterFieldTestForm.MainBox.StringField.class);
      Assert.assertNotNull(field);
      Assert.assertNull(field.getMasterField());
    }
    finally {
      f.doClose();
    }
  }

  private void checkMasterSlaveBox(AbstractMasterSlaveBox masterSlaveBox) {
    Assert.assertEquals(false, masterSlaveBox.isMasterChanged());
    masterSlaveBox.changeMasterField();
    Assert.assertEquals(true, masterSlaveBox.isMasterChanged());
  }

  private void checkInnerMasterSlaveBox(AbstractInnerGroupBox innerMasterSlaveBox) {
    checkMasterSlaveBox(innerMasterSlaveBox.getInnerMasterSlave1Box());
    checkMasterSlaveBox(innerMasterSlaveBox.getInnerMasterSlave2Box());
  }

  public final static class MyForm extends AbstractForm {

    private MyForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "MyForm";
    }

    @Override
    protected boolean getConfiguredModal() {
      return false;
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0f)
      public class MasterSlave1Box extends AbstractMasterSlaveBox {
      }

      @Order(20.0f)
      public class MasterSlave2Box extends AbstractMasterSlaveBox {
      }

      @Order(30.0f)
      public class MasterSlave3Box extends AbstractMasterSlaveBox {
      }

      @Order(40.0f)
      public class GroupBox1 extends AbstractInnerGroupBox {
      }

      @Order(50.0f)
      public class GroupBox2 extends AbstractInnerGroupBox {
      }
    }

    public void start() throws ProcessingException {
      startInternal(new FormHandler());
    }
  }

  public static abstract class AbstractInnerGroupBox extends AbstractGroupBox {

    public InnerMasterSlave1Box getInnerMasterSlave1Box() {
      return getFieldByClass(InnerMasterSlave1Box.class);
    }

    public InnerMasterSlave2Box getInnerMasterSlave2Box() {
      return getFieldByClass(InnerMasterSlave2Box.class);
    }

    @Order(10.0f)
    public class InnerMasterSlave1Box extends AbstractMasterSlaveBox {
    }

    @Order(20.0f)
    public class InnerMasterSlave2Box extends AbstractMasterSlaveBox {
    }
  }

  public static abstract class AbstractMasterSlaveBox extends AbstractGroupBox {

    private boolean m_masterChanged = false;

    public boolean isMasterChanged() {
      return m_masterChanged;
    }

    public void changeMasterField() {
      getFieldByClass(MasterField.class).setValue(System.currentTimeMillis());
    }

    @Order(10.0f)
    public class MasterField extends AbstractSmartField<Long> {
      @Override
      protected String getConfiguredLabel() {
        return "Master";
      }
    }

    @Order(20.0f)
    public class SlaveField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return "Slave";
      }

      @Override
      protected Class<? extends IValueField> getConfiguredMasterField() {
        return MasterField.class;
      }

      @Override
      protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
        super.execChangedMasterValue(newMasterValue);
        m_masterChanged = true;
      }
    }
  }

  public final static class NonExistingMasterFieldTestForm extends AbstractForm {

    private NonExistingMasterFieldTestForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "NonExistingMasterFieldTestForm";
    }

    @Override
    protected boolean getConfiguredModal() {
      return false;
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0f)
      public class StringField extends AbstractStringField {
        @Override
        protected Class<? extends IValueField> getConfiguredMasterField() {
          return AbstractMasterSlaveBox.MasterField.class;
        }
      }
    }

    public void start() throws ProcessingException {
      startInternal(new FormHandler());
    }
  }
}
