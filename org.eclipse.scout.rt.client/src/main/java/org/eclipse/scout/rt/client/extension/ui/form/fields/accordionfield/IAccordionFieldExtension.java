/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
