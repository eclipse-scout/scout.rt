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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

/**
 * By default service tunnel access is denied.
 * <p>
 * In some cases it is useful to grant remote access to a service with the exception of one or two methods.
 * <p>
 * This annotation generally disables access to the method.
 * {@link IAccessControlService#checkServiceTunnelAccess(Class, java.lang.reflect.Method, Object[])} checks this
 * annotation.
 * <p>
 * This feature is in effect since eclipse 3.7, therefore policy enforcement is only activated using the server-side
 * property <i>org.eclipse.scout.service.security=true</i> In future releases this will be the default value.
 * <p>
 * Policy enforcement is done inside
 * {@link IAccessControlService#checkServiceTunnelAccess(Class, java.lang.reflect.Method, Object[])}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceTunnelAccessDenied {
}
