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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.jms.internal.JmsTransactionMember;

/**
 * J2eeJmsObserver implementation with anchor in servlet context J2eeJmsObserver
 * expects to find its IServerSession in the servlet context
 */
public class JmsObserver {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JmsObserver.class);

  private Object m_observerMapLock;
  private HashMap<JmsJndiConfig, Collection<WeakReference<IJmsCallback>>> m_observerMap;
  private Object m_jmsListenerMapLock;
  private HashMap<JmsJndiConfig, JmsListener> m_jmsListenerMap;

  public JmsObserver() {
    m_observerMapLock = new Object();
    m_observerMap = new HashMap<JmsJndiConfig, Collection<WeakReference<IJmsCallback>>>();
    m_jmsListenerMapLock = new Object();
    m_jmsListenerMap = new HashMap<JmsJndiConfig, JmsListener>();
  }

  /**
   * Cleanup all weak references. Should be called when a Session terminates to
   * remove all potentially weak referenced callback classes.
   * 
   * @throws ProcessingException
   */
  public void cleanup() throws ProcessingException {
    ArrayList<JmsJndiConfig> empty = new ArrayList<JmsJndiConfig>();
    synchronized (m_observerMap) {
      Collection<JmsJndiConfig> allLists = m_observerMap.keySet();
      for (JmsJndiConfig conf : allLists) {
        Collection<WeakReference<IJmsCallback>> list = m_observerMap.get(conf);
        for (Iterator<WeakReference<IJmsCallback>> it = list.iterator(); it.hasNext();) {
          WeakReference<IJmsCallback> ref = it.next();
          IJmsCallback o = ref.get();
          if (o == null) {
            it.remove();
          }
        }
        if (list.size() == 0) {
          empty.add(conf);
        }
      }
    }
    synchronized (m_jmsListenerMapLock) {
      for (JmsJndiConfig conf : empty) {
        m_observerMap.remove(conf);
        JmsListener listener = m_jmsListenerMap.get(conf);
        if (listener != null) {
          listener.stopListeningOnQueue();
          m_jmsListenerMap.remove(conf);
        }
      }
    }
  }

  /**
   * Callback method that gets called by all listeners on the queue. Iterates
   * the observer map for given config and calls all registered callback classes
   * for this config.
   * 
   * @param config
   *          {@link JmsJndiConfig}
   * @param msg
   *          {@link Message}
   * @param value
   *          {@link Object}
   */
  private void fireCallback(JmsJndiConfig config, final Message msg, final Object value) {
    synchronized (m_observerMapLock) {
      if (m_observerMap != null) {
        for (WeakReference<IJmsCallback> callback : m_observerMap.get(config)) {
          final IJmsCallback c = callback.get();
          if (c != null) {
            try {
              c.execOnMessage(msg, value);
            }
            catch (ProcessingException e) {
              LOG.error("Message " + msg, e);
            }
          }
        }
      }
    }
  }

  /**
   * Adds a listener to the queue. Makes sure that only one listener per service
   * and configuration is added. Takes a implementation of the {@link IJmsCallback} interface as parameter. This service
   * will be called
   * upon a message to the queue. Method is thread safe. Creates an {@link JmsListener} object that is listening on the
   * queue. Will enable the
   * use of username password to connect to JMS-server. Warning: IJmsCallback
   * classes given are weak referenced! Make sure the reference to it is managed
   * outside this class. It will be garbage collected otherwise!
   * 
   * @param callback
   *          {@link IJmsCallback}
   * @param config
   *          {@link JmsJndiConfig}
   * @throws ProcessingException
   */
  public void addListener(IJmsCallback callback, JmsJndiConfig config) throws ProcessingException {
    // only add if not already added
    cleanup();
    Collection<WeakReference<IJmsCallback>> list = null;
    synchronized (m_observerMapLock) {
      list = m_observerMap.get(config);
      if (list == null) {
        list = new ArrayList<WeakReference<IJmsCallback>>();
        m_observerMap.put(config, list);
      }
      list.add(new WeakReference<IJmsCallback>(callback));
    }
    synchronized (m_jmsListenerMapLock) {
      JmsListener listener = m_jmsListenerMap.get(config);
      if (listener == null) {
        listener = new JmsListener(config);
        m_jmsListenerMap.put(config, new JmsListener(config));
      }
      listener.startListeningOnQueue();
    }
  }

  /**
   * Removes listener from the queue. Takes a implementation of the {@link IJmsCallback} interface and the config that
   * defines the queue that
   * the callback is listening on as parameter. Makes sure that listener is
   * removed if no callback class is listening on the given config anymore.
   * Method is thread safe.
   * 
   * @param callback
   *          {@link IJmsCallback}
   * @param config
   *          {@link JmsJndiConfig}
   * @throws ProcessingException
   */
  public void removeListener(IJmsCallback callback, JmsJndiConfig config) throws ProcessingException {
    cleanup();
    Collection<WeakReference<IJmsCallback>> list = null;
    synchronized (m_observerMap) {
      list = m_observerMap.get(config);
      if (list != null) {
        list.remove(callback);
      }
    }
    synchronized (m_jmsListenerMap) {
      if (list != null && list.size() == 0) {
        JmsListener listener = m_jmsListenerMap.get(config);
        if (listener != null) {
          if (LOG.isInfoEnabled()) {
            LOG.info("method=" + callback.getClass().getName());
          }
          listener.stopListeningOnQueue();
          m_jmsListenerMap.remove(config);
          m_observerMap.remove(config);
        }
      }
    }
  }

  /**
   * Removes all listeners from the queue. Cleanup using this method. Warning:
   * removes all listeners and callback classes!
   */
  public void removeAllListeners() throws ProcessingException {
    cleanup();
    // only add if not already added
    synchronized (m_jmsListenerMap) {
      if (LOG.isInfoEnabled()) {
        LOG.info("" + m_observerMap + " listeners");
      }
      for (JmsListener listener : m_jmsListenerMap.values()) {
        try {
          listener.stopListeningOnQueue();
        }
        catch (ProcessingException e) {
          LOG.error(null, e);
        }
      }
    }
    m_jmsListenerMap.clear();
    synchronized (m_observerMapLock) {
      for (Collection<WeakReference<IJmsCallback>> list : m_observerMap.values()) {
        list.clear();
      }
      m_observerMap.clear();
    }
  }

  /**
   * Inner class defining a listener on a JMS queue or topic. Inherits from the {@link MessageListener} interface.
   */
  private class JmsListener implements javax.jms.MessageListener {

    private JmsJndiConfig m_config;
    private JmsTransactionMember m_jmsXaResource;

    public JmsListener(JmsJndiConfig config) {
      m_config = config;
      m_jmsXaResource = new JmsTransactionMember(m_config);
    }

    /**
     * make sure resources are freed
     */
    @Override
    protected void finalize() throws Throwable {

      m_jmsXaResource.release();
      m_jmsXaResource = null;
    }

    /**
     * Start to listen on the queue defined in {@link JmsTransactionMember} instance of
     * this class.
     * 
     * @throws ProcessingException
     */
    public void startListeningOnQueue() throws ProcessingException {
      stopListeningOnQueue();
      try {
        MessageConsumer consumer = m_jmsXaResource.getMessageConsumer();
        consumer.setMessageListener(this);
      }
      catch (JMSException e) {
        stopListeningOnQueue();
        throw new ProcessingException(e.getLocalizedMessage(), e.getCause());
      }
    }

    /**
     * Stop to listen on the queue defined in {@link ScoutJMSXAResource} instance of this class.
     * 
     * @throws ProcessingException
     */
    public void stopListeningOnQueue() throws ProcessingException {
      m_jmsXaResource.release();
    }

    /**
     * The onMessage Method is called by JMS when a message has arrived for the
     * queue / topic that this listener listens to.
     */
    @Override
    public final void onMessage(Message msg) {
      try {
        if (msg != null) {
          Object valueForS = null;
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
          fireCallback(m_config, msg, valueForS);
          msg.acknowledge();
          if (m_jmsXaResource.commitPhase1()) {
            m_jmsXaResource.commitPhase2();
          }
        }
      }
      catch (JMSException ex) {
        LOG.error("receiving message: " + msg, ex);
        // err, no acknowledge
        m_jmsXaResource.rollback();
      }
    }
  }
}
