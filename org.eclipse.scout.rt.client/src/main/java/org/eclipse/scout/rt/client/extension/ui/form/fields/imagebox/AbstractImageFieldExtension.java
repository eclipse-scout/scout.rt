/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;

public abstract class AbstractImageFieldExtension<OWNER extends AbstractImageField> extends AbstractFormFieldExtension<OWNER> implements IImageFieldExtension<OWNER> {

  public AbstractImageFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public TransferObject execDragRequest(ImageFieldDragRequestChain chain) {
    return chain.execDragRequest();
  }

  @Override
  public void execDropRequest(ImageFieldDropRequestChain chain, TransferObject transferObject) {
    chain.execDropRequest(transferObject);
  }
}
