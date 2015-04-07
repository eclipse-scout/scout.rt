/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Exception that wraps a {@link ProcessingException} into a {@link RuntimeException}.
 * TODO [dwi][abr]: to be removed once ProcessingException is a RuntimeException.
 */
public class ProcessingRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ProcessingRuntimeException(final ProcessingException cause) {
    super(cause);
  }
}
