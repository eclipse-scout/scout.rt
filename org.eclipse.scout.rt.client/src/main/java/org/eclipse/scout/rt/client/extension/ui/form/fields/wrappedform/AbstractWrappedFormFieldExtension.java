/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

public abstract class AbstractWrappedFormFieldExtension<T extends IForm, OWNER extends AbstractWrappedFormField<T>> extends AbstractFormFieldExtension<OWNER> implements IWrappedFormFieldExtension<T, OWNER> {

  public AbstractWrappedFormFieldExtension(OWNER owner) {
    super(owner);
  }
}
