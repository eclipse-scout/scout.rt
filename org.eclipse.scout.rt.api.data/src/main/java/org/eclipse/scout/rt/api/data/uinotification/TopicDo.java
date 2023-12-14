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
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.Topic")
public class TopicDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoList<UiNotificationDo> lastNotifications() {
    return doList("lastNotifications");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TopicDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TopicDo withLastNotifications(Collection<? extends UiNotificationDo> lastNotifications) {
    lastNotifications().updateAll(lastNotifications);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TopicDo withLastNotifications(UiNotificationDo... lastNotifications) {
    lastNotifications().updateAll(lastNotifications);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<UiNotificationDo> getLastNotifications() {
    return lastNotifications().get();
  }
}
