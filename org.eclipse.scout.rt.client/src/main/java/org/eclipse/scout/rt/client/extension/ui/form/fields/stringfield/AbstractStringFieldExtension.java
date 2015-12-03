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
package org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractBasicFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldDropRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldLinkActionChain;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

public abstract class AbstractStringFieldExtension<OWNER extends AbstractStringField> extends AbstractBasicFieldExtension<String, OWNER> implements IStringFieldExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractStringFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execDropRequest(StringFieldDropRequestChain chain, TransferObject transferObject) {
    chain.execDropRequest(transferObject);
  }

  @Override
  public void execAction(StringFieldLinkActionChain chain) {
    chain.execAction();
  }

  @Override
  public TransferObject execDragRequest(StringFieldDragRequestChain chain) {
    return chain.execDragRequest();
  }

}
