/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.rest.container.AntiCsrfContainerFilter;

/**
 * JAX-RS REST application implementation filtering list of provided {@link IRestResource} to {@link RestApplicationScopes#EXT} and excluding {@link AntiCsrfContainerFilter}.
 */
public class ExtRestApplication extends RestApplication {

  @Override
  protected boolean filterClass(Class<?> clazz) {
    return !AntiCsrfContainerFilter.class.isAssignableFrom(clazz) && isExtScope(clazz);
  }

  protected boolean isExtScope(Class<?> clazz) {
    RestApplicationScope annotation = clazz.getAnnotation(RestApplicationScope.class);
    if (annotation != null) {
      // check matching scope
      return ObjectUtility.isOneOf(RestApplicationScopes.EXT, annotation.value());
    }
    // include all non-IRestResource classes
    return !IRestResource.class.isAssignableFrom(clazz);
  }
}
