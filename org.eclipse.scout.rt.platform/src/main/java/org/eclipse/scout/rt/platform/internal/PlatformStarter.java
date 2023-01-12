/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    super("scout-platform-starter");
    m_platform = platform;
  }

  @Override
  public void run() {
    m_platform.start();
  }
}
