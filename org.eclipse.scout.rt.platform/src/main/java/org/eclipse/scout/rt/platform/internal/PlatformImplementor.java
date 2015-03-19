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
package org.eclipse.scout.rt.platform.internal;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.IApplication;
import org.eclipse.scout.rt.platform.IBeanContext;
import org.eclipse.scout.rt.platform.IBeanInstanceFactory;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.platform.SimpleBeanInstanceFactory;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder;
import org.jboss.jandex.Index;

/**
 * @since 15.2
 */
public class PlatformImplementor implements IPlatform {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PlatformImplementor.class);

  private final ReentrantReadWriteLock m_stateLock;
  private volatile State m_state; // may be read at any time by any thread
  private IClassInventory m_classInventory;
  private BeanContextImplementor m_beanContext;
  private IApplication m_application;

  public PlatformImplementor() {
    m_stateLock = new ReentrantReadWriteLock(true);
    m_state = State.PlatformStopped;
  }

  @Override
  public State getState() {
    return m_state;
  }

  @Override
  public IClassInventory getClassInventory() {
    // use lock to ensure the caller waits until the platform has been started completely
    m_stateLock.readLock().lock();
    try {
      return m_classInventory;
    }
    finally {
      m_stateLock.readLock().unlock();
    }
  }

  @Override
  public IBeanContext getBeanContext() {
    // use lock to ensure the caller waits until the platform has been started completely
    m_stateLock.readLock().lock();
    try {
      return m_beanContext;
    }
    finally {
      m_stateLock.readLock().unlock();
    }
  }

  @Override
  public synchronized void start() {
    m_stateLock.writeLock().lock();
    try {
      changeState(State.PlatformStopped);
      m_classInventory = createClassInventory();
      m_beanContext = createBeanContext();
      //now all IPlatformListener are registered and can receive platform events

      changeState(State.PlatformInit);
      changeState(State.ClassInventoryValid);
      changeState(State.BeanContextPrepared);

      initBeanInstanceFactory();
      changeState(State.BeanContextValid);
      startCreateImmediatelyBeans();

      changeState(State.ApplicationStarting);
      startApplication();
      changeState(State.ApplicationStarted);
    }
    finally {
      m_stateLock.writeLock().unlock();
    }
  }

  protected IClassInventory createClassInventory() {
    try {
      long t0 = System.nanoTime();

      JandexInventoryBuilder beanFinder = new JandexInventoryBuilder();
      beanFinder.scanAllModules();
      beanFinder.finish();
      long millis = (System.nanoTime() - t0) / 1000000L;
      Index index = beanFinder.getIndex();
      LOG.info("created class inventory  in {0} ms", millis);

      return new JandexClassInventory(index);
    }
    catch (Throwable t) {
      throw new PlatformException("Error while building class inventory", t);
    }
  }

  protected BeanContextImplementor createBeanContext() {
    BeanContextImplementor context = new BeanContextImplementor();
    Set<Class> allBeans = new BeanFilter().collect(getClassInventory());
    for (Class<?> bean : allBeans) {
      context.registerClass(bean);
    }
    return context;
  }

  protected void initBeanInstanceFactory() {
    if (m_beanContext.getBeanInstanceFactory() != null) {
      return;
    }
    TreeSet<IBeanRegistration> regs = m_beanContext.getRegistrationsInternal(IBeanInstanceFactory.class);
    if (regs.size() > 0) {
      m_beanContext.setBeanInstanceFactory((IBeanInstanceFactory) regs.first().getInstance());
    }
    if (m_beanContext.getBeanInstanceFactory() != null) {
      return;
    }
    LOG.warn("Using " + SimpleBeanInstanceFactory.class.getName() + ". Please verify that this application really has no client or server side " + IBeanInstanceFactory.class.getSimpleName());
    m_beanContext.setBeanInstanceFactory(new SimpleBeanInstanceFactory());
  }

  protected void startCreateImmediatelyBeans() {
    ((BeanContextImplementor) m_beanContext).startCreateImmediatelyBeans();
  }

  protected void startApplication() {
    m_application = OBJ.getOptional(IApplication.class);
    if (m_application != null) {
      try {
        m_application.start();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not start application '%s'.", m_application.getClass().getName()), e);
      }
    }
    else {
      LOG.warn("Start platform without an application. No application has been found.");
    }
  }

  @Override
  public synchronized void stop() {
    m_stateLock.writeLock().lock();
    try {
      changeState(State.ApplicationStopping);
      stopApplication();
      changeState(State.ApplicationStopped);
      destroyBeanContext();
      destroyClassInventory();
      if (Platform.get() == this) {
        Platform.set(null);
      }
      changeState(State.PlatformStopped);
    }
    finally {
      m_stateLock.writeLock().unlock();
    }
  }

  protected void stopApplication() {
    if (m_application != null) {
      try {
        m_application.stop();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not stop application '%s'.", m_application.getClass().getName()), e);
      }
      finally {
        m_application = null;
      }
    }
  }

  protected void destroyClassInventory() {
    m_classInventory = null;
  }

  protected void destroyBeanContext() {
    m_beanContext = null;
  }

  protected void changeState(State newState) {
    verifyStateChange(m_state, newState);
    if (m_state == newState) {
      return;
    }
    m_state = newState;
    fireStateEvent(newState);
  }

  protected void verifyStateChange(State oldState, State newState) {
    if (oldState == newState) {
      return;
    }
    else if (oldState == State.PlatformInit && newState == State.ClassInventoryValid) {
      return;
    }
    else if (oldState == State.ClassInventoryValid && newState == State.BeanContextPrepared) {
      return;
    }
    else if (oldState == State.BeanContextPrepared && newState == State.BeanContextValid) {
      return;
    }
    else if (oldState == State.BeanContextValid && newState == State.ApplicationStarting) {
      return;
    }
    else if (oldState == State.ApplicationStarting && newState == State.ApplicationStarted) {
      return;
    }
    else if (oldState == State.ApplicationStarted && newState == State.ApplicationStopping) {
      return;
    }
    else if (oldState == State.ApplicationStopping && newState == State.ApplicationStopped) {
      return;
    }
    else if (oldState == State.ApplicationStopped && newState == State.PlatformStopped) {
      return;
    }
    else if (oldState == State.PlatformStopped && newState == State.PlatformInit) {
      return;
    }
    throw new PlatformException("Invalid state change from " + oldState + " to " + newState);
  }

  protected void fireStateEvent(State newState) {
    PlatformEvent e = new PlatformEvent(this, newState);
    for (IBeanRegistration reg : m_beanContext.getRegistrationsInternal(IPlatformListener.class)) {
      try {
        IPlatformListener listener = (IPlatformListener) reg.getInstance();
        listener.stateChanged(e);
      }
      catch (Throwable t) {
        LOG.warn(IPlatformListener.class.getSimpleName() + " " + reg.getBean().getBeanClazz(), t);
      }
    }
  }
}
