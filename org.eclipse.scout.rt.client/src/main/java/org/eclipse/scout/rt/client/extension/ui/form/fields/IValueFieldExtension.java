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
public interface IValueFieldExtension<VALUE, OWNER extends AbstractValueField<VALUE>> extends IFormFieldExtension<OWNER> {

  /**
   * @param chain
   * @param rawValue
   * @return
   * @throws ProcessingException
   */
  VALUE execValidateValue(ValueFieldExecValidateChain<VALUE> chain, VALUE rawValue) throws ProcessingException;

  String execFormatValue(ValueFieldFormatValueChain<VALUE> chain, VALUE validValue);

  VALUE execValidateValue(ValueFieldValidateValueChain<VALUE> chain, VALUE rawValue) throws ProcessingException;

  void execChangedValue(ValueFieldChangedValueChain<VALUE> chain) throws ProcessingException;

  VALUE execParseValue(ValueFieldParseValueChain<VALUE> chain, String text) throws ProcessingException;

}
