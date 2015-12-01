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
package org.eclipse.scout.rt.server.services.common.code;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.ICoalescer;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * @deprecated replaced with {@link InvalidateCacheNotification}. Will be removed in Scout 6.1. See {@link ICache}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class CodeTypeNotificationCoalescer implements ICoalescer<CodeTypeChangedNotification> {

  @Override
  public List<CodeTypeChangedNotification> coalesce(List<CodeTypeChangedNotification> notifications) {
    if (notifications.isEmpty()) {
      return CollectionUtility.emptyArrayList();
    }
    return CollectionUtility.arrayList(new CodeTypeChangedNotification(collectCodeTypeClasses(notifications)));
  }

  private Set<Class<? extends ICodeType<?, ?>>> collectCodeTypeClasses(List<CodeTypeChangedNotification> notifications) {
    Set<Class<? extends ICodeType<?, ?>>> codeTypeClasses = new HashSet<>();
    for (CodeTypeChangedNotification n : notifications) {
      codeTypeClasses.addAll(n.getCodeTypes());
    }
    return codeTypeClasses;
  }

}
