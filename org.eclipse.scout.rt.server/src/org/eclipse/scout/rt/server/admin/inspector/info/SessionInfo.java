/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.admin.inspector.info;

import javax.security.auth.Subject;

public class SessionInfo {
  private String m_sessionId;
  private long m_creationTime;
  private long m_lastAccessedTime;
  private String m_userId;
  private Subject m_subject;

  public SessionInfo() {
  }

  public String getUserId() {
    return m_userId;
  }

  public void setUserId(String user) {
    this.m_userId = user;
  }

  public Subject getSubject() {
    return m_subject;
  }

  public void setSubject(Subject s) {
    m_subject = s;
  }

  public long getCreationTime() {
    return m_creationTime;
  }

  public void setCreationTime(long t) {
    this.m_creationTime = t;
  }

  public long getLastAccessedTime() {
    return m_lastAccessedTime;
  }

  public void setLastAccessedTime(long t) {
    this.m_lastAccessedTime = t;
  }

  public String getSessionId() {
    return m_sessionId;
  }

  public void setSessionId(String s) {
    this.m_sessionId = s;
  }

}
