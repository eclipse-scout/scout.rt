/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
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

}
