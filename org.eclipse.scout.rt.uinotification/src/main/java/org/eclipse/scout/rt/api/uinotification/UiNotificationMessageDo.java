/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import java.io.Serializable;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.api.data.uinotification.UiNotificationDo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.UiNotificationMessage")
public class UiNotificationMessageDo extends DoEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public DoValue<String> user() {
    return doValue("user");
  }

  public DoValue<UiNotificationDo> notification() {
    return doValue("notification");
  }

  public DoValue<Long> timeout() {
    return doValue("timeout");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationMessageDo withUser(String user) {
    user().set(user);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getUser() {
    return user().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationMessageDo withNotification(UiNotificationDo notification) {
    notification().set(notification);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo getNotification() {
    return notification().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationMessageDo withTimeout(Long timeout) {
    timeout().set(timeout);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Long getTimeout() {
    return timeout().get();
  }
}
