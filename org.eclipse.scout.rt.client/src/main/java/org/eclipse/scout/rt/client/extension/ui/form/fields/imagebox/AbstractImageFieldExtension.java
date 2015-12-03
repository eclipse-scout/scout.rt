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
