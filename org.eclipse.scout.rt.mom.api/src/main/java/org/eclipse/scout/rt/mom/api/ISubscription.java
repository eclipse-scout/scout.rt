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
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.util.IDisposable;

/**
 * Represents a subscription to a destination.
 * <p>
 * Use this object to unsubscribe from the destination.
 *
 * @see IMom
 * @since 6.1
 */
public interface ISubscription extends IDisposable {

  /**
   * Returns the destination this subscription belongs to.
   */
  IDestination<?> getDestination();
}
