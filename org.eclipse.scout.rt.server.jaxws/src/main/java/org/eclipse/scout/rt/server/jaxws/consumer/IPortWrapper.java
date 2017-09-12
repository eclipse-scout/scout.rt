/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
 * Interface used by classes that are wrapping a JAX-WS port instance.
 *
 * @since 6.0.300
 */
@FunctionalInterface
public interface IPortWrapper<PORT> {

  /**
   * @return Returns the wrapped port instance.
   */
  PORT getPort();
}
