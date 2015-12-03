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

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ExecMethodExtensionTest.MyForm.MainBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldExecValidateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;
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
public class ExecMethodExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExecValidate() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(NameFieldExtension.class, NameField.class);

    MyForm myForm = new MyForm();
    myForm.start();
    NameField nameField = myForm.getNameField();
    nameField.getUIFacade().parseAndSetValueFromUI("hans");
    Assert.assertNull(nameField.getErrorStatus());

    nameField.getUIFacade().parseAndSetValueFromUI("bernd");
    Assert.assertNotNull(nameField.getErrorStatus());

    nameField.getUIFacade().parseAndSetValueFromUI("berndExtension");
    Assert.assertNotNull(nameField.getErrorStatus());

  }

  public class MyForm extends AbstractForm {

    public MyForm() {
      super();
    }

    @Override
    public void start() {
      startInternal(new StartHandler());
    }

    public NameField getNameField() {
      return getFieldByClass(NameField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class NameField extends AbstractStringField {
        @Override
        protected String execValidateValue(String rawValue) {
          if (StringUtility.equalsIgnoreCase("Bernd", rawValue)) {
            throw new VetoException("Bernd is not allowed");
          }
          return super.execValidateValue(rawValue);
        }
      }

    }

    public class StartHandler extends AbstractFormHandler {

    }
  }

  public static class NameFieldExtension extends AbstractStringFieldExtension<NameField> {

    /**
     * @param owner
     */
    public NameFieldExtension(NameField owner) {
      super(owner);
    }

    @Override
    public String execValidateValue(ValueFieldExecValidateChain<String> chain, String rawValue) {
      String retVal = chain.execValidateValue(rawValue);
      if (StringUtility.equalsIgnoreCase("berndExtension", retVal)) {
        throw new VetoException("BerndExtension is not allowed");
      }
      return retVal;
    }

  }
}
