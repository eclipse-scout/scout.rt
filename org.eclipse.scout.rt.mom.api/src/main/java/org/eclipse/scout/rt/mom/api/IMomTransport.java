package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Marker interface for a {@link IMom} initialized with a transport to connect to a messaging system like a network or
 * broker, and which is used by the application to send and receive messages.
 *
 * @see IMom
 * @since 6.1
 */
@ApplicationScoped
public interface IMomTransport {
}
