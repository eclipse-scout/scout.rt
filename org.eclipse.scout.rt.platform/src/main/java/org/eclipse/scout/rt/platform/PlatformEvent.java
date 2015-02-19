/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.util.EventObject;

/**
 *
 */
public class PlatformEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int ABOUT_TO_START = 1;
  public static final int MODULES_STARTED = 2;
  public static final int STARTED = 3;
  public static final int ABOUT_TO_STOP = 4;
  public static final int STOPPED = 5;

  private int m_eventType;

  PlatformEvent(IPlatform platform, int eventType) {
    super(platform);
    m_eventType = eventType;
  }

  @Override
  public IPlatform getSource() {
    return (IPlatform) super.getSource();
  }

  public int getEventType() {
    return m_eventType;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Platform Event: ");
    switch (getEventType()) {
      case ABOUT_TO_START:
        builder.append("ABOUT_TO_START");
        break;
      case MODULES_STARTED:
        builder.append("MODULES_STARTED");
        break;
      case STARTED:
        builder.append("STARTED");
        break;
      case ABOUT_TO_STOP:
        builder.append("ABOUT_TO_STOP");
        break;
      case STOPPED:
        builder.append("STOPPED");
        break;
      default:
        builder.append("undefined");
        break;
    }
    return builder.toString();
  }
}
