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
package org.eclipse.scout.commons.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.CollationRulesPatch;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.commons";
  // The shared instance
  private static Activator m_plugin;

  public Activator() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
    CollationRulesPatch.patchDefaultCollationRules();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return m_plugin;
  }
}
