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
}
