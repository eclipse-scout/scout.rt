/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.notification;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

public class Notification extends AbstractWidget implements INotification {

  private final IStatus m_status;

  /**
   * Creates a simple info notification with a text.
   */
  public Notification(String text) {
    this(new Status(text, IStatus.INFO));
  }

  /**
   * Creates a notification with a status.
   */
  public Notification(IStatus status) {
    m_status = status;
  }

  @Override
  public IStatus getStatus() {
    return m_status;
  }
}
