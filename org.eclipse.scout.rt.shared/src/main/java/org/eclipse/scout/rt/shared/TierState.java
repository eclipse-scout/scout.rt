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

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.IService;

/**
 * Indicates whether the osgi is running in back-end, front-end or undetermined
 * <p>
 * see {@link IService} for description of this indicator
 * <p>
 * When scout.osgi.tier is not set, the default value is automatically determined. </ul>
 */
public final class TierState {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TierState.class);
  private static final Tier VALUE;

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

  static {
    String s = ConfigIniUtility.getProperty("scout.osgi.tier");
    if (StringUtility.isNullOrEmpty(s)) {
      s = ConfigIniUtility.getProperty("scout.tier");
    }
    if ("frontend".equalsIgnoreCase(s)) {
      VALUE = Tier.FrontEnd;
    }
    else if ("backend".equalsIgnoreCase(s)) {
      VALUE = Tier.BackEnd;
    }
    else if ("undefined".equalsIgnoreCase(s)) {
      VALUE = Tier.Undefined;
    }
    else {
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
        VALUE = Tier.FrontEnd;
      }
      else if (hasServer) {
        VALUE = Tier.BackEnd;
      }
      else {
        VALUE = Tier.Undefined;
      }
    }
  }

  private TierState() {
  }

}
