/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AccordionFieldChains.AccordionFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AccordionFieldChains.AccordionFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.accordionfield.AbstractAccordionField;

public abstract class AbstractAccordionFieldExtension<T extends IAccordion, OWNER extends AbstractAccordionField<T>> extends AbstractFormFieldExtension<OWNER> implements IAccordionFieldExtension<T, OWNER> {

  public AbstractAccordionFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public TransferObject execDragRequest(AccordionFieldDragRequestChain chain) {
    return chain.execDragRequest();
  }

  @Override
  public void execDropRequest(AccordionFieldDropRequestChain chain, TransferObject transferObject) {
    chain.execDropRequest(transferObject);
  }
}
