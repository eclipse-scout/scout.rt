/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;

/**
 * By default remote service access is denied.
 * <p>
 * In some cases it is useful to grant remote access to a service with the exception of one or two methods. Then this
 * annotation is used.
 * <p>
 * This annotation finally disables access to the method (all methods if placed on the type level) and ignores any
 * present {@link RemoteServiceAccessPermission}s.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RemoteServiceAccessDenied {
}
