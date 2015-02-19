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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldExecValidateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldFormatValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldParseValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldValidateValueChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;

/**
 *
 */
public abstract class AbstractValueFieldExtension<VALUE, OWNER extends AbstractValueField<VALUE>>
    extends AbstractFormFieldExtension<OWNER>
    implements IValueFieldExtension<VALUE, OWNER> {

  /**
   * @param owner
   */
  public AbstractValueFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public VALUE execValidateValue(ValueFieldExecValidateChain<VALUE> chain, VALUE rawValue) throws ProcessingException {
    VALUE retChain = chain.execValidateValue(rawValue);
    return retChain;
  }

  @Override
  public String execFormatValue(ValueFieldFormatValueChain<VALUE> chain, VALUE validValue) {
    return chain.execFormatValue(validValue);
  }

  @Override
  public VALUE execValidateValue(ValueFieldValidateValueChain<VALUE> chain, VALUE rawValue) throws ProcessingException {
    return chain.execValidateValue(rawValue);
  }

  @Override
  public void execChangedValue(ValueFieldChangedValueChain<VALUE> chain) throws ProcessingException {
    chain.execChangedValue();
  }

  @Override
  public VALUE execParseValue(ValueFieldParseValueChain<VALUE> chain, String text) throws ProcessingException {
    return chain.execParseValue(text);
  }
}
