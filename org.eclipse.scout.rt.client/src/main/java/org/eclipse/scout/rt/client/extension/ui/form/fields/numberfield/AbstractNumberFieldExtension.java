/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractBasicFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;

public abstract class AbstractNumberFieldExtension<NUMBER extends Number, OWNER extends AbstractNumberField<NUMBER>> extends AbstractBasicFieldExtension<NUMBER, OWNER> implements INumberFieldExtension<NUMBER, OWNER> {

  public AbstractNumberFieldExtension(OWNER owner) {
    super(owner);
  }
}
