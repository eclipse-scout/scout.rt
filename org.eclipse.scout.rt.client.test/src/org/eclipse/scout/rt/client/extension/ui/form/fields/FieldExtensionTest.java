/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FieldExtensionTest.MyForm.MainBox.NameField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class FieldExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testFieldExtension() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(PrenameField.class, MyForm.MainBox.class);
    SERVICES.getService(IExtensionRegistry.class).register(FirstField.class, MyForm.MainBox.class);

    MyForm myForm = new MyForm();
    myForm.start();

    Assert.assertNotNull(myForm.getFieldByClass(PrenameField.class));
    List<IFormField> fields = myForm.getMainBox().getFields();
    Assert.assertEquals(3, fields.size());
    Assert.assertEquals(fields.get(0).getClass(), FirstField.class);
    Assert.assertEquals(fields.get(1).getClass(), NameField.class);
    Assert.assertEquals(fields.get(2).getClass(), PrenameField.class);
  }

  public static class MyForm extends AbstractForm {

    /**
     * @throws ProcessingException
     */
    public MyForm() throws ProcessingException {
      super();
    }

    public void start() throws ProcessingException {
      startInternal(new StartHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public NameField getNameField() {
      return getFieldByClass(NameField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class NameField extends AbstractStringField {

      }

    }

    public class StartHandler extends AbstractFormHandler {

    }
  }

  @Order(20)
  public static class PrenameField extends AbstractStringField {
    @Override
    protected String getConfiguredLabel() {
      return "Prename";
    }
  }

  @Order(5)
  public static class FirstField extends AbstractStringField {
    @Override
    protected String getConfiguredLabel() {
      return "First";
    }
  }
}
