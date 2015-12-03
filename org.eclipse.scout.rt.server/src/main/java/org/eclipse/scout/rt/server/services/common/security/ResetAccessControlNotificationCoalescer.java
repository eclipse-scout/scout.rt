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
package org.eclipse.scout.rt.server.services.common.security;

import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.ICoalescer;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;

/**
 * @deprecated replaced with {@link InvalidateCacheNotification}. Will be removed in Scout 6.1. See {@link ICache}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class ResetAccessControlNotificationCoalescer implements ICoalescer<ResetAccessControlChangedNotification> {

  @Override
  public List<ResetAccessControlChangedNotification> coalesce(List<ResetAccessControlChangedNotification> notifications) {
    // reduce to one
    return CollectionUtility.arrayList(CollectionUtility.firstElement(notifications));
  }
}
