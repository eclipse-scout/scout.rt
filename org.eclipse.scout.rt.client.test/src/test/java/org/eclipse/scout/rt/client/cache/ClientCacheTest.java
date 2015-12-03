/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.cache;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.notification.TypeParameterBeanRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ClientCacheTest {

  @Test
  public void testCacheNotificationHandlerRegistration() {
    TypeParameterBeanRegistry<INotificationHandler> registry = new TypeParameterBeanRegistry<>();
    registry.registerBeans(INotificationHandler.class, BEANS.all(INotificationHandler.class));
    List<INotificationHandler> handlers = registry.getBeans(InvalidateCacheNotification.class);

    // ensure that in client exactly one cache notification handler is registered and
    // that its type is CacheClientNotificationHandler
    Assert.assertEquals(handlers.size(), 1);
    Assert.assertTrue(handlers.get(0) instanceof CacheClientNotificationHandler);
  }
}
