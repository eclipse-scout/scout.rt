/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.exceptionhandler;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.internal.InternalClientExceptionHandlerService;

@Priority(-1)
public class ClientExceptionHandlerService extends InternalClientExceptionHandlerService {

  @Override
  public void handleException(ProcessingException pe) {
    super.handleException(pe);
  }

  @Override
  protected void showExceptionInUI(ProcessingException pe) {
    super.showExceptionInUI(pe);
  }
}
