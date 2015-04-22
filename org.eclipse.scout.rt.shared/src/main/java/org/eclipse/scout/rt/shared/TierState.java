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
package org.eclipse.scout.rt.shared;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.SharedConfigProperties.TierProperty;

/**
 * Indicates whether the application is running in back-end, front-end or undetermined
 * <p>
 * When <code>scout.tier</code> is not set, the default value is automatically detected.
 *
 * @see TierProperty
 * @see IService
 */
public final class TierState {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TierState.class);
  private static final Tier VALUE;

  static {
    Tier result = CONFIG.getPropertyValue(TierProperty.class);
    if (result == null) {
      //auto detect
      boolean hasClient = false;
      boolean hasServer = false;
      try {
        Class.forName("org.eclipse.scout.rt.client.IClientSession");
        hasClient = true;
      }
      catch (Exception ex) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("detect TierState: no IClientSession");
        }
      }
      try {
        Class.forName("org.eclipse.scout.rt.server.IServerSession");
        hasServer = true;
      }
      catch (Exception ex) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("detect TierState: no IServerSession");
        }
      }

      if (hasClient) {
        result = Tier.FrontEnd;
      }
      else if (hasServer) {
        result = Tier.BackEnd;
      }
      else {
        result = Tier.Undefined;
      }
    }
    VALUE = result;
  }

  public static Tier get() {
    return VALUE;
  }

  public static enum Tier {
    /**
     * scout client on classpath
     */
    FrontEnd,
    /**
     * scout server on classpath
     */
    BackEnd,
    /**
     * neither client nor server found on classpath
     */
    Undefined
  }

  private TierState() {
  }
}
