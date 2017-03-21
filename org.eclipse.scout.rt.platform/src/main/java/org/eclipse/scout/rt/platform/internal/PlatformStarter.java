/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal;

import org.eclipse.scout.rt.platform.IPlatform;

/**
 * Starts the main platform
 *
 * @since 5.1
 */
public class PlatformStarter extends Thread {
  private final IPlatform m_platform;

  public PlatformStarter(IPlatform platform) {
    m_platform = platform;
  }

  @Override
  public void run() {
    m_platform.start();
  }
}
