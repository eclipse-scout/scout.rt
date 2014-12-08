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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FieldExtensionTest.MyForm.MainBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FieldExtensionTest.PrenameField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class ExtendAbstractStringFieldTest extends AbstractLocalExtensionTestCase {

  private static AtomicInteger nameFieldExecInitFieldCounter = new AtomicInteger(0);
  private static AtomicInteger prenameFieldExecInitFieldCounter = new AtomicInteger(0);
  private static AtomicInteger allStringFieldExecInitFieldCounter = new AtomicInteger(0);

  @Test
  public void testOwnerFromGeneric() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(AllStringFieldExtension.class);
    // testcode
    MyForm form = new MyForm();
    form.start();
    Assert.assertEquals(1, nameFieldExecInitFieldCounter.get());
    Assert.assertEquals(1, prenameFieldExecInitFieldCounter.get());
    Assert.assertEquals(2, allStringFieldExecInitFieldCounter.get());
  }

  private static class MyForm extends AbstractForm {

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

    public PrenameField getPrenameField() {
      return getFieldByClass(PrenameField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class NameField extends AbstractStringField {
        @Override
        protected void execInitField() throws ProcessingException {
          nameFieldExecInitFieldCounter.incrementAndGet();
        }
      }

      @Order(20)
      public class PrenameField extends AbstractStringField {
        @Override
        protected void execInitField() throws ProcessingException {
          prenameFieldExecInitFieldCounter.incrementAndGet();
        }
      }

    }

    public class StartHandler extends AbstractFormHandler {

    }
  }

  public static class AllStringFieldExtension extends AbstractStringFieldExtension<AbstractStringField> {

    public AllStringFieldExtension(AbstractStringField owner) {
      super(owner);
    }

    @Override
    public void execInitField(FormFieldInitFieldChain chain) throws ProcessingException {
      allStringFieldExecInitFieldCounter.incrementAndGet();
      chain.execInitField();
    }
  }
}
