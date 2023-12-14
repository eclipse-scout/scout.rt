/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.uinotification;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.UiNotificationResponse")
public class UiNotificationResponse extends DoEntity {
  public DoList<UiNotificationDo> notifications() {
    return doList("notifications");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationResponse withNotifications(Collection<? extends UiNotificationDo> notifications) {
    notifications().updateAll(notifications);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationResponse withNotifications(UiNotificationDo... notifications) {
    notifications().updateAll(notifications);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<UiNotificationDo> getNotifications() {
    return notifications().get();
  }
}
