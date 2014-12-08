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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ExecMethodExtensionTest.MyForm.MainBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldExecValidateChain;
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
public class ExecMethodExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExecValidate() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(NameFieldExtension.class, NameField.class);

    MyForm myForm = new MyForm();
    myForm.start();
    NameField nameField = myForm.getNameField();
    nameField.getUIFacade().setTextFromUI("hans", false);
    Assert.assertNull(nameField.getErrorStatus());

    nameField.getUIFacade().setTextFromUI("bernd", false);
    Assert.assertNotNull(nameField.getErrorStatus());

    nameField.getUIFacade().setTextFromUI("berndExtension", false);
    Assert.assertNotNull(nameField.getErrorStatus());

  }

  public class MyForm extends AbstractForm {

    /**
     * @throws ProcessingException
     */
    public MyForm() throws ProcessingException {
      super();
    }

    public void start() throws ProcessingException {
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
        protected String execValidateValue(String rawValue) throws ProcessingException {
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
    public String execValidateValue(ValueFieldExecValidateChain<String> chain, String rawValue) throws ProcessingException {
      String retVal = chain.execValidateValue(rawValue);
      if (StringUtility.equalsIgnoreCase("berndExtension", retVal)) {
        throw new VetoException("BerndExtension is not allowed");
      }
      return retVal;
    }

  }
}
