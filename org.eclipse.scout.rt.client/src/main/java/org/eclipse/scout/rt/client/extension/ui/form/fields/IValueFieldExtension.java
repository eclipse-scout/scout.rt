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

import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldFormatValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldParseValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldValidateValueChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;

public interface IValueFieldExtension<VALUE, OWNER extends AbstractValueField<VALUE>> extends IFormFieldExtension<OWNER> {

  VALUE execValidateValue(ValueFieldValidateValueChain<VALUE> chain, VALUE rawValue);

  String execFormatValue(ValueFieldFormatValueChain<VALUE> chain, VALUE value);

  void execChangedValue(ValueFieldChangedValueChain<VALUE> chain);

  VALUE execParseValue(ValueFieldParseValueChain<VALUE> chain, String text);
}
