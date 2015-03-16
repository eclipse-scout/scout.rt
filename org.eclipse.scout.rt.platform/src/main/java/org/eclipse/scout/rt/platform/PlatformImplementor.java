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
package org.eclipse.scout.rt.platform;

import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder;
import org.jboss.jandex.Index;

/**
 * @since 15.2
 */
public class PlatformImplementor implements IPlatform {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PlatformImplementor.class);

  private State m_state = State.PlatformStopped;
  private IClassInventory m_classInventory;
  private IBeanContext m_beanContext;
  private IApplication m_application;

  public PlatformImplementor() {
  }

  @Override
  public State getState() {
    return m_state;
  }

  @Override
  public IClassInventory getClassInventory() {
    return m_classInventory;
  }

  @Override
  public IBeanContext getBeanContext() {
    return m_beanContext;
  }

  @Override
  public synchronized void start() {
    m_classInventory = createClassInventory();
    m_beanContext = createBeanContext();
    //now all IPlatformListener are registered and can receive platform events

    changeState(State.PlatformInit);
    changeState(State.ClassInventoryValid);
    changeState(State.BeanContextPrepared);
    changeState(State.BeanContextValid);
    startCreateImmediatelyBeans();

    changeState(State.ApplicationStarting);
    startApplication();
    changeState(State.ApplicationStarted);
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

  protected IBeanContext createBeanContext() {
    BeanContext context = new BeanContext();
    Set<Class> allBeans = new BeanFilter().collect(getClassInventory());
    for (Class<?> bean : allBeans) {
      context.registerClass(bean);
    }
    context.initBeanInstanceFactory();
    return context;
  }

  protected void startCreateImmediatelyBeans() {
    ((BeanContext) m_beanContext).startCreateImmediatelyBeans();
  }

  protected void startApplication() {
    m_application = OBJ.oneOrNull(IApplication.class);
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
    m_state = newState;
    fireStateEvent(newState);
  }

  protected void verifyStateChange(State oldState, State newState) {
    if (oldState == State.PlatformInit && newState == State.ClassInventoryValid) {
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
    for (IPlatformListener l : m_beanContext.getInstances(IPlatformListener.class)) {
      try {
        l.stateChanged(e);
      }
      catch (Throwable t) {
        LOG.warn(IPlatformListener.class.getSimpleName() + " " + l.getClass(), t);
      }
    }
  }
}
