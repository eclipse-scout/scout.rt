/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class RwtApplication implements IApplication {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtApplication.class);

  @Override
  public Object start(final IApplicationContext context) throws Exception {

    RWT.getSessionStore().addSessionStoreListener(new SessionStoreListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void beforeDestroy(SessionStoreEvent event) {
        HttpServletRequest request = RWT.getRequest();
        String userAgent = "";
        if (request != null) {
          userAgent = request.getHeader("User-Agent");
        }
        String msg = "Thread: {0} Session goes down...; SessionStoreEvent: {1}\n UserAgent: {2}";
        LOG.error(msg, new Object[]{Long.valueOf(Thread.currentThread().getId()), event.toString(), userAgent});
      }
    });

    return EXIT_OK;
  }

  @Override
  public void stop() {
  }

}
