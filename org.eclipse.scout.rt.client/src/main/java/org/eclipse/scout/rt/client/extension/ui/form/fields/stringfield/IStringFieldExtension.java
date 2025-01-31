/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IBasicFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldDropRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldLinkActionChain;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public interface IStringFieldExtension<OWNER extends AbstractStringField> extends IBasicFieldExtension<String, OWNER> {

  void execDropRequest(StringFieldDropRequestChain chain, TransferObject transferObject);

  void execAction(StringFieldLinkActionChain chain);

  TransferObject execDragRequest(StringFieldDragRequestChain chain);
}
