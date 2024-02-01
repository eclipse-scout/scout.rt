/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.clientnotification;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.NotificationSubjectProperty;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@CreateImmediately
public class ClientNotificationPoller {

  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationPoller.class);

  private final Object m_lock = new Object();
  private IFuture<Void> m_pollerFuture;
  private IFuture<Void> m_livenessFuture;
  private volatile long m_pollerStaleTimeMillis;
  private volatile long m_lastPollRequest;

  @PostConstruct
  public void start() {
    synchronized (m_lock) {
      // ensure the poller starts only once.
      Assertions.assertNull(m_pollerFuture);
      if (BEANS.get(IServiceTunnel.class).isActive()) {
        startPoller();
        startLivenessChecker();
      }
      else {
        LOG.debug("Starting without notifications due to no proxy service is available");
      }
    }
  }

  public void stop() {
    synchronized (m_lock) {
      stopLivenessChecker();
      stopPoller();
    }
  }

  protected void startPoller() {
    touch(); // reset timestamp of last poll request to eliminate timing issues (poller must run before liveness check)
    m_pollerFuture = Jobs.schedule(new P_NotificationPoller(this::touch), Jobs.newInput()
        .withRunContext(createRunContext())
        .withName(ClientNotificationPoller.class.getSimpleName()));
  }

  protected void stopPoller() {
    if (m_pollerFuture == null) {
      return;
    }

    LOG.debug("Stopping client notification poller [clientNodeId={}].", IIds.toString(NodeId.current()));
    m_pollerFuture.cancel(true);
    m_pollerFuture = null;
  }

  protected RunContext createRunContext() {
    return ClientRunContexts.empty()
        .withSubject(BEANS.get(NotificationSubjectProperty.class).getValue())
        .withUserAgent(UserAgents.createDefault())
        .withSession(null, false);
  }

  protected static void handleMessagesReceived(List<ClientNotificationMessage> notifications) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received {} notifications ({}) [clientNodeId={}].", notifications.size(), LOG.isTraceEnabled() ? notifications : "use level TRACE to see notifications", IIds.toString(NodeId.current()));
    }
    // process notifications
    if (!notifications.isEmpty()) {
      BEANS.get(ClientNotificationDispatcher.class).dispatchNotifications(notifications);
    }
    LOG.debug("Dispatched notifications [clientNodeId={}]", IIds.toString(NodeId.current()));
  }

  private static final class P_NotificationPoller implements IRunnable {

    private final Runnable m_livenessCheck;

    public P_NotificationPoller(Runnable livenessCheck) {
      m_livenessCheck = livenessCheck;
    }

    @Override
    public void run() {
      final RunMonitor outerRunMonitor = RunMonitor.CURRENT.get();
      while (!outerRunMonitor.isCancelled()) {
        m_livenessCheck.run();
        try {
          // use temporary new run monitor to avoid registering anonymous new cancellables with parent (current) run monitor
          // new local run monitor is registered with parent run monitor as cancellable, however it is unregistered from
          // parent after handling received messages in any case to avoid memory overusage
          final RunMonitor tempRunMonitor = BEANS.get(RunMonitor.class);
          RunContexts.copyCurrent()
              .withRunMonitor(tempRunMonitor)
              .withParentRunMonitor(outerRunMonitor)
              .run(() -> {
                try {
                  LOG.debug("Getting notifications from backend [clientNodeId={}]", IIds.toString(NodeId.current()));
                  handleMessagesReceived(BEANS.get(IClientNotificationService.class).getNotifications(NodeId.current()));
                }
                finally {
                  outerRunMonitor.unregisterCancellable(tempRunMonitor);
                }
              });
        }
        catch (ThreadInterruptedError | FutureCancelledError e) {
          LOG.debug("Client notification polling has been interrupted. [clientNodeId={}]", IIds.toString(NodeId.current()), e);
        }
        catch (RuntimeException e) {
          if (!(e instanceof PlatformException && ((PlatformException) e).isConsumed())) {
            LOG.error("Error receiving client notifications [clientNodeId={}]", IIds.toString(NodeId.current()), e);
          }
          SleepUtil.sleepSafe(10, TimeUnit.SECONDS); // sleep some time before connecting anew
        }
      }
      LOG.debug("Client notification polling has ended because the job was cancelled. [clientNodeId={}]", IIds.toString(NodeId.current()));
    }
  }

  // --- liveness check methods ------------------------------------------------

  protected void startLivenessChecker() {
    if (m_livenessFuture != null) {
      return;
    }

    final long pollerCheckIntervalMillis = Assertions.assertNotNull(CONFIG.getPropertyValue(NotificationPollerLivenessCheckIntervalMillis.class));
    m_pollerStaleTimeMillis = Assertions.assertNotNull(CONFIG.getPropertyValue(MaxNotificationPollerStaleTimeMillis.class));

    m_livenessFuture = Jobs.schedule(this::checkPollerLiveness, Jobs.newInput()
        .withRunContext(createRunContext())
        .withName(ClientNotificationPoller.class.getSimpleName() + "-livenessCheck")
        .withExceptionHandling(BEANS.get(ExceptionHandler.class), true)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(pollerCheckIntervalMillis, TimeUnit.MILLISECONDS))));
  }

  protected void stopLivenessChecker() {
    if (m_livenessFuture == null) {
      return;
    }
    m_livenessFuture.cancel(true);
    m_livenessFuture = null;
  }

  protected void touch() {
    m_lastPollRequest = System.currentTimeMillis();
  }

  protected void checkPollerLiveness() {
    LOG.debug("Checking client notification poller liveness");
    final long lastPollRequest = m_lastPollRequest;
    if (lastPollRequest + m_pollerStaleTimeMillis < System.currentTimeMillis()) {
      LOG.warn("Detected stale client notification poller. Restarting poller job [lastPollRequest={}]",
          DateUtility.format(new Date(lastPollRequest), "yyyy-MM-dd HH:mm:ss.SSS"));
      synchronized (m_lock) {
        try {
          stopPoller();
        }
        catch (RuntimeException e) {
          LOG.info("Exception while stopping client notification poller", e);
        }
        try {
          startPoller();
          LOG.info("Restarted client notification poller");
        }
        catch (RuntimeException e) {
          LOG.error("Could not start client notification poller", e);
        }
      }
    }
  }

  /**
   * {@link IPlatformListener} to shutdown this {@link ClientNotificationPoller} upon platform shutdown.
   */
  @Order(-1000)
  public static final class ShutdownListener implements IPlatformListener {
    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        ClientNotificationPoller poller = BEANS.get(ClientNotificationPoller.class);
        poller.stop();
        poller.createRunContext().run(() -> BEANS.optional(IClientNotificationService.class)
            .ifPresent(svc -> svc.unregisterNode(NodeId.current())));
      }
    }
  }

  public static class MaxNotificationPollerStaleTimeMillis extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.SECONDS.toMillis(90);
    }

    @Override
    public String description() {
      return "The maximum amount of time in milliseconds between two client notification poller invocations before "
          + "the poller job is restarted. Note: this value should be at least two times of both 'scout.clientnotification.maxNotificationBlockingTimeOut' "
          + "and 'scout.clientnotification.notificationPollerCheckInterval'. The default is 90 seconds.";
    }

    @Override
    public String getKey() {
      return "scout.clientnotification.maxNotificationPollerStaleTime";
    }
  }

  public static class NotificationPollerLivenessCheckIntervalMillis extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.SECONDS.toMillis(30);
    }

    @Override
    public String description() {
      return "Interval in milliseconds the poller job liveness check is performed. The default is 30 seconds.";
    }

    @Override
    public String getKey() {
      return "scout.clientnotification.notificationPollerLivenessCheckInterval";
    }
  }
}
