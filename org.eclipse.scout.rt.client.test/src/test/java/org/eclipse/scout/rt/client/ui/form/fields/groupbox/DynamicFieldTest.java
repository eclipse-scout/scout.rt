/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    @ClassId("e642b204-d0f1-4d42-baaf-32e350c419b5")
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
    @ClassId("ff2429f7-0a9f-441e-9c33-c8dd63972482")
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
    public void init() {
      super.init();
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
