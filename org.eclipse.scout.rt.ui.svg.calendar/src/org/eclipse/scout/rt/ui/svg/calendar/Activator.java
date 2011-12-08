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
package org.eclipse.scout.rt.ui.svg.calendar;


import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.svg.calendar";

  private static Activator plugin;

  public Activator() {
  }

  public static Activator getDefault() {
    return plugin;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

}
