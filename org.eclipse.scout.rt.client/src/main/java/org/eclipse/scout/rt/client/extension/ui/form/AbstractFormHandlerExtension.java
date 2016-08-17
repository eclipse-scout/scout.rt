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
package org.eclipse.scout.rt.client.extension.ui.form;

import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerCheckFieldsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerDiscardChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerFinallyChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerPostLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerStoreChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerValidateChain;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.platform.extension.InheritOuterExtensionScope;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

@InheritOuterExtensionScope(false)
public abstract class AbstractFormHandlerExtension<OWNER extends AbstractFormHandler> extends AbstractExtension<OWNER> implements IFormHandlerExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractFormHandlerExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPostLoad(FormHandlerPostLoadChain chain) {
    chain.execPostLoad();
  }

  @Override
  public boolean execValidate(FormHandlerValidateChain chain) {
    return chain.execValidate();
  }

  @Override
  public void execLoad(FormHandlerLoadChain chain) {
    chain.execLoad();
  }

  @Override
  public void execStore(FormHandlerStoreChain chain) {
    chain.execStore();
  }

  @Override
  public void execDiscard(FormHandlerDiscardChain chain) {
    chain.execDiscard();
  }

  @Override
  public boolean execCheckFields(FormHandlerCheckFieldsChain chain) {
    return chain.execCheckFields();
  }

  @Override
  public void execFinally(FormHandlerFinallyChain chain) {
    chain.execFinally();
  }

}
