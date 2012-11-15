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

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.service.IService;

/**
 * Indicates whether the osgi is running in back-end, front-end or undetermined
 * <p>
 * see {@link IService} for description of this indicator
 * <p>
 * When scout.osgi.tier is not set, the default value is automatically determined.
 * <ul>
 * <li>exists(org.eclipse.scout.rt.ui.swing) or exists(org.eclipse.scout.rt.ui.swt) -&gt; FrontEnd</li>
 * <li>exists(org.eclipse.scout.rt.client) and not exists(org.eclipse.scout.rt.server) -&gt; FrontEnd</li>
 * <li>not exists(org.eclipse.scout.rt.ui.swing) and not exists(org.eclipse.scout.rt.ui.swt) and not
 * exists(org.eclipse.scout.rt.client) and exists(org.eclipse.scout.rt.server)-&gt; BackEnd</li>
 * <li>all other cases -&gt; Undefined</li>
 * </ul>
 */
public final class TierState {
  private static final Tier VALUE;

  static {
    String s;
    if (Activator.getDefault() != null) {
      s = Activator.getDefault().getBundle().getBundleContext().getProperty("scout.osgi.tier");
    }
    else {
      s = System.getProperty("scout.osgi.tier");
    }
    if ("frontend".equals(s)) {
      VALUE = Tier.FrontEnd;
    }
    else if ("backend".equals(s)) {
      VALUE = Tier.BackEnd;
    }
    else {
      //auto detect
      boolean hasClient = Platform.getBundle("org.eclipse.scout.rt.client") != null;
      boolean hasServer = Platform.getBundle("org.eclipse.scout.rt.server") != null;
      boolean hasSwing = Platform.getBundle("org.eclipse.scout.rt.ui.swing") != null;
      boolean hasSwt = Platform.getBundle("org.eclipse.scout.rt.ui.swt") != null;
      if (hasSwing || hasSwt) {
        VALUE = Tier.FrontEnd;
      }
      else if (hasClient && !hasServer) {
        VALUE = Tier.FrontEnd;
      }
      else if (hasServer && !hasClient && !hasSwing && !hasSwt) {
        VALUE = Tier.BackEnd;
      }
      else {
        VALUE = Tier.Undefined;
      }
    }
  }

  public static enum Tier {
    FrontEnd, BackEnd, Undefined
  }

  private TierState() {
  }

  public static Tier get() {
    return VALUE;
  }
}
