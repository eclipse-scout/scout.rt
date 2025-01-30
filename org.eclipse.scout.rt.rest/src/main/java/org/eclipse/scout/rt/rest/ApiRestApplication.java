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

/**
 * JAX-RS REST application implementation filtering list of provided {@link IRestResource} to {@link RestApplicationScopes#API} or empty scope.
 */
public class ApiRestApplication extends RestApplication {

  @Override
  protected boolean filterClass(Class<?> clazz) {
    RestApplicationScope annotation = clazz.getAnnotation(RestApplicationScope.class);
    return annotation == null || ObjectUtility.isOneOf(RestApplicationScopes.API, annotation.value());
  }
}
