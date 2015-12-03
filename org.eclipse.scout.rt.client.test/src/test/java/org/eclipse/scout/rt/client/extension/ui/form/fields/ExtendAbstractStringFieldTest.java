/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FieldExtensionTest.MyForm.MainBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FieldExtensionTest.PrenameField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ExtendAbstractStringFieldTest extends AbstractLocalExtensionTestCase {

  private static AtomicInteger nameFieldExecInitFieldCounter = new AtomicInteger(0);
  private static AtomicInteger prenameFieldExecInitFieldCounter = new AtomicInteger(0);
  private static AtomicInteger allStringFieldExecInitFieldCounter = new AtomicInteger(0);

  @Test
  public void testOwnerFromGeneric() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(AllStringFieldExtension.class);
    // testcode
    MyForm form = new MyForm();
    form.start();
    Assert.assertEquals(1, nameFieldExecInitFieldCounter.get());
    Assert.assertEquals(1, prenameFieldExecInitFieldCounter.get());
    Assert.assertEquals(2, allStringFieldExecInitFieldCounter.get());
  }

  private static class MyForm extends AbstractForm {

    public MyForm() {
      super();
    }

    @Override
    public void start() {
      startInternal(new StartHandler());
    }

    @SuppressWarnings("unused")
    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @SuppressWarnings("unused")
    public NameField getNameField() {
      return getFieldByClass(NameField.class);
    }

    @SuppressWarnings("unused")
    public PrenameField getPrenameField() {
      return getFieldByClass(PrenameField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class NameField extends AbstractStringField {
        @Override
        protected void execInitField() {
          nameFieldExecInitFieldCounter.incrementAndGet();
        }
      }

      @Order(20)
      public class PrenameField extends AbstractStringField {
        @Override
        protected void execInitField() {
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
    public void execInitField(FormFieldInitFieldChain chain) {
      allStringFieldExecInitFieldCounter.incrementAndGet();
      chain.execInitField();
    }
  }
}
