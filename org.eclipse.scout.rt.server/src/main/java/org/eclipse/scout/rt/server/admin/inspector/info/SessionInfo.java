/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.inspector.info;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.shared.ui.UserAgent;

public class SessionInfo {
  private String m_sessionId;
  private Long m_creationTime;
  private Long m_lastAccessedTime;
  private String m_userId;
  private Subject m_subject;
  private UserAgent m_userAgent;

  public String getUserId() {
    return m_userId;
  }

  public void setUserId(String user) {
    m_userId = user;
  }

  public Subject getSubject() {
    return m_subject;
  }

  public void setSubject(Subject s) {
    m_subject = s;
  }

  public Long getCreationTime() {
    return m_creationTime;
  }

  public void setCreationTime(Long t) {
    m_creationTime = t;
  }

  public Long getLastAccessedTime() {
    return m_lastAccessedTime;
  }

  public void setLastAccessedTime(Long t) {
    m_lastAccessedTime = t;
  }

  public String getSessionId() {
    return m_sessionId;
  }

  public void setSessionId(String s) {
    m_sessionId = s;
  }

  public UserAgent getUserAgent() {
    return m_userAgent;
  }

  public void setUserAgent(UserAgent userAgent) {
    m_userAgent = userAgent;
  }
}
