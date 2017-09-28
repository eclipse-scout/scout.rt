/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.SubScopeExtensionTest.AbstractBoxTemplate.MyStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.SubScopeExtensionTest.ScopeTestForm.MainBox.MyButton;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.AbstractButtonExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <h3>{@link SubScopeExtensionTest}</h3>
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SubScopeExtensionTest {

  private static final int MAX_LEN = 99;

  @Test
  public void testScope() {
    BEANS.get(IExtensionRegistry.class).register(FirstExtension.class);
    BEANS.get(IExtensionRegistry.class).register(SecondExtension.class);

    ScopeTestForm f = new ScopeTestForm();
    f.initForm();
    MyStringField stringField = f.getRootGroupBox().getFieldByClass(MyStringField.class);
    Assert.assertNotNull(stringField);
    Assert.assertEquals(MAX_LEN, stringField.getMaxLength());
  }

  public static abstract class AbstractBoxTemplate extends AbstractGroupBox {
    @Order(1000)
    public class MyStringField extends AbstractStringField {
    }
  }

  public static class ScopeTestForm extends AbstractForm {
    public class MainBox extends AbstractGroupBox {

      @Order(1000)
      public class MyButton extends AbstractButton {
      }

      @Order(2000)
      public class MyTabBox extends AbstractTabBox {
        public class MyBox extends AbstractBoxTemplate {

        }
      }
    }
  }

  public static class FirstExtension extends AbstractFormExtension<ScopeTestForm> {

    public FirstExtension(ScopeTestForm ownerForm) {
      super(ownerForm);
    }

    public class ButtonExtension extends AbstractButtonExtension<MyButton> {
      public ButtonExtension(MyButton owner) {
        super(owner);
      }
    }
  }

  public static class SecondExtension extends AbstractGroupBoxExtension<AbstractBoxTemplate> {

    public SecondExtension(AbstractBoxTemplate owner) {
      super(owner);
    }

    public class StringFieldExtension extends AbstractStringFieldExtension<MyStringField> {
      public StringFieldExtension(MyStringField owner) {
        super(owner);
      }

      @Override
      public void execInitField(FormFieldInitFieldChain chain) {
        chain.execInitField();
        getOwner().setMaxLength(MAX_LEN);
      }
    }
  }
}
