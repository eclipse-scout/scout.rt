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
package org.eclipse.scout.rt.client;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanScopeEvaluator;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientBeanScopeEvaluator implements IBeanScopeEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(ClientBeanScopeEvaluator.class);

  @Override
  public Object getCurrentScope() {
    IClientSession currentSession = ClientSessionProvider.currentSession();
    if (currentSession == null) {
      return null;
    }
    return currentSession.getClass();
  }

  @Override
  public <T> List<IBean<T>> filter(List<IBean<T>> candidates, Object currentScope) {
    List<IBean<T>> result = new ArrayList<IBean<T>>(candidates.size());
    for (IBean<T> bean : candidates) {
      Client clientAnnotation = bean.getBeanAnnotation(Client.class);
      //(from imo, 03.09.2015) this line is only for the mig-app. This breaks the general tunneltoserver conecpt which does NOT require a clientsession at all
      /*
      TunnelToServer tunnelToServerAnnotation = bean.getBeanAnnotation(TunnelToServer.class);
      if (clientAnnotation == null && tunnelToServerAnnotation == null) {
      */
      if (clientAnnotation == null) {
        // no filter -> accept
        result.add(bean);
      }
      else if (currentScope != null) {
        // check session scope
        Class<? extends IClientSession> expectedSession = null;
        if (clientAnnotation != null) {
          expectedSession = clientAnnotation.value();
        }
        /* (from imo) see comment above
        else {
          // @TunnelToServer does not specify a specific session class
          expectedSession = IClientSession.class;
        }
        */

        Class<?> scopeClass = (Class<?>) currentScope;
        if (expectedSession.isAssignableFrom(scopeClass)) {
          result.add(bean);
        }
        else {
          LOG.debug("Filtered out bean " + bean.toString() + " because it expects session '" + expectedSession.getName() + "' but was '" + scopeClass.getName() + "'.", new Exception());
        }
      }
      else if (IClientNotificationService.class.isAssignableFrom(bean.getBeanClazz())) {
        //TODO jgu REMOVE!!!: quickfix to allow client notifications without sessions
        result.add(bean);
      }
      else {
        LOG.debug("Filtered out " + bean.toString() + " because no client session exists in the current context.", new Exception());
      }
    }
    return result;
  }
}
