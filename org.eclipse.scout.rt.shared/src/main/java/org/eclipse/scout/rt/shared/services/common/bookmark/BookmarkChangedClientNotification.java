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
package org.eclipse.scout.rt.shared.services.common.bookmark;

import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 *
 */
public class BookmarkChangedClientNotification extends AbstractClientNotification {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean coalesce(IClientNotification existingNotification) {
    return true;
  }
}
