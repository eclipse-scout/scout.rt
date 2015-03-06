package org.eclipse.scout.service;

/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 *
 */
public class ServiceReference implements IServiceReference {

  private Class<? extends Object> m_service;
  private Class<?> m_clientSession;
  private Class<?> m_serverSession;
  private boolean m_createImmediately;
  private double m_ranking;
  private boolean m_proxy;

  public void setService(Class<? extends Object> service) {
    m_service = service;
  }

  @Override
  public Class<? extends Object> getService() {
    return m_service;
  }

  public void setClientSession(Class<?> clientSession) {
    m_clientSession = clientSession;
  }

  @Override
  public Class<?> getClientSession() {
    return m_clientSession;
  }

  @Override
  public Class<?> getServerSession() {
    return m_serverSession;
  }

  public void setServerSession(Class<?> serverSession) {
    m_serverSession = serverSession;
  }

  public void setCreateImmediately(boolean createImmediately) {
    m_createImmediately = createImmediately;
  }

  @Override
  public boolean isCreateImmediately() {
    return m_createImmediately;
  }

  public void setRanking(double ranking) {
    m_ranking = ranking;
  }

  @Override
  public double getRanking() {
    return m_ranking;
  }

  public void setProxy(boolean proxy) {
    m_proxy = proxy;
  }

  @Override
  public boolean isProxy() {
    return m_proxy;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("service='").append(getService() != null ? getService().getName() : "null").append("'");
    builder.append(", clientSession='").append(getClientSession() != null ? getClientSession().getName() : "null").append("'");
    builder.append(", serverSession='").append(getServerSession() != null ? getServerSession().getName() : "null").append("'");
    builder.append(", ranking='").append(Double.toString(getRanking())).append("'");
    builder.append(", isProxy='").append(Boolean.toString(isProxy())).append("'");
    builder.append(", createImmediately='").append(Boolean.toString(isCreateImmediately())).append("'");
    return builder.toString();
  }

}
