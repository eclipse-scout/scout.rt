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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;

/**
 * By default remote service access must be authorized. Typically by a {@link RemoteServiceAccessPermission}
 * <p>
 * In some cases however a service method requires no or very specialized local authorization. In these cases this
 * annotation is used to whitelist and mark these business cases and exclude from regular authorization.
 * <p>
 * Warning: This annotation therefore passes unauthorized calls to the annotated method!
 *
 * @since 6.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RemoteServiceWithoutAuthorization {
}
