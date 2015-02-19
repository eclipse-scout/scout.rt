/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerCheckFieldsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerDiscardChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerFinallyChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerPostLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerStoreChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerValidateChain;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 *
 */
public interface IFormHandlerExtension<OWNER extends AbstractFormHandler> extends IExtension<OWNER> {

  void execPostLoad(FormHandlerPostLoadChain chain) throws ProcessingException;

  boolean execValidate(FormHandlerValidateChain chain) throws ProcessingException;

  void execLoad(FormHandlerLoadChain chain) throws ProcessingException;

  void execStore(FormHandlerStoreChain chain) throws ProcessingException;

  void execDiscard(FormHandlerDiscardChain chain) throws ProcessingException;

  boolean execCheckFields(FormHandlerCheckFieldsChain chain) throws ProcessingException;

  void execFinally(FormHandlerFinallyChain chain) throws ProcessingException;

}
