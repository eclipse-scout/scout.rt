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

public interface ServletConstants {

  String API_PATH = "/api";

  String API_PATH_WITH_WILDCARD = ServletConstants.API_PATH + "/*";

  String AUTH_PATH = "/auth";

  String AUTH_PATH_WITH_WILDCARD = ServletConstants.AUTH_PATH + "/*";
}
