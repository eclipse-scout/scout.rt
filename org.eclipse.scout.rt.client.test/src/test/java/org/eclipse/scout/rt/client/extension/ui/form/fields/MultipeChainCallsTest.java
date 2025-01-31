/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldValidateValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.junit.Assert;
import org.junit.Test;

public class MultipeChainCallsTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testMultipeChainCalls() {
    BEANS.get(IExtensionRegistry.class).register(NameFieldExt.class);

    NameField field = new NameField();
    field.getUIFacade().parseAndSetValueFromUI("abc");
    Assert.assertNull(field.getErrorStatus());
    Assert.assertEquals(3, nameFieldExecValidateCounter.intValue());
    Assert.assertEquals(1, nameFieldExtExecValidateCounter.intValue());
  }

  private static final AtomicInteger nameFieldExecValidateCounter = new AtomicInteger(0);

  public static class NameField extends AbstractStringField {
    @Override
    protected String execValidateValue(String rawValue) {
      nameFieldExecValidateCounter.incrementAndGet();
      return super.execValidateValue(rawValue);
    }
  }

  private static final AtomicInteger nameFieldExtExecValidateCounter = new AtomicInteger(0);

  public static class NameFieldExt extends AbstractStringFieldExtension<NameField> {

    public NameFieldExt(NameField owner) {
      super(owner);
    }

    @Override
    public String execValidateValue(ValueFieldValidateValueChain<String> chain, String rawValue) {
      nameFieldExtExecValidateCounter.incrementAndGet();
      String chainVal01 = chain.execValidateValue(rawValue);
      String chainVal02 = chain.execValidateValue(rawValue);
      Assert.assertEquals(chainVal01, chainVal02);
      return super.execValidateValue(chain, rawValue);
    }
  }
}
