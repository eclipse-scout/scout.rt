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
package org.eclipse.scout.rt.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanScopeEvaluator;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;

/**
 *
 */
public class ServerBeanScopeEvaluator implements IBeanScopeEvaluator {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerBeanScopeEvaluator.class);

  @Override
  public Object getCurrentScope() {
    IServerSession currentSession = ServerSessionProvider.currentSession();
    if (currentSession == null) {
      return null;
    }
    return currentSession.getClass();
  }

  @Override
  public <T> List<IBean<T>> filter(List<IBean<T>> candidates, Object currentScope) {
    List<IBean<T>> result = new ArrayList<IBean<T>>(candidates.size());
    for (IBean<T> bean : candidates) {
      Server annotation = bean.getBeanAnnotation(Server.class);
      if (annotation == null) {
        // no server filter -> accept
        result.add(bean);
      }
      else if (currentScope != null) {
        // check session scope
        Class<? extends IServerSession> expectedSession = annotation.value();
        Class<?> scopeClass = (Class<?>) currentScope;
        if (expectedSession.isAssignableFrom(scopeClass)) {
          result.add(bean);
        }
        else {
          LOG.debug("Filtered out bean " + bean.toString() + " because it expects session '" + expectedSession.getName() + "' but was '" + scopeClass.getName() + "'.");
        }
      }
      else {
        LOG.debug("Filtered out " + bean.toString() + " because no server session exists in the current context.", new Exception());
      }
    }
    return result;
  }

}
