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
package org.eclipse.scout.rt.platform.cdi;

import org.eclipse.scout.commons.exception.InitializationException;
import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;
import org.eclipse.scout.rt.platform.internal.ScoutServiceLoader;

/**
 *
 */
public final class CDI {

  private static final CDI instance = new CDI();

  private BeanContext m_beanContext;

  private CDI() {
  }

  public static void start() {
    instance.startInternal();
  }

  public static synchronized IBeanContext getBeanContext() {
    return instance.getBeanContextInternal();
  }

  public synchronized IBeanContext getBeanContextInternal() {
    return m_beanContext;
  }

  private synchronized void startInternal() {
    if (m_beanContext != null) {
      throw new IllegalStateException("CDI is already initialized!");
    }
    BeanContext context = new BeanContext();
    registerBeans(context);
    // instantiate @CreateImmediatly beans
    instantiateCreateImmediately(context);
    m_beanContext = context;
  }

  /**
   *
   */
  private void registerBeans(IBeanContext context) {
    for (IBeanContributor contributor : ScoutServiceLoader.loadServices(IBeanContributor.class)) {
      contributor.contributeBeans(context);
    }
  }

  /**
   * @param context
   */
  private void instantiateCreateImmediately(BeanContext context) {
    for (IBean<?> bean : context.getAllRegisteredBeans()) {
      if (BeanContext.isCreateImmediately(bean)) {
        if (BeanContext.isApplicationScoped(bean)) {
          bean.get();
        }
        else {
          throw new InitializationException(String.format("Bean '%s' is marked with @CreateImmediately and is not application scoped (@ApplicationScoped) - unuseful configuration! ", bean.getBeanClazz()));
        }
      }
    }
  }

}
