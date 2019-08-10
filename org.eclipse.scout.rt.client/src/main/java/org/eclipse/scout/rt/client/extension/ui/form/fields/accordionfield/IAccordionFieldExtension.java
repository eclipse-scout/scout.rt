/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AccordionFieldChains.AccordionFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AccordionFieldChains.AccordionFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.accordionfield.AbstractAccordionField;

public interface IAccordionFieldExtension<T extends IAccordion, OWNER extends AbstractAccordionField<T>> extends IFormFieldExtension<OWNER> {

  TransferObject execDragRequest(AccordionFieldDragRequestChain chain);

  void execDropRequest(AccordionFieldDropRequestChain chain, TransferObject transferObject);
}
