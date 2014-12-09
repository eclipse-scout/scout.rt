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
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public class OrigFormStringFieldContribution extends AbstractStringField {

  @Override
  protected String execValidateValue(String rawValue) throws ProcessingException {
    String validatedValue = super.execValidateValue(rawValue);
    ((OrigForm) getForm()).logOperation(OrigFormStringFieldContribution.class, OrigForm.EXEC_VALIDATE_VALUE_OPERATION_NAME);
    return validatedValue;
  }
}
