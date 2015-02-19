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
package org.eclipse.scout.rt.shared.services.cdi;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.interceptor.InvocationContext;
import org.eclipse.scout.rt.shared.ISession;

/**
 *
 */
@SessionRequired
@Interceptor
public class SessionRequiredInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SessionRequiredInterceptor.class);

  @AroundInvoke
  public Object checkSession(InvocationContext context) throws Exception {
    IBean<?> bean = (IBean<?>) context.getContextData().get(IBean.class.getName());
    SessionRequired annotation = bean.getBeanAnnotation(SessionRequired.class);
    Class<? extends ISession> requiredSession = annotation.value();
    ISession session = ISession.CURRENT.get();
    if (session == null) {
      throw new IllegalStateException(String.format("Method '%s' call on '%s' is not inside a '%s'. ", context.getMethod().getName(), context.getTarget().getClass().getName(), requiredSession.getName()));
    }
    if (requiredSession.isInstance(session)) {
      try {
        return context.proceed();
      }
      catch (Exception e) {
        LOG.error(String.format("Could not proceed bean '%s' from interceptor.", context.getTarget().getClass().getName()), e);
        throw e;
      }
    }
    else {
      throw new IllegalStateException(String.format("The session '%s' is not instance of expected session'%s'. ", session.getClass().getName(), requiredSession.getName()));
    }
  }

}
