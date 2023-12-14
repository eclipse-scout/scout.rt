/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.internal;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.config.IConfigurationValidator;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.1
 */
public class PlatformImplementor implements IPlatform {

  public static final String SCOUT_HEADLESS_PROPERTY = "scout.headless";
  public static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

  private static final LazyValue<Boolean> IN_DEVELOPMENT_MODE = new LazyValue<>(() -> BooleanUtility.nvl(CONFIG.getPropertyValue(PlatformDevModeProperty.class)));
  private static final Logger LOG = LoggerFactory.getLogger(PlatformImplementor.class);

  private final ReentrantReadWriteLock m_platformLock = new ReentrantReadWriteLock(true);
  private volatile CountDownLatch m_platformStarted = new CountDownLatch(1);
  private volatile CountDownLatch m_platformStarting = new CountDownLatch(1);
  private final AtomicReference<State> m_state; // may be read at any time by any thread
  private BeanManagerImplementor m_beanManager;
  private volatile boolean m_initializedSuccessfully;

  public PlatformImplementor() {
    m_state = new AtomicReference<>(State.PlatformStopped);
  }

  @Override
  public State getState() {
    return m_state.get();
  }

  @Override
  public IBeanManager getBeanManager() {
    if (m_initializedSuccessfully) {
      return m_beanManager;
    }
    // use lock to ensure the caller waits until the platform has been started completely
    m_platformLock.readLock().lock();
    try {
      throwOnPlatformInvalid();
      return m_beanManager;
    }
    finally {
      m_platformLock.readLock().unlock();
    }
  }

  @Override
  public void awaitPlatformStarted() {
    awaitLatchSafe(m_platformStarted);
    throwOnPlatformInvalid();
  }

  @Override
  public void awaitPlatformStarting() {
    awaitLatchSafe(m_platformStarting);
  }

  protected void throwOnPlatformInvalid() {
    if (getState() == State.PlatformInvalid) {
      throw new PlatformException("The platform is in an invalid state.");
    }
  }

  protected static void awaitLatchSafe(CountDownLatch latch) {
    boolean interrupted;
    do {
      interrupted = false;
      try {
        latch.await();
      }
      catch (InterruptedException e) {
        interrupted = true;
      }
    }
    while (interrupted);
  }

  protected void notifyPlatformStarted() {
    m_platformStarted.countDown();
  }

  protected void notifyPlatformStarting() {
    m_platformStarting.countDown();
  }

  @Override
  @SuppressWarnings("squid:S1181")
  public void start() {
    try {
      m_platformLock.writeLock().lock();
      try {
        notifyPlatformStarting();
        if (m_state.get() != State.PlatformStopped) {
          throw new PlatformException("Platform is not stopped [m_state=" + m_state.get() + "]");
        }

        try {
          validateHeadless();
          m_beanManager = createBeanManager();
          //now all IPlatformListener are registered and can receive platform events
          changeState(State.BeanManagerPrepared, true);

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
      m_initializedSuccessfully = true;
    }
    finally {
      notifyPlatformStarted();
    }
  }

  protected void validateHeadless() {
    final boolean scoutHeadless = ConfigUtility.getPropertyBoolean(SCOUT_HEADLESS_PROPERTY, true);
    String awtHeadlessStr = System.getProperty(AWT_HEADLESS_PROPERTY);
    final boolean awtHeadless = TypeCastUtility.castValue(awtHeadlessStr, boolean.class);
    String autoSetMsg = "";
    if (scoutHeadless && !awtHeadless) {
      System.setProperty(AWT_HEADLESS_PROPERTY, "true");
      autoSetMsg = " (automatically set by Scout)";
      awtHeadlessStr = "true";
    }
    boolean graphicsEnvironmentHeadless = GraphicsEnvironment.isHeadless();
    LOG.info("Headless mode: {}={}, {}={}{}, GraphicsEnvironment.isHeadless()={}", SCOUT_HEADLESS_PROPERTY, scoutHeadless,
        AWT_HEADLESS_PROPERTY, awtHeadlessStr, autoSetMsg, graphicsEnvironmentHeadless);

    if (scoutHeadless && !graphicsEnvironmentHeadless) {
      LOG.warn("{} is 'true', but GraphicsEnvironment.isHeadless() reports 'false'. AWT seems to have been already initialized. "
          + "Please try setting the system property {}=true manually when starting the VM. You can turn off this message by setting {}=false",
          SCOUT_HEADLESS_PROPERTY, AWT_HEADLESS_PROPERTY, SCOUT_HEADLESS_PROPERTY);
    }
  }

  protected void validateConfiguration() {
    if (!ConfigUtility.hasProviderProperties()) {
      LOG.info("No {} found. Running with empty configuration.", ConfigUtility.DEFAULT_PATH_TO_CONFIG_FILE);
      return;
    }
    final List<IConfigurationValidator> validators = BEANS.all(IConfigurationValidator.class);
    final List<String> invalidProperties = new ArrayList<>();
    for (Entry<String, String> config : ConfigUtility.getAllEntries().entrySet()) {
      if (!isConfigEntryValid(validators, config)) {
        invalidProperties.add(config.getKey());
        LOG.error("Config property with key '{}' does not exist or has an invalid value.", config.getKey());
      }
    }
    if (!invalidProperties.isEmpty()) {
      throw new PlatformException("Cannot start platform due to {} invalid config properties: {}", invalidProperties.size(), invalidProperties);
    }
  }

  protected boolean isConfigEntryValid(List<IConfigurationValidator> validators, Entry<String, String> config) {
    for (IConfigurationValidator validator : validators) {
      if (validator.isValid(config.getKey(), config.getValue())) {
        return true;
      }
    }
    return false;
  }

  protected BeanManagerImplementor newBeanManagerImplementor() {
    return new BeanManagerImplementor();
  }

  protected BeanManagerImplementor createBeanManager() {
    BeanManagerImplementor beanManager = newBeanManagerImplementor();
    IClassInventory inv = ClassInventory.get();
    long t0 = System.nanoTime();
    Set<Class> allBeans = new BeanFilter().collect(inv);
    long t1 = System.nanoTime();
    LOG.info("Collected {} beans in {} ms", allBeans.size(), StringUtility.formatNanos(t1 - t0));
    for (Class<?> bean : allBeans) {
      beanManager.registerClass(bean);
    }
    long t2 = System.nanoTime();
    LOG.info("Registered {} beans in {} ms", allBeans.size(), StringUtility.formatNanos(t2 - t1));
    return beanManager;
  }

  protected void initBeanDecorationFactory() {
    if (m_beanManager.getBeanDecorationFactory() != null) {
      return;
    }
    IBean<IBeanDecorationFactory> bean = m_beanManager.optBean(IBeanDecorationFactory.class);
    if (bean != null) {
      m_beanManager.setBeanDecorationFactory(bean.getInstance());
      return;
    }
    LOG.warn("Using {}. Please verify that this application really has no client or server side {}", SimpleBeanDecorationFactory.class.getName(), IBeanDecorationFactory.class.getSimpleName());
    m_beanManager.setBeanDecorationFactory(new SimpleBeanDecorationFactory());
  }

  protected void startCreateImmediatelyBeans() {
    m_beanManager.startCreateImmediatelyBeans();
  }

  @Override
  public void stop() {
    m_beanManager.callPreDestroyOnBeans();
    changeState(State.PlatformStopping, true);

    m_initializedSuccessfully = false;
    m_platformLock.writeLock().lock();
    try {
      changeState(State.PlatformStopped, false);
      m_platformStarted = new CountDownLatch(1);
      m_platformStarting = new CountDownLatch(1);
      destroyBeanManager();
    }
    finally {
      m_platformLock.writeLock().unlock();
    }
  }

  protected void destroyBeanManager() {
    m_beanManager = null;
  }

  protected void changeState(State newState, boolean throwOnIllegalStateChange) {
    if (newState == null) {
      throw new IllegalArgumentException("new state cannot be null.");
    }
    if (m_state.get() == newState) {
      return;
    }

    EnumSet<State> possibleExpectedCurrentStates = getPreviousStates(newState);
    if (possibleExpectedCurrentStates == null || possibleExpectedCurrentStates.isEmpty()) {
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

  @SuppressWarnings("squid:S1181")
  protected void fireStateEvent(State newState) {
    if (m_beanManager == null) {
      return; // can happen if there is an error creating the bean manager. cannot move to status invalid. just do nothing.
    }
    PlatformEvent event = new PlatformEvent(this, newState);
    for (IBean<IPlatformListener> bean : m_beanManager.getBeans(IPlatformListener.class)) {
      try {
        IPlatformListener listener = bean.getInstance();
        long t0 = System.nanoTime();
        listener.stateChanged(event);
        long t1 = System.nanoTime();
        if (LOG.isDebugEnabled()) {
          LOG.debug("StateEvent {} took {} ms for '{}' ", newState, StringUtility.formatNanos(t1 - t0), bean);
        }
      }
      catch (RuntimeException | Error e) {
        LOG.error("Error during event listener notification.", e);
        switch (newState) {
          case PlatformStopping:
          case PlatformStopped:
            //go on
            break;
          default:
            changeState(State.PlatformInvalid, true);
            throw e;
        }
      }
    }
  }

  @Override
  public boolean inDevelopmentMode() {
    return IN_DEVELOPMENT_MODE.get(); // cannot be null
  }
}
