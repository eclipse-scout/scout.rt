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
package org.eclipse.scout.rt.spec.client;

import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;

/**
 *
 */
public abstract class AbstractSpecAssembly {
  public final SpecFileConfig m_fileConfig;

  public AbstractSpecAssembly(String pluginName) {
    m_fileConfig = new SpecFileConfig(pluginName);
  }

  protected SpecFileConfig getFileConfig() {
    return m_fileConfig;
  }
}
