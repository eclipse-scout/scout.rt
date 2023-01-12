/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldValidateValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public class OrigFormStringFieldExtension extends AbstractStringFieldExtension<AbstractStringField> {

  public OrigFormStringFieldExtension(AbstractStringField owner) {
    super(owner);
  }

  @Override
  public String execValidateValue(ValueFieldValidateValueChain<String> chain, String rawValue) {
    String validatedValue = super.execValidateValue(chain, rawValue);
    ((OrigForm) getOwner().getForm()).logOperation(OrigFormStringFieldExtension.class, OrigForm.EXEC_VALIDATE_VALUE_OPERATION_NAME);
    return validatedValue;
  }
}
