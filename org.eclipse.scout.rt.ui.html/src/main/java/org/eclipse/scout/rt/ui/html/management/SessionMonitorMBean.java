/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.management;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.management.ObjectName;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.jmx.MBeanUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.ui.html.IUiSession;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpSession;

@ApplicationScoped
@CreateImmediately
public class SessionMonitorMBean implements ISessionMonitorMBean {
  protected static final Object MARKER_VALUE = new Object();

  protected Map<WeakReference<HttpSession>, Object> m_httpSessionRefs = new ConcurrentHashMap<>();
  protected Map<WeakReference<IUiSession>, Object> m_uiSessionRefs = new ConcurrentHashMap<>();
  protected Map<WeakReference<IClientSession>, Object> m_clientSessionRefs = new ConcurrentHashMap<>();

  /*
   * JMX registration
   */

  protected ObjectName jmxObjectName() {
    return MBeanUtility.toJmxName("org.eclipse.scout.rt.ui.html", "Sessions");
  }

  @PostConstruct
  protected void postConstruct() {
    MBeanUtility.register(jmxObjectName(), this);
  }

  @PreDestroy
  protected void preDestroy() {
    MBeanUtility.unregister(jmxObjectName());
  }

  /*
   * Concurrent state updates
   */

  public void weakRegister(HttpSession httpSession) {
    m_httpSessionRefs.put(new WeakReference<>(httpSession), MARKER_VALUE);
  }

  public void weakRegister(IUiSession uiSession) {
    m_uiSessionRefs.put(new WeakReference<>(uiSession), MARKER_VALUE);
  }

  public void weakRegister(IClientSession clientSession) {
    m_clientSessionRefs.put(new WeakReference<>(clientSession), MARKER_VALUE);
  }

  /*
   * MBean implementation
   */
  @Override
  public int getNumHttpSessions() {
    return (int) m_httpSessionRefs
        .keySet()
        .stream()
        .map(Reference::get)
        .filter(Objects::nonNull)
        .count();
  }

  @Override
  public int getNumUiSessions() {
    return (int) m_uiSessionRefs
        .keySet()
        .stream()
        .map(Reference::get)
        .filter(Objects::nonNull)
        .count();
  }

  @Override
  public int getNumClientSessions() {
    return (int) m_clientSessionRefs
        .keySet()
        .stream()
        .map(Reference::get)
        .filter(Objects::nonNull)
        .count();
  }

  @Override
  public List<String> getSessionTable() {
    List<SessionDetail> details = getSessionDetails();
    List<String> strings = new ArrayList<>(1 + details.size());
    strings.add(SessionDetail.toCsvHeader());
    for (SessionDetail d : details) {
      strings.add(d.toCsvRow());
    }
    return strings;
  }

  @Override
  public List<SessionDetail> getSessionDetails() {
    Map<String, IClientSession> clients =
        m_clientSessionRefs
            .keySet()
            .stream()
            .map(Reference::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(ISession::getId, c -> c));
    Map<String, List<IUiSession>> clientToUis =
        m_uiSessionRefs
            .keySet()
            .stream()
            .map(Reference::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(uiSession -> ObjectUtility.nvl(uiSession.getClientSessionId(), "[Unmapped]")));
    //add client sessions that are not referenced by any uiSession
    clients
        .values()
        .stream()
        .forEach(clientSession -> clientToUis.computeIfAbsent(clientSession.getId(), clientSessionId -> Collections.singletonList(null)));

    ArrayList<SessionDetail> list = new ArrayList<>();
    clientToUis
        .forEach((clientSessionId, uiSessions) -> uiSessions
            .forEach(uiSession -> list.add(createSessionInfo(uiSession, clients.get(clientSessionId)))));

    Comparator<SessionDetail> comp = Comparator
        .comparing(SessionDetail::getHttpSessionId)
        .thenComparing(SessionDetail::getUiSessionId)
        .thenComparing(SessionDetail::getClientSessionId);
    Collections.sort(list, comp);
    return list;
  }

  private SessionDetail createSessionInfo(IUiSession uiSession, IClientSession clientSession) {
    String httpSessionId = "";
    String uiSessionId = "";
    String clientSessionId = "";
    String userId = "";
    long lastAccessed = 0L;
    String uiState = "";
    String clientState = "";
    if (uiSession != null) {
      httpSessionId = uiSession.getHttpSessionId();
      uiSessionId = uiSession.getUiSessionId();
      lastAccessed = uiSession.getLastAccessedTime();
      if (uiSession.isDisposed()) {
        uiState = "Disposed";
      }
      else if (uiSession.isInitialized()) {
        uiState = "Active";
      }
      else {
        uiState = "Preregistered";
      }
    }
    if (clientSession != null) {
      clientSessionId = clientSession.getId();
      userId = clientSession.getUserId();
      if (clientSession.isStopping()) {
        if (clientSession.isActive()) {
          clientState = "Stopping";
        }
        else {
          clientState = "Stopped";
        }
      }
      else {
        if (clientSession.isActive()) {
          clientState = "Active";
        }
        else {
          clientState = "Starting";
        }
      }
    }
    return new SessionDetail(
        httpSessionId,
        uiSessionId,
        clientSessionId,
        userId,
        uiState,
        clientState,
        lastAccessed);
  }
}
