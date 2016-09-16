package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;

/**
 * Marker interface for a {@link IMom} initialized with a transport to connect to a messaging system like a network or
 * broker, and which is used by the application to send and receive messages.
 * <p>
 * The transport is a singleton and connects to the messaging system immediately upon platform startup. That way, a
 * misconfiguration in the network/broker settings is detected early during platform startup.
 *
 * @see IMom
 * @since 6.1
 */
@ApplicationScoped
@CreateImmediately
public interface IMomTransport {
}
