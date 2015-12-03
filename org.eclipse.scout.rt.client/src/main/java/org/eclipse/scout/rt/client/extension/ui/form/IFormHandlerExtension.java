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
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IFormHandlerExtension<OWNER extends AbstractFormHandler> extends IExtension<OWNER> {

  void execPostLoad(FormHandlerPostLoadChain chain);

  boolean execValidate(FormHandlerValidateChain chain);

  void execLoad(FormHandlerLoadChain chain);

  void execStore(FormHandlerStoreChain chain);

  void execDiscard(FormHandlerDiscardChain chain);

  boolean execCheckFields(FormHandlerCheckFieldsChain chain);

  void execFinally(FormHandlerFinallyChain chain);

}
