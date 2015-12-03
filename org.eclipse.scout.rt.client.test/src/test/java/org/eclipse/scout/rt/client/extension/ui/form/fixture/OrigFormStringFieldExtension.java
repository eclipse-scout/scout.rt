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
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldExecValidateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public class OrigFormStringFieldExtension extends AbstractStringFieldExtension<AbstractStringField> {

  public OrigFormStringFieldExtension(AbstractStringField owner) {
    super(owner);
  }

  @Override
  public String execValidateValue(ValueFieldExecValidateChain<String> chain, String rawValue) {
    String validatedValue = super.execValidateValue(chain, rawValue);
    ((OrigForm) getOwner().getForm()).logOperation(OrigFormStringFieldExtension.class, OrigForm.EXEC_VALIDATE_VALUE_OPERATION_NAME);
    return validatedValue;
  }
}
