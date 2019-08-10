/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.notification;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A notification is used to show short information on a widget.
 *
 * @since 8.0
 */
public interface INotification extends IWidget {

  IStatus getStatus();

}
