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
package org.eclipse.scout.rt.ui.rap.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.rap.core";

  private static final String CLIENT_LIBRARY_VARIANT = "org.eclipse.rwt.clientLibraryVariant";
  private static final String DEBUG_CLIENT_LIBRARY_VARIANT = "DEBUG";

  private static Activator m_plugin;

  public Activator() {
    //[imo] js patching mode. Rebuild client.js before removing these lines of code!! - SLE client.js is patched
//    System.out.println("Debug does not use compiled client.js; Setting " + CLIENT_LIBRARY_VARIANT + "=" + DEBUG_CLIENT_LIBRARY_VARIANT);
//    System.setProperty(CLIENT_LIBRARY_VARIANT, DEBUG_CLIENT_LIBRARY_VARIANT);
  }

  public static Activator getDefault() {
    return m_plugin;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin = null;
    super.stop(context);
  }
}
