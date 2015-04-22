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
package org.eclipse.scout.rt.platform.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.IServiceInitializer.ServiceInitializerResult;

/**
 * Convenience {@link IService} implementation with support for {@link IServiceInitializer}s.
 */
public abstract class AbstractService implements IService {

  /**
   * This default implementation calls all {@link IServiceInitializer}s.
   */
  @PostConstruct
  protected void initializeService() {
    for (IServiceInitializer i : BEANS.all(IServiceInitializer.class)) {
      ServiceInitializerResult res = i.initializeService(this);
      if (ServiceInitializerResult.STOP.equals(res)) {
        break;
      }
    }
  }

  /**
   * This default implementation does nothing
   */
  @PreDestroy
  public void disposeServices() {
  }
}
