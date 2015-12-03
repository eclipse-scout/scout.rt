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
package org.eclipse.scout.rt.testing.server.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.server.clientnotification.IClientNodeId;
import org.eclipse.scout.rt.server.clientnotification.TransactionalClientNotificationCollector;

/**
 * Annotation to set {@link ThreadLocal} for client notifications.
 * <ul>
 * <li>{@link IClientNodeId#CURRENT}</li>
 * <li>{@link TransactionalClientNotificationCollector#CURRENT}</li>
 * </ul>
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface RunWithClientNotifications {

  /**
   * @return client node ID to be used.
   */
  String clientNodeId() default "test";
}
