/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.maintenance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.rest.IRestResource;

/**
 * <p>Annotate a {@link IRestResource} to disable access if application is in maintenance mode ({@link MaintenanceModeFilter} is installed for annotated types/methods).</p>
 * <p>If annotation is used an implementation of {@link IMaintenanceModeService} must be provided (could be as simple as a bean holding a boolean attribute to enable/disable maintenance mode)</p>
 * <p>Annotation may be used on types and their methods, if it is provided on both of them the method's annotation is the relevant one (allowing to disable a resource type in general for maintenance mode and enable a specific method on the
 * type).</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MaintenanceMode {

  /**
   * @return true to disable access to this type/method in maintenance mode; false to ignore maintenance mode resp. always allow access
   */
  boolean disabled();
}
