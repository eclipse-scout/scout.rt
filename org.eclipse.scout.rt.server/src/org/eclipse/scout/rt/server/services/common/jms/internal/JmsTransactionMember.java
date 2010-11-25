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
package org.eclipse.scout.rt.server.services.common.jms.internal;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.jms.JmsJndiConfig;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;

/**
 * Title: BSI Scout V3
 * , Michael Rudolf
 * @version 3.x
 */

/**
 * Queue/Topic xa resource (per request)
 * 
 * @since Build 192
 *        (Extracted out of JmsService class to an autonomous class)
 */
public class JmsTransactionMember implements ITransactionMember {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JmsTransactionMember.class);

  public static final String TRANSACTION_MEMBER_ID = "JmsTransactionMember";
  // app ctx
  private ConnectionFactory m_cf;
  private Destination m_destination;
  private JmsJndiConfig m_config;
  // request ctx
  private Connection m_conn;
  private Session m_session;
  private MessageConsumer m_mc;
  private MessageProducer m_mp;
  private boolean m_typeQueue;
  private boolean m_useSecurityCredential;

  public JmsTransactionMember(JmsJndiConfig config) {
    this.m_config = config;
  }

  /**
   * Makes sure the message connection factory and destination (queue / topic)
   * cache is set and can be used.
   * 
   * @throws ProcessingException
   */
  public void ensureCache() throws ProcessingException {
    if (m_cf == null || m_destination == null) {
      InitialContext ctx = null;
      try {
        ctx = m_config.createInitialContext();
        try {
          m_cf = (ConnectionFactory) ctx.lookup(m_config.getConnectionFactoryJndiName());
          m_destination = (Destination) ctx.lookup(m_config.getJndiName());
        }
        catch (NamingException e) {
          throw new ProcessingException(e.getMessage(), e.getCause());
        }
      }
      finally {
        if (ctx != null) {
          // try{ctx.close();}catch(Exception fatal){}
        }
      }
    }
  }

  public String getMemberId() {

    return new Long(m_config.getCrc()).toString();
  }

  public boolean isQueue() {
    return m_typeQueue;
  }

  public boolean isTopic() {
    return !m_typeQueue;
  }

  /**
   * Forces connection setup
   */
  public void connectToJmsImmediately() throws ProcessingException {
    ensureConnectionToJms();
  }

  private void ensureConnectionToJms() throws ProcessingException {
    ensureCache();
    if (m_conn == null || m_session == null) {
      try {
        // new interface
        try {
          if (m_useSecurityCredential) {
            m_conn = m_cf.createConnection(m_config.getUserName(), m_config.getPassword());
          }
          else {
            m_conn = m_cf.createConnection();
          }
        }
        catch (Throwable e2) {
          // old interface
          if (isQueue()) {
            if (m_useSecurityCredential) {
              m_conn = ((QueueConnectionFactory) m_cf).createQueueConnection(m_config.getUserName(), m_config.getPassword());
            }
            else {
              m_conn = ((QueueConnectionFactory) m_cf).createQueueConnection();
            }
          }
          else {
            if (m_useSecurityCredential) {
              m_conn = ((TopicConnectionFactory) m_cf).createTopicConnection(m_config.getUserName(), m_config.getPassword());
            }
            else {
              m_conn = ((TopicConnectionFactory) m_cf).createTopicConnection();
            }
          }
        }
        // new interface
        try {
          m_session = m_conn.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        }
        catch (Throwable e2) {
          // old interface
          if (isQueue()) {
            m_session = ((QueueConnection) m_conn).createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
          }
          // topic
          else {
            m_session = ((TopicConnection) m_conn).createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
          }
        }
        m_conn.start();
      }
      catch (JMSException e) {
        release();
        throw new ProcessingException(e.getMessage(), e.getCause());
      }
    }
  }

  public Connection getConnection() throws ProcessingException {
    ensureConnectionToJms();
    return m_conn;
  }

  public Session getSession() throws ProcessingException {
    ensureConnectionToJms();
    return m_session;
  }

  public MessageConsumer getMessageConsumer() throws ProcessingException {
    ensureConnectionToJms();
    try {
      if (m_mc == null) {
        // new interface
        try {
          m_mc = m_session.createConsumer(m_destination);
        }
        catch (Throwable t) {
          if (isQueue()) {
            m_mc = ((QueueSession) m_session).createReceiver((Queue) m_destination);
          }
          else {
            m_mc = ((TopicSession) m_session).createSubscriber((Topic) m_destination);
          }
        }
      }
    }
    catch (JMSException e) {
      throw new ProcessingException(e.getMessage(), e.getCause());
    }
    return m_mc;
  }

  public MessageProducer getMessageProducer() throws ProcessingException {
    ensureConnectionToJms();
    try {
      if (m_mp == null) {
        // new interface
        try {
          m_mp = m_session.createProducer(m_destination);
        }
        catch (Throwable t) {
          // old interface
          if (isQueue()) {
            m_mp = ((QueueSession) m_session).createSender((Queue) m_destination);
          }
          else {
            m_mp = ((TopicSession) m_session).createPublisher((Topic) m_destination);
          }
        }
      }
    }
    catch (JMSException e) {
      throw new ProcessingException(e.getMessage(), e.getCause());
    }
    return m_mp;
  }

  public void putObject(Object obj, boolean autoCommit) throws ProcessingException {
    ensureCache();

    if (LOG.isInfoEnabled()) LOG.info("obj=" + obj);
    Session session = getSession();
    //
    Message msg0 = null;
    try {
      if (obj instanceof byte[]) {
        BytesMessage msg = session.createBytesMessage();
        msg.writeBytes((byte[]) obj);
        msg0 = msg;
      }
      else if (obj instanceof String) {
        TextMessage msg = session.createTextMessage((String) obj);
        msg0 = msg;
      }
      else if (obj instanceof Serializable) {
        ObjectMessage msg = session.createObjectMessage((Serializable) obj);
        msg0 = msg;
      }
      else {
        throw new IllegalArgumentException("Cannot put object of type " + (obj != null ? obj.getClass() : null));
      }
    }
    catch (JMSException e) {
      throw new ProcessingException(e.getMessage(), e.getCause());
    }
    if (msg0 != null) {
      MessageProducer mp = getMessageProducer();
      // new interface
      try {
        mp.send(msg0);
      }
      catch (Throwable t) {
        // old interface
        try {
          if (isQueue()) {
            ((QueueSender) mp).send(msg0);
          }
          else {
            ((TopicPublisher) mp).publish(msg0);
          }
        }
        catch (JMSException e) {
          throw new ProcessingException(e.getMessage(), e.getCause());
        }
      }
      if (autoCommit) {
        try {
          session.commit();
        }
        catch (JMSException e) {
          throw new ProcessingException(e.getMessage(), e.getCause());
        }
      }
    }
    release();
  }

  public Object getObject(long timeoutMillis, boolean autoCommit) throws ProcessingException {
    ensureCache();

    Session session = getSession();
    //
    MessageConsumer mc = getMessageConsumer();
    Message msg = null;
    if (timeoutMillis > 0) {
      // new interface
      try {
        msg = mc.receive(timeoutMillis);
      }
      catch (Throwable t) {
        // old interface
        try {
          if (isQueue()) {
            msg = ((QueueReceiver) mc).receive(timeoutMillis);
          }
          else {
            msg = ((TopicSubscriber) mc).receive(timeoutMillis);
          }
        }
        catch (JMSException e) {
          throw new ProcessingException(e.getMessage(), e.getCause());
        }
      }
    }
    else if (timeoutMillis == 0) {
      // new interface
      try {
        msg = mc.receiveNoWait();
      }
      catch (Throwable t) {
        // old interface
        try {
          if (isQueue()) {
            msg = ((QueueReceiver) mc).receiveNoWait();
          }
          else {
            msg = ((TopicSubscriber) mc).receiveNoWait();
          }
        }
        catch (JMSException e) {
          throw new ProcessingException(e.getMessage(), e.getCause());
        }
      }
    }
    else {
      // new interface
      try {
        msg = mc.receive();
      }
      catch (Throwable t) {
        // old interface
        try {
          if (isQueue()) {
            msg = ((QueueReceiver) mc).receive();
          }
          else {
            msg = ((TopicSubscriber) mc).receive();
          }
        }
        catch (JMSException e) {
          throw new ProcessingException(e.getMessage(), e.getCause());
        }
      }
    }
    //
    if (msg == null) {
      release();
      return null;
    }
    else {
      Object valueForS = null;
      try {
        if (msg instanceof TextMessage) {
          valueForS = ((TextMessage) msg).getText();
        }
        else if (msg instanceof BytesMessage) {
          byte[] ba = new byte[(int) ((BytesMessage) msg).getBodyLength()];
          ((BytesMessage) msg).readBytes(ba);
          valueForS = ba;
        }
        else if (msg instanceof ObjectMessage) {
          valueForS = ((ObjectMessage) msg).getObject();
        }
        else {
          throw new IllegalArgumentException("Unexpected message of type " + msg.getClass());
        }
        msg.acknowledge();
        if (autoCommit) {
          session.commit();
        }
      }
      catch (JMSException e) {
        throw new ProcessingException(e.getMessage(), e.getCause());
      }
      release();
      return valueForS;
    }
  }

  public boolean needsCommit() {
    return true;
  }

  public boolean commitPhase1() {
    return true;
  }

  public void commitPhase2() {
    if (m_session != null) {
      try {
        m_session.commit();
      }
      catch (JMSException ex) {
        LOG.error("commit: " + m_session, ex);
      }
    }
  }

  public void rollback() {
    if (m_session != null) {
      try {
        m_session.rollback();
      }
      catch (JMSException ex) {
        LOG.error("rollback: " + m_session, ex);
      }
    }
  }

  public void release() {
    if (m_mc != null) {
      try {
        m_mc.close();
      }
      catch (JMSException fatal) {
        LOG.error(null, fatal);
      }
      m_mc = null;
    }
    if (m_mp != null) {
      try {
        m_mp.close();
      }
      catch (JMSException fatal) {
        LOG.error(null, fatal);
      }
      m_mp = null;
    }
    if (m_conn != null) {
      try {
        m_conn.stop();
      }
      catch (JMSException fatal) {
        LOG.error(null, fatal);
      }
      try {
        m_conn.close();
      }
      catch (JMSException fatal) {
        LOG.error(null, fatal);
      }
      m_conn = null;
    }
    if (m_session != null) {
      try {
        m_session.close();
      }
      catch (JMSException fatal) {
        LOG.error(null, fatal);
      }
      m_session = null;
    }
  }
}
