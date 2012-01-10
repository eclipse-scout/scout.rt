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
package org.eclipse.scout.rt.ui.rap.busy;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.AbstractBusyHandler;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Default SWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class RwtBusyHandler extends AbstractBusyHandler {

  /**
   * Use this key to set a {@link Control} on {@link IClientSession#setData(String, Object)}.
   * <p>
   * This control is set visible/invisible when busy in on/off.
   */
  public static final String BUSY_CONTROL_CLIENT_SESSION_KEY = "RwtBusyHandler.busyControl";

  /**
   * This is the CSS key used to style the busy control in rwt
   */
  public static final String CUSTOM_VARIANT_CSS_NAME = "busyIndicator";

  private final IRwtEnvironment m_env;
  private final Display m_display;

  public RwtBusyHandler(IClientSession session, IRwtEnvironment env) {
    super(session);
    m_env = env;
    m_display = env.getDisplay();
  }

  public IRwtEnvironment getUiEnvironment() {
    return m_env;
  }

  public Display getDisplay() {
    return m_display;
  }

  @Override
  protected void runBusy() {
    new RwtBusyWaitJob(TEXTS.get("BusyJob"), this).schedule();
  }
}
