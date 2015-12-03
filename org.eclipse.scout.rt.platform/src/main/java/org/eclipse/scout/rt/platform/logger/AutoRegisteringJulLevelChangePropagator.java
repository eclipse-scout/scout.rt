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
package org.eclipse.scout.rt.platform.logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.jul.LevelChangePropagator;

/**
 * {@link LevelChangePropagator} extension that automatically registers the {@link SLF4JBridgeHandler} on startup.
 * Further, this implementation removes all handlers that were registered on JUL's root logger by default.
 * <p/>
 * <b>Registration</b>: Add the following line to your logback.xml configuration file.
 *
 * <pre>
 * &lt;contextListener class="org.eclipse.scout.rt.platform.logger.logback.AutoRegisteringJulLevelChangePropagator"/&gt;
 * </pre>
 *
 * The optional nested element <em>removeRootHandlers</em> controls whether handlers are removed from the root logger.
 * Its default value is <code>true</code>.
 *
 * <pre>
 * &lt;contextListener class="org.eclipse.scout.rt.platform.logger.logback.AutoRegisteringJulLevelChangePropagator"&gt;
 *   &lt;removeRootHandlers&gt;false&lt;/removeRootHandlers&gt;
 * &lt;/contextListener&gt;
 * </pre>
 *
 * @since 5.1
 */
public class AutoRegisteringJulLevelChangePropagator extends LevelChangePropagator {

  private boolean m_removeRootHandlers = true;

  public boolean isRemoveRootHandlers() {
    return m_removeRootHandlers;
  }

  public void setRemoveRootHandlers(boolean removeRootHandlers) {
    m_removeRootHandlers = removeRootHandlers;
  }

  @Override
  public void start() {
    if (isRemoveRootHandlers()) {
      addInfo("removing all handlers for java.util.logging root logger");
      SLF4JBridgeHandler.removeHandlersForRootLogger();
    }
    SLF4JBridgeHandler.install();
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
    SLF4JBridgeHandler.uninstall();
  }
}
