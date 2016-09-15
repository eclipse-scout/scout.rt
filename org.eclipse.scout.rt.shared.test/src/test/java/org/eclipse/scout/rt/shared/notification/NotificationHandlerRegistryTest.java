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
package org.eclipse.scout.rt.shared.notification;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for {@link NotificationHandlerRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class NotificationHandlerRegistryTest {

  //notification handler for all Serializables
  @BeanMock
  private GlobalNotificationHandler m_globalNotificationHanlder;
  //notification handler for all INotificationGroup
  @BeanMock
  private GroupNotificationHandler m_groupNotificationHanlder;

  @Before
  public void before() throws Exception {
    // ensure bean hander cache of notification dispatcher gets refreshed
    ensureHandlerRegistryRefreshed();
  }

  @After
  public void after() {
    ensureHandlerRegistryRefreshed();
  }

  private void ensureHandlerRegistryRefreshed() {
    IBeanManager beanManager = BEANS.getBeanManager();
    IBean<NotificationHandlerRegistry> bean = beanManager.getBean(NotificationHandlerRegistry.class);
    beanManager.unregisterBean(bean);
    beanManager.registerBean(new BeanMetaData(bean));
  }

  /**
   * Tests that a notification of type {@link String} is only handled by handlers for Strings.
   **/
  @Test
  public void testStringNotification() {
    NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
    reg.notifyNotificationHandlers("A simple string notification");
    Mockito.verify(m_globalNotificationHanlder, Mockito.times(1)).handleNotification(Mockito.any(Serializable.class));
    Mockito.verify(m_groupNotificationHanlder, Mockito.times(0)).handleNotification(Mockito.any(INotificationGroup.class));
  }

  /**
   * Tests that a notification of type {@link INotificationGroup} is only handled by handlers for INotificationGroups.
   **/
  @Test
  public void testNotificationGroup() {
    NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
    reg.notifyNotificationHandlers(new Notification01());
    reg.notifyNotificationHandlers(new Notification01());
    Mockito.verify(m_globalNotificationHanlder, Mockito.times(2)).handleNotification(Mockito.any(Serializable.class));
    Mockito.verify(m_groupNotificationHanlder, Mockito.times(2)).handleNotification(Mockito.any(INotificationGroup.class));
  }

  private static class GlobalNotificationHandler implements INotificationHandler<Serializable> {

    @Override
    public void handleNotification(Serializable notification) {
    }
  }

  private static class GroupNotificationHandler implements INotificationHandler<INotificationGroup> {

    @Override
    public void handleNotification(INotificationGroup notification) {
    }
  }

  public static interface INotificationGroup extends Serializable {

  }

  private static final class Notification01 implements INotificationGroup {

    private static final long serialVersionUID = 1L;
  }
}
