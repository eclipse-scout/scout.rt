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
public class DynamicFieldTest {

  @Test
  public void testAddField() {
    DynamicForm form = new DynamicForm("TestForm", new MainBox());
    form.start();
    DynamicStringField dynField = Mockito.spy(new DynamicStringField());
    dynField.setOrder(100);

    MainBox mainBox = form.getFieldByClass(MainBox.class);
    // add dynamic field
    mainBox.addField(dynField);
    Assert.assertTrue(form.isFormStarted());
    Assert.assertEquals(3, mainBox.getFields().size());
    Mockito.verify(dynField, Mockito.times(1)).init();
    Assert.assertEquals(mainBox, dynField.getParentField());
    Assert.assertEquals("StaticStringField,DynamicStringField,CancelButton", mainBox.getFields().stream()
        .map(f -> f.getLabel())
        .collect(Collectors.joining(",")));

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
    @ClassId("8b3d5608-e2fc-46ca-8776-18f9beab61e2")
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
    @ClassId("16ef7312-df19-4633-8c5d-43597ff51f7b")
    public class CancelButton extends AbstractCancelButton {
      @Override
      protected String getConfiguredLabel() {
        return getClass().getSimpleName();
      }
    }
  }

  class DynamicStringField extends AbstractStringField {

    @Override
    protected String getConfiguredLabel() {
      return getClass().getSimpleName();
    }

    @Override
    protected void initFieldInternal() {
      super.initFieldInternal();
      System.out.println("init");
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      System.out.println("initConfig");
    }
  }

  class DynamicGroupBox extends AbstractGroupBox {

  }
}
