/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;

/**
 * Represents a transport to connect to a messaging system like a network or broker, and which is used by the
 * application to send and receive messages.
 * <p>
 * The transport is a singleton (@{@link ApplicationScoped}).
 * <p>
 * To connect to the messaging system immediately upon platform startup (and detect misconfiguration in the
 * network/broker settings early), add @{@link CreateImmediately} to the transport.
 *
 * @see IMom
 * @since 6.1
 */
@ApplicationScoped
public interface IMomTransport extends IMom {

  /**
   * Returns <code>true</code> if no {@link IMomImplementor} is configured for this MOM or the configured implementor is
   * of type {@link NullMomImplementor}.
   * <p>
   * Unlike the other methods on this class, this method can be called <b>without</b> triggering the initialization of
   * the delegate.
   */
  boolean isNullTransport();
}
