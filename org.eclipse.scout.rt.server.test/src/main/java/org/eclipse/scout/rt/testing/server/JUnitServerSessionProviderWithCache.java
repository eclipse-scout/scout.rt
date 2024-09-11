/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;

/**
 * Like {@link ServerSessionProviderWithCache}, but additionally adds the session class to the cache key.
 * <p>
 * That is because JUnit tests can be configured to run with another session via {@link RunWithServerSession}.
 *
 * @see RunWithServerSession
 */
@Replace
public class JUnitServerSessionProviderWithCache extends ServerSessionProviderWithCache {

  @Override
  protected CompositeObject newSessionCacheKey(final String sessionId, final Subject subject) {
    if (sessionId == null && subject == null) {
      return null;
    }
    final Object[] superComponents = super.newSessionCacheKey(sessionId, subject).getComponents();
    if (superComponents == null) {
      return null;
    }

    // Make the session part of the cache key.
    final Object[] components = new Object[superComponents.length + 1];
    System.arraycopy(superComponents, 0, components, 1, superComponents.length);
    components[0] = BEANS.get(IServerSession.class).getClass();

    return new CompositeObject(components);
  }
}
