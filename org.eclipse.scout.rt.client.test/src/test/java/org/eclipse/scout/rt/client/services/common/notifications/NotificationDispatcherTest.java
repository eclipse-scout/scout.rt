/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.notifications;

import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@RunWithSubject("anna")
public class NotificationDispatcherTest {

  private List<IBean<?>> m_serviceReg;
  private volatile GlobalNotificationHandler m_globalNotificationHanlder;
  private volatile GroupNotificationHandler m_groupNotificationHanlder;

  @Before
  public void before() {
    m_globalNotificationHanlder = mock(GlobalNotificationHandler.class);
    m_groupNotificationHanlder = mock(GroupNotificationHandler.class);
    m_serviceReg = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(GlobalNotificationHandler.class).withInitialInstance(m_globalNotificationHanlder).withApplicationScoped(true),
        new BeanMetaData(GroupNotificationHandler.class).withInitialInstance(m_groupNotificationHanlder).withApplicationScoped(true));

    // ensure bean hander cache of notification dispatcher gets refreshed
    IBeanManager beanManager = BEANS.getBeanManager();
    IBean<NotificationHandlerRegistry> bean = beanManager.getBean(NotificationHandlerRegistry.class);
    beanManager.unregisterBean(bean);
    beanManager.registerClass(NotificationHandlerRegistry.class);
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_serviceReg);
    // ensure bean hander cache of notification dispatcher gets refreshed
    IBeanManager beanManager = BEANS.getBeanManager();
    IBean<NotificationHandlerRegistry> bean = beanManager.getBean(NotificationHandlerRegistry.class);
    beanManager.unregisterBean(bean);
    beanManager.registerClass(NotificationHandlerRegistry.class);
  }

  @Test
  public void testStringNotification() {
    final IBlockingCondition cond = Jobs.newBlockingCondition(true);
    final String stringNotification = "A simple string notification";

    Jobs.schedule(() -> {
      final ClientNotificationDispatcher dispatcher = BEANS.get(ClientNotificationDispatcher.class);
      dispatcher.dispatchForSession((IClientSession) IClientSession.CURRENT.get(), stringNotification, mock(ClientNotificationAddress.class));
      waitForPendingNotifications(dispatcher);
    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent()))
        .whenDone(event -> cond.setBlocking(false), null);
    cond.waitFor();
    Mockito.verify(m_globalNotificationHanlder, Mockito.times(1)).handleNotification(Mockito.any(Serializable.class));
    Mockito.verify(m_groupNotificationHanlder, Mockito.times(0)).handleNotification(Mockito.any(INotificationGroup.class));
  }

  @Test
  public void testSuperClassNotification() {
    final IBlockingCondition cond = Jobs.newBlockingCondition(true);

    Jobs.schedule(() -> {
      ClientNotificationDispatcher dispatcher = BEANS.get(ClientNotificationDispatcher.class);
      dispatcher.dispatchForSession((IClientSession) IClientSession.CURRENT.get(), new Notification01(), mock(ClientNotificationAddress.class));
      dispatcher.dispatchForSession((IClientSession) IClientSession.CURRENT.get(), new Notification02(), mock(ClientNotificationAddress.class));
      dispatcher.dispatchForSession((IClientSession) IClientSession.CURRENT.get(), new Notification02(), mock(ClientNotificationAddress.class));
      waitForPendingNotifications(dispatcher);
    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent()))
        .whenDone(event -> cond.setBlocking(false), null);
    cond.waitFor();
    Mockito.verify(m_globalNotificationHanlder, Mockito.times(3)).handleNotification(Mockito.any(Serializable.class));
    Mockito.verify(m_groupNotificationHanlder, Mockito.times(2)).handleNotification(Mockito.any(INotificationGroup.class));
  }

  /**
   * This method should only be used for debugging or test reasons. It waits for all notification jobs to be executed.
   */
  private void waitForPendingNotifications(ClientNotificationDispatcher dispatcher) {
    final Set<IFuture<?>> futures = dispatcher.getPendingNotifications();
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(futures)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter(), Integer.MAX_VALUE, TimeUnit.SECONDS);
  }

  @IgnoreBean
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

  private static final class Notification01 implements Serializable {

    private static final long serialVersionUID = 1L;
  }

  public static interface INotificationGroup extends Serializable {

  }

  private static final class Notification02 implements INotificationGroup {

    private static final long serialVersionUID = 1L;
  }
}
