/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.DynamicForm;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DynamicFieldGridDataTest {

  @Test
  public void testAddField() {
    DynamicForm form = new DynamicForm("TestForm", new MainBox());
    form.start();
    DynamicGroupBox dynField = Mockito.spy(new DynamicGroupBox());
    dynField.setOrder(100);

    MainBox mainBox = form.getFieldByClass(MainBox.class);
    // add dynamic field
    mainBox.addField(dynField);
    Assert.assertTrue(form.isFormStarted());
    Assert.assertEquals(3, mainBox.getFields().size());
    Mockito.verify(dynField, Mockito.times(1)).init();
    Assert.assertEquals(mainBox, dynField.getParentField());
    Assert.assertEquals("StaticStringField,DynamicGroupBox,CancelButton", mainBox.getFields().stream()
        .map(f -> f.getLabel())
        .collect(Collectors.joining(",")));
    Assert.assertEquals(1, dynField.getField01Field().getGridData().h);
    Assert.assertEquals(1, dynField.getField01Field().getGridData().w);
    Assert.assertEquals(3, dynField.getField02Field().getGridData().h);

    // remove dynamic field
    mainBox.removeField(dynField);
    Assert.assertEquals(2, mainBox.getFields().size());
  }

  class MainBox extends AbstractGroupBox {

    public StaticStringField getStaticStringField() {
      return getFieldByClass(StaticStringField.class);
    }

    public CancelButton getCancelButton() {
      return getFieldByClass(CancelButton.class);
    }

    @Order(0)
    @ClassId("0bf5f199-75b7-48bf-946d-97b84b81fc8f")
    public class StaticStringField extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredMaxLength() {
        return 128;
      }
    }

    @Order(1000)
    @ClassId("3410c2b6-ffa1-4d8a-b54b-ce4c7d18c6a8")
    public class CancelButton extends AbstractCancelButton {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }
  }

  class DynamicGroupBox extends AbstractGroupBox {
    @Override
    protected String getConfiguredLabel() {
      return getClass().getSimpleName();
    }

    public Field01Field getField01Field() {
      return getFieldByClass(Field01Field.class);
    }

    public Field02Field getField02Field() {
      return getFieldByClass(Field02Field.class);
    }

    @Order(1000)
    @ClassId("4a275fea-26e4-4af9-953c-25fe7f32777c")
    public class Field01Field extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredMaxLength() {
        return 128;
      }
    }

    @Order(2000)
    @ClassId("d2243d6f-b05f-414c-a30e-15cca507c9ef")
    public class Field02Field extends AbstractStringField {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }

      @Override
      protected int getConfiguredMaxLength() {
        return 128;
      }

      @Override
      protected int getConfiguredGridH() {
        return 3;
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }
    }
  }
}
