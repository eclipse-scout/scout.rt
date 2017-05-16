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
