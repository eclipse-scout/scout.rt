/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.PlatformStateLatch;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.1
 */
public class PlatformImplementor implements IPlatform {
  private static final Logger LOG = LoggerFactory.getLogger(PlatformImplementor.class);

  private final ReentrantReadWriteLock m_platformLock = new ReentrantReadWriteLock(true);
  private final Object m_platformStartedLock = new Object();
  private final AtomicReference<State> m_state; // may be read at any time by any thread
  private BeanManagerImplementor m_beanContext;

  public PlatformImplementor() {
    m_state = new AtomicReference<>(State.PlatformStopped);
  }

  @Override
  public State getState() {
    return m_state.get();
  }

  @Override
  public IBeanManager getBeanManager() {
    // use lock to ensure the caller waits until the platform has been started completely
    m_platformLock.readLock().lock();
    try {
      if (getState() == State.PlatformInvalid) {
        throw new PlatformException("The platform is in an invalid state.");
      }
      return m_beanContext;
    }
    finally {
      m_platformLock.readLock().unlock();
    }
  }

  @Override
  public void awaitPlatformStarted() {
    if (isPlatformStarted()) {
      return;
    }
    synchronized (m_platformStartedLock) {
      while (!isPlatformStarted()) {
        try {
          m_platformStartedLock.wait();
        }
        catch (InterruptedException e) {
          // nop
        }
      }
    }
  }

  protected boolean isPlatformStarted() {
    final State state = getState();
    if (state == null || state == State.PlatformInvalid) {
      throw new PlatformException("The platform is in an invalid state.");
    }
    if (state == State.PlatformStopping) {
      throw new PlatformException("The platform is stopping.");
    }
    return state == State.PlatformStarted;
  }

  protected void notifyPlatformStarted() {
    synchronized (m_platformStartedLock) {
      m_platformStartedLock.notifyAll();
    }
  }

  @Override
  public void start() {
    start(null);
  }

  @Override
  public void start(PlatformStateLatch stateLatch) {
    try {
      m_platformLock.writeLock().lock();
      try {
        if (stateLatch != null) {
          stateLatch.release();
        }
        if (m_state.get() != State.PlatformStopped) {
          throw new PlatformException("Platform is not stopped [m_state=" + m_state.get() + "]");
        }

        try {
          m_beanContext = createBeanManager();
          //now all IPlatformListener are registered and can receive platform events
          changeState(State.BeanManagerPrepared, true);

          //validateBeanManager();
          validateConfiguration();
          initBeanDecorationFactory();

          changeState(State.BeanManagerValid, true);
          startCreateImmediatelyBeans();
        }
        catch (RuntimeException | Error e) {
          LOG.error("Error during platform startup", e);
          changeState(State.PlatformInvalid, true);
          throw e;
        }
      }
      finally {
        //since we are using a reentrant lock, platform beans can be accessed within platform listeners
        //lock has to be released after the State.BeanManagerValid change to make sure everything is initialized correctly, before beans can be accessed.
        m_platformLock.writeLock().unlock();
      }
      changeState(State.PlatformStarted, true);
    }
    finally {
      notifyPlatformStarted();
    }
  }

  protected void validateConfiguration() {
    if (!ConfigUtility.isInitialized()) {
      LOG.info("No {} found. Running with empty configuration.", ConfigUtility.CONFIG_FILE_NAME);
    }

    int errorCount = 0;
    for (IConfigProperty prop : BEANS.all(IConfigProperty.class)) {
      try {
        prop.getValue();
      }
      catch (Exception ex) {
        errorCount++;
        LOG.error("Failed reading config property '{}'", prop.getKey(), ex);
      }
    }
    if (errorCount > 0) {
      throw new PlatformException("Cannot start platform due to " + errorCount + " invalid config properties");
    }
  }

  protected BeanManagerImplementor createBeanManager() {
    BeanManagerImplementor context = new BeanManagerImplementor();
    Set<Class> allBeans = new BeanFilter().collect(ClassInventory.get());
    for (Class<?> bean : allBeans) {
      context.registerClass(bean);
    }
    return context;
  }

  protected void initBeanDecorationFactory() {
    if (m_beanContext.getBeanDecorationFactory() != null) {
      return;
    }
    IBean<IBeanDecorationFactory> bean = m_beanContext.optBean(IBeanDecorationFactory.class);
    if (bean != null) {
      m_beanContext.setBeanDecorationFactory(bean.getInstance());
      return;
    }
    LOG.warn("Using {}. Please verify that this application really has no client or server side {}", SimpleBeanDecorationFactory.class.getName(), IBeanDecorationFactory.class.getSimpleName());
    m_beanContext.setBeanDecorationFactory(new SimpleBeanDecorationFactory());
  }

  protected void validateBeanManager() {
    try {
      //collect all service interfaces
      HashSet<Class> serviceInterfaces = new HashSet<>();
      for (IBean bean : getBeanManager().getRegisteredBeans(IService.class)) {
        for (Class<?> i : BeanUtility.getInterfacesHierarchy(bean.getBeanClazz(), Object.class)) {
          if (IService.class.isAssignableFrom(i)) {
            serviceInterfaces.add(i);
          }
        }
      }
      StringBuilder sb = new StringBuilder();
      for (Class s : serviceInterfaces) {
        if (s.equals(IService.class)) {
          continue;
        }
        try {
          @SuppressWarnings("unchecked")
          List<IBean<Object>> list = getBeanManager().getBeans(s);
          if (list.size() <= 1) {
            continue;
          }
          sb.append("-------- ").append(s.getName()).append(" --------\n");
          for (IBean<?> bean : list) {
            sb.append(" @Order(").append(BeanHierarchy.orderOf(bean)).append(") ").append(bean.getBeanClazz()).append('\n');
          }
        }
        catch (Exception e) {
          LOG.warn("Could not list beans of class [{}]", s.getName(), e);
        }
      }
      LOG.info("Query classes that are resolving to multiple beans\n{}", sb);
    }
    catch (Exception e) {
      LOG.warn("Could not validate bean manager", e);
    }
  }

  protected void startCreateImmediatelyBeans() {
    ((BeanManagerImplementor) m_beanContext).startCreateImmediatelyBeans();
  }

  @Override
  public void stop() {
    changeState(State.PlatformStopping, true);

    m_platformLock.writeLock().lock();
    try {
      if (Platform.get() == this) {
        Platform.set(null);
      }
      changeState(State.PlatformStopped, false);
      destroyBeanManager();
    }
    finally {
      m_platformLock.writeLock().unlock();
    }
  }

  protected void destroyBeanManager() {
    m_beanContext = null;
  }

  protected void changeState(State newState, boolean throwOnIllegalStateChange) {
    if (newState == null) {
      throw new IllegalArgumentException("new state cannot be null.");
    }
    if (m_state.get() == newState) {
      return;
    }

    EnumSet<State> possibleExpectedCurrentStates = getPreviousStates(newState);
    if (possibleExpectedCurrentStates == null || possibleExpectedCurrentStates.size() == 0) {
      throw new IllegalStateException("Unknown state transition: '" + newState + "' has no preceeding state defined.");
    }

    boolean changed = false;
    for (State expectedCurrentState : possibleExpectedCurrentStates) {
      changed = m_state.compareAndSet(expectedCurrentState, newState);
      if (changed) {
        break;
      }
    }
    if (!changed && throwOnIllegalStateChange) {
      throw new PlatformException(
          "Invalid state change. Current state (" + m_state.get() + ") cannot be changed to " + newState + ". A state change to " + newState + " is only allowed in these states " + possibleExpectedCurrentStates);
    }
    fireStateEvent(newState);
  }

  protected static EnumSet<State> getPreviousStates(State reference) {
    switch (reference) {
      case BeanManagerPrepared:
        return EnumSet.of(State.PlatformStopped);
      case BeanManagerValid:
        return EnumSet.of(State.BeanManagerPrepared);
      case PlatformStarted:
        return EnumSet.of(State.BeanManagerValid);
      case PlatformStopping:
        return EnumSet.of(State.PlatformStarted);
      case PlatformStopped:
        return EnumSet.of(State.PlatformStopping);
      case PlatformInvalid:
        return EnumSet.of(State.BeanManagerPrepared, State.BeanManagerValid, State.PlatformStarted, State.PlatformStopping, State.PlatformStopped);
    }
    return EnumSet.noneOf(State.class);
  }

  protected void fireStateEvent(State newState) {
    PlatformEvent event = new PlatformEvent(this, newState);
    try {
      for (IBean<IPlatformListener> bean : m_beanContext.getBeans(IPlatformListener.class)) {
        IPlatformListener listener = bean.getInstance();
        listener.stateChanged(event);
      }
    }
    catch (RuntimeException | Error e) {
      LOG.error("Error during event listener notification.", e);
      changeState(State.PlatformInvalid, true);
      throw e;
    }
  }

  @Override
  public boolean inDevelopmentMode() {
    return CONFIG.getPropertyValue(PlatformDevModeProperty.class); // cannot be null
  }
}
