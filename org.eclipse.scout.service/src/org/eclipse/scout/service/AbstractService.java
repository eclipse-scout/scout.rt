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
package org.eclipse.scout.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.IServiceInitializer.ServiceInitializerResult;
import org.eclipse.scout.service.internal.Activator;
import org.osgi.framework.ServiceRegistration;

/**
 * Convenience {@link IService} implementation with support for config.ini
 * variable injection. see {@link ServiceUtility#injectConfigProperties(IService)}
 */
public abstract class AbstractService implements IService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractService.class);

  public AbstractService() {
  }

  /**
   * This default implementation calls the default initializer {@link DefaultServiceInitializer} which calls
   * {@link org.eclipse.scout.service.ServiceUtility#injectConfigParams}(this).
   * It ensures that properties are getting initialized. This method can be overwritten by
   * implementers. Implementers should aware the property injection is only done if the super call is made.
   */
  @Override
  public void initializeService(ServiceRegistration registration) {
    Activator activator = Activator.getDefault();
    if (activator == null || activator.getServicesExtensionManager() == null) {
      LOG.error("Could not initialize service. " + getClass().getName());
      return;
    }

    //get service initializers
    List<IServiceInitializer> initializers = new ArrayList<IServiceInitializer>();
    Collection<IServiceInitializerFactory> factories = activator.getServicesExtensionManager().getServiceIntializerFactories();
    for (IServiceInitializerFactory factory : factories) {
      IServiceInitializer serviceInitializer = factory.createInstance(this);
      if (serviceInitializer != null) {
        initializers.add(serviceInitializer);
      }
    }

    //sort with respect to priority
    final Comparator<IServiceInitializer> priorityComparator = new Comparator<IServiceInitializer>() {
      @Override
      public int compare(IServiceInitializer o1, IServiceInitializer o2) {
        return (int) ((o1.getRunOrder() - o2.getRunOrder()) * 100);
      }
    };
    Collections.sort(initializers, priorityComparator);

    //execute service initializers until an initializer returns stop
    for (IServiceInitializer initializer : initializers) {
      ServiceInitializerResult res = initializer.initializeService(this);
      if (ServiceInitializerResult.STOP.equals(res)) {
        break;
      }
    }
  }

  /**
   * This default implementation does nothing
   */
  public void disposeServices() {
  }
}
