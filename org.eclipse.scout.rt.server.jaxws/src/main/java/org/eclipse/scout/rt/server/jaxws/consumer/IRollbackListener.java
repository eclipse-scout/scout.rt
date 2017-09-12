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
package org.eclipse.scout.rt.server.jaxws.consumer;

/**
 * Listener to participate in <code>2-phase-commit-protocol (2PC)</code>.
 *
 * @since 5.1
 */
@FunctionalInterface
public interface IRollbackListener {

  /**
   * Invoked to rollback any operation done on behalf of the current transaction.
   */
  void onRollback();
}
