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
package org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.AbstractService;

/**
 * Exception handler service used in JUnit test environments. The exceptions, if not already consumed, are wrapped into
 * a {@link WrappedProcessingRuntimeException}, rethrown and unpacked by the JUnit statement
 * {@link ProcessingRuntimeExceptionUnwrappingStatement}.
 */
public class WrappingProcessingRuntimeExceptionHandlerService extends AbstractService implements IExceptionHandlerService {

  @Override
  public void handleException(ProcessingException t) {
    if (!t.isConsumed()) {
      throw new WrappedProcessingRuntimeException(t);
    }
  }
}
