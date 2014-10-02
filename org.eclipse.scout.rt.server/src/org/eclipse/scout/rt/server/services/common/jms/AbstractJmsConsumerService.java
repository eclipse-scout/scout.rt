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
package org.eclipse.scout.rt.server.services.common.jms;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.jms.internal.JmsTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.service.AbstractService;

/**
 * @deprecated use org.eclipse.scout.rt.server.jms.transactional.AbstractTransactionalJmsService<T>. Will be removed in
 *             the N release.
 */
@Deprecated
public abstract class AbstractJmsConsumerService extends AbstractService implements IJmsConsumerService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJmsConsumerService.class);

  private JmsJndiConfig m_config = new JmsJndiConfig();

  public AbstractJmsConsumerService() {
    initConfig();
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  protected String getConfiguredContextFactory() {
    return null;
  }

  public String getContextFactory() {
    return m_config.getInitialContextFactory();
  }

  public void setContextFactory(String s) {
    m_config.setInitialContextFactory(s);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  protected String getConfiguredProviderUrl() {
    return null;
  }

  public String getProviderUrl() {
    return m_config.getProviderUrl();
  }

  public void setProviderUrl(String s) {
    m_config.setProviderUrl(s);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  protected String getConfiguredConnectionFactoryJndiName() {
    return null;
  }

  public String getConnectionFactoryJndiName() {
    return m_config.getConnectionFactoryJndiName();
  }

  public void setConnectionFactoryJndiName(String s) {
    m_config.setConnectionFactoryJndiName(s);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(40)
  protected String getConfiguredJndiName() {
    return null;
  }

  public String getJndiName() {
    return m_config.getJndiName();
  }

  public void setJndiName(String s) {
    m_config.setJndiName(s);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredJmsUsername() {
    return null;
  }

  public String getJmsUsername() {
    return m_config.getUserName();
  }

  public void setJmsUsername(String s) {
    m_config.setUserName(s);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  protected String getConfiguredJmsPassword() {
    return null;
  }

  public String getJmsPassword() {
    return m_config.getPassword();
  }

  public void setJmsPassword(String s) {
    m_config.setPassword(s);
  }

  protected void initConfig() {
    setConnectionFactoryJndiName(getConfiguredConnectionFactoryJndiName());
    setContextFactory(getConfiguredContextFactory());
    setJmsPassword(getConfiguredJmsPassword());
    setJmsUsername(getConfiguredJmsUsername());
    setJndiName(getConfiguredJndiName());
    setProviderUrl(getConfiguredProviderUrl());
  }

  /**
   * Sends given object to the JMS queue / topic
   * 
   * @param object
   *          {@link Object}
   */
  @Override
  public void putObject(Object object) throws ProcessingException {
    if (LOG.isInfoEnabled()) {
      LOG.info("obj=" + object);
    }
    getJmsXAResource().putObject(object, true);
  }

  /**
   * Retrieve next object from JMS queue / topic.<br>
   * Unlimited wait.
   */
  @Override
  public Object getObject() throws ProcessingException {
    return getObject(-1);
  }

  /**
   * Retrieve next object from queue / topic.<br>
   * Wait max given milliseconds for next message if not there yet.
   * 
   * @param timeoutMillis
   *          long
   */
  @Override
  public Object getObject(long timeoutMillis) throws ProcessingException {
    return getJmsXAResource().getObject(timeoutMillis, true);
  }

  /**
   * Get the XA Resource on message queue. One per config.<br>
   * Retrieve from registry or create new if not existing and register.<br>
   * Uses configs hashCode to identify queue configuration.
   */
  private JmsTransactionMember getJmsXAResource() throws ProcessingException {
    String resId = new Long(m_config.getCrc()).toString();
    ITransaction reg = ThreadContext.getTransaction();
    if (reg == null) {
      throw new ProcessingException("no ITransaction available, use ServerJob to run truncactions");
    }
    JmsTransactionMember res = (JmsTransactionMember) reg.getMember(resId);
    if (res == null) {
      res = new JmsTransactionMember(m_config);
      reg.registerMember(res);
    }
    return res;
  }
}
