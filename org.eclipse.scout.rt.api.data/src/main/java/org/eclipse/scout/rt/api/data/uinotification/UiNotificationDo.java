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

import java.io.Serializable;
import java.util.Date;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.UiNotification")
public class UiNotificationDo extends DoEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<String> topic() {
    return doValue("topic");
  }

  public DoValue<IDoEntity> message() {
    return doValue("message");
  }

  public DoValue<String> nodeId() {
    return doValue("nodeId");
  }

  public DoValue<Date> creationTime() {
    return doValue("creationTime");
  }

  /**
   * Marks the notification as initial subscription notification.
   * This notification will be returned, if the topic in the request does not contain {@link TopicDo#lastNotifications()}.
   * The message is empty. The client is only supposed to store the id as lastNotificationId and discard the notification.
   */
  public DoValue<Boolean> subscriptionStart() {
    return doValue("subscriptionStart");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo withTopic(String topic) {
    topic().set(topic);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTopic() {
    return topic().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo withMessage(IDoEntity message) {
    message().set(message);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getMessage() {
    return message().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo withNodeId(String nodeId) {
    nodeId().set(nodeId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getNodeId() {
    return nodeId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo withCreationTime(Date creationTime) {
    creationTime().set(creationTime);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getCreationTime() {
    return creationTime().get();
  }

  /**
   * See {@link #subscriptionStart()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public UiNotificationDo withSubscriptionStart(Boolean subscriptionStart) {
    subscriptionStart().set(subscriptionStart);
    return this;
  }

  /**
   * See {@link #subscriptionStart()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getSubscriptionStart() {
    return subscriptionStart().get();
  }

  /**
   * See {@link #subscriptionStart()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public boolean isSubscriptionStart() {
    return nvl(getSubscriptionStart());
  }
}
