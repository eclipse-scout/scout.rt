package org.eclipse.scout.testing.client.servicetunnel.http;

import org.eclipse.scout.rt.client.IClientSession;

/**
 * HTTP cookie store implementation that distinguishes between different {@link IClientSession} connecting concurrently
 * to the same backend (i.e. same URL).
 * 
 * @deprecated use org.eclipse.scout.rt.client.MultiClientSessionCookieStore, will be removed with Scout 5.0
 */
@Deprecated
public class MultiClientSessionCookieStore extends org.eclipse.scout.rt.client.MultiClientSessionCookieStore {

}
