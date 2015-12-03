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

import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public class TemplateStringFieldExtension extends AbstractStringFieldExtension<AbstractStringField> {

  public TemplateStringFieldExtension(AbstractStringField owner) {
    super(owner);
  }

  @Override
  public void execInitField(FormFieldInitFieldChain chain) {
    chain.execInitField();
    getOwner().setValue("A");
  }
}
