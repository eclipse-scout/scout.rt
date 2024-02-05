/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.internal;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.WidgetEvent;
import org.eclipse.scout.rt.client.ui.WidgetListener;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeManager;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeListener;
import org.eclipse.scout.rt.client.ui.desktop.internal.VirtualDesktopTest.VirtualDesktopForwardTestSession;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.util.ChangeStatus;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that accessing the Desktop using IDesktop.CURRENT.get() in an async task that was scheduled while the Desktop
 * on the Session was still the VirtualDesktop (e.g. if scheduled during Desktop creation):
 * <ol>
 * <li>Is called as it was scheduled: with the VirtualDesktop on the RunContext</li>
 * <li>This virtual Desktop forwards all calls to the real Desktop which has been created in the meantime.</li>
 * </ol>
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(VirtualDesktopForwardTestSession.class)
public class VirtualDesktopTest {

  @Test
  public void testRealDesktopUsedAsSoonAsAvailable() throws Throwable {
    VirtualDesktopForwardTestSession session = ClientSessionProvider.currentSession(VirtualDesktopForwardTestSession.class);
    assertTrue(session.m_asyncJobFinished.await(10, TimeUnit.SECONDS));
    if (session.m_exFromAsyncInDesktopConstructor != null) {
      throw session.m_exFromAsyncInDesktopConstructor;
    }
    if (session.m_exFromAsyncInExecLoad != null) {
      throw session.m_exFromAsyncInExecLoad;
    }
    if (session.m_exFromAsyncAfterDesktopSet1 != null) {
      throw session.m_exFromAsyncAfterDesktopSet1;
    }
    if (session.m_exFromAsyncAfterDesktopSet2 != null) {
      throw session.m_exFromAsyncAfterDesktopSet2;
    }
    AbstractDesktop realDesktop = (AbstractDesktop) session.getDesktop();
    int numListenerExpected = 12;
    assertEquals(numListenerExpected, realDesktop
        .desktopListeners()
        .list(DesktopEvent.TYPE_DESKTOP_CLOSED).stream()
        .filter(l -> l instanceof VirtualDesktopForwardTestDesktopListener).count());
    assertEquals(numListenerExpected, session.m_realDesktopPropertySupport
        .getPropertyChangeListeners().stream()
        .filter(l -> l instanceof VirtualDesktopForwardTestPropertyChangeListener).count());
    DataChangeManager dataChangeManager = (DataChangeManager) realDesktop.dataChangeListeners();
    assertEquals(numListenerExpected, dataChangeManager
        .listAll().entrySet().stream()
        .flatMap(e -> e.getValue().stream())
        .filter(l -> l instanceof VirtualDesktopForwardTestDataChangeListener)
        .distinct().count());
    DataChangeManager dataChangeDesktopInForegroundListeners = (DataChangeManager) realDesktop.dataChangeDesktopInForegroundListeners();
    assertEquals(numListenerExpected, dataChangeDesktopInForegroundListeners
        .listAll().entrySet().stream()
        .flatMap(e -> e.getValue().stream())
        .filter(l -> l instanceof VirtualDesktopForwardTestDataChangeListener)
        .distinct().count());
    assertEquals(numListenerExpected, realDesktop
        .widgetListeners()
        .list(WidgetEvent.TYPE_SCROLL_TO_TOP).stream()
        .filter(l -> l instanceof VirtualDesktopForwardTestWidgetListener).count());
    assertEquals(List.of("virtualListener:event1", "realListener:event1", "virtualListener:event2", "realListener:event2", "virtualListener:event3", "realListener:event3", "virtualListener:event4", "realListener:event4"),
        session.m_dataChangeEvents);
  }

  public static class VirtualDesktopForwardTestSession extends AbstractClientSession {

    private final CountDownLatch m_asyncJobFinished = new CountDownLatch(5);
    private Throwable m_exFromAsyncInDesktopConstructor;
    private Throwable m_exFromAsyncInExecLoad;
    private Throwable m_exFromAsyncAfterDesktopSet1;
    private Throwable m_exFromAsyncAfterDesktopSet2;
    private BasicPropertySupport m_realDesktopPropertySupport;
    private List<String> m_dataChangeEvents = new ArrayList<>();

    public VirtualDesktopForwardTestSession() {
      super(true);
    }

    @Override
    protected void execLoadSession() {
      IDesktop virtualDesktop = getDesktopElseVirtualDesktop();
      assertTrue(virtualDesktop instanceof VirtualDesktop);
      Throwable t = execWithCurrentDesktop(virtualDesktop, virtualDesktop, false /* cannot succeed here*/);
      if (t != null) {
        throw new ProcessingException("Error in execLoad", t);
      }
      virtualDesktop.dataChanged("event1");
      ModelJobs.schedule(() -> {
        m_exFromAsyncInExecLoad = execWithCurrentDesktop(getDesktop(), virtualDesktop, true);
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
      virtualDesktop.addDataChangeListener(event -> m_dataChangeEvents.add("virtualListener:" + event.getDataType().toString()));

      IDesktop realDesktop = new AbstractDesktop() {
        /**
         * executed when the desktop is created (constructor)
         */
        @Override
        protected void initConfigInternal() {
          super.initConfigInternal();
          addDataChangeListener(event -> m_dataChangeEvents.add("realListener:" + event.getDataType().toString()));
        }

        /**
         * Executed when the desktop is initialized (init is call in AbstractClientSession.setDesktop)
         */
        @Override
        protected void initInternal() {
          m_realDesktopPropertySupport = propertySupport;
          ModelJobs.schedule(() -> {
            m_exFromAsyncInDesktopConstructor = execWithCurrentDesktop(this, virtualDesktop, true);
          }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
          super.initInternal();
        }
      };

      virtualDesktop.dataChanged("event2");
      ModelJobs.schedule(() -> {
        m_exFromAsyncInExecLoad = execWithCurrentDesktop(realDesktop, virtualDesktop, true);
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));

      virtualDesktop.fireDataChangeEvent(new DataChangeEvent("event3", ChangeStatus.UPDATED));
      setDesktop(realDesktop);
      virtualDesktop.dataChanged("event4");

      ModelJobs.schedule(() -> {
        m_exFromAsyncAfterDesktopSet1 = execWithCurrentDesktop(realDesktop, virtualDesktop /* virtual because here the current thread-local is copied */, true);
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
      ModelJobs.schedule(() -> {
        m_exFromAsyncAfterDesktopSet2 = execWithCurrentDesktop(realDesktop, realDesktop /* real desktop as withSession() re-applies the values from the session and overwrites the ones from the current thread-local */, true);
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent().withSession(this, true)));
    }

    private Throwable execWithCurrentDesktop(IDesktop expectedDesktopOnSession, IDesktop expectedDesktopOnThreadLocal, boolean callMethodOnDesktop) {
      try {
        IDesktop desktopFromSession = ClientSessionProvider.currentSession().getDesktopElseVirtualDesktop();
        assertSame(expectedDesktopOnSession, desktopFromSession);
        if (callMethodOnDesktop) {
          desktopFromSession.dataChanged(); // would throw if the real desktop is not already known to the virtual one
        }
        desktopFromSession.desktopListeners().add(new VirtualDesktopForwardTestDesktopListener(), false, DesktopEvent.TYPE_DESKTOP_CLOSED);
        desktopFromSession.addPropertyChangeListener(new VirtualDesktopForwardTestPropertyChangeListener());
        desktopFromSession.dataChangeListeners().add(new VirtualDesktopForwardTestDataChangeListener(), false);
        desktopFromSession.dataChangeDesktopInForegroundListeners().add(new VirtualDesktopForwardTestDataChangeListener(), false);
        desktopFromSession.widgetListeners().add(new VirtualDesktopForwardTestWidgetListener(), false, WidgetEvent.TYPE_SCROLL_TO_TOP);

        IDesktop desktopFromThreadLocal = IDesktop.CURRENT.get();
        assertSame(expectedDesktopOnThreadLocal, desktopFromThreadLocal);
        if (callMethodOnDesktop) {
          desktopFromThreadLocal.dataChanged(); // would throw if the real desktop is not already known to the virtual one
        }
        desktopFromThreadLocal.desktopListeners().add(new VirtualDesktopForwardTestDesktopListener(), false, DesktopEvent.TYPE_DESKTOP_CLOSED);
        desktopFromThreadLocal.addPropertyChangeListener(new VirtualDesktopForwardTestPropertyChangeListener());
        desktopFromThreadLocal.dataChangeListeners().add(new VirtualDesktopForwardTestDataChangeListener(), false);
        desktopFromThreadLocal.dataChangeDesktopInForegroundListeners().add(new VirtualDesktopForwardTestDataChangeListener(), false);
        desktopFromThreadLocal.widgetListeners().add(new VirtualDesktopForwardTestWidgetListener(), false, WidgetEvent.TYPE_SCROLL_TO_TOP);
      }
      catch (Throwable e) {
        return e;
      }
      finally {
        m_asyncJobFinished.countDown();
      }
      return null;
    }
  }

  private static class VirtualDesktopForwardTestWidgetListener implements WidgetListener {
    @Override
    public void widgetChanged(WidgetEvent event) {
    }
  }

  private static class VirtualDesktopForwardTestDataChangeListener implements IDataChangeListener {
    @Override
    public void dataChanged(DataChangeEvent event) {
    }
  }

  private static class VirtualDesktopForwardTestPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }
  }

  private static class VirtualDesktopForwardTestDesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(DesktopEvent e) {
    }
  }
}
