/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
