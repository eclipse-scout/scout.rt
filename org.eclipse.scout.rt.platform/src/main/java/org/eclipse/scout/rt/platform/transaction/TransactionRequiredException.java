/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.transaction;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * A <code>TransactionRequiredException</code> is thrown if a {@link ServerRunContext} requires a transaction to be
 * available.
 *
 * @see TransactionScope#MANDATORY
 * @since 5.1
 */
public class TransactionRequiredException extends PlatformException {

  private static final long serialVersionUID = 1L;

  public TransactionRequiredException() {
    super("Transaction expected, but no transaction is available");
  }
}
