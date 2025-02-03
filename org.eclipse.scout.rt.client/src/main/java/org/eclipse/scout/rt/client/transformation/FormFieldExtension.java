/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDisposeFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;

public class FormFieldExtension extends AbstractFormFieldExtension<AbstractFormField> {

  public FormFieldExtension(AbstractFormField ownerField) {
    super(ownerField);
  }

  @Override
  public void execInitField(FormFieldInitFieldChain chain) {
    super.execInitField(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformFormField(getOwner());
  }

  @Override
  public void execDisposeField(FormFieldDisposeFieldChain chain) {
    super.execDisposeField(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().notifyFieldDisposed(getOwner());
  }
}
