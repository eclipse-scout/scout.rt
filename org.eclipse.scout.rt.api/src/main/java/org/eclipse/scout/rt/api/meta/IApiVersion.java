/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.meta;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Defines the version of the API. Can be used to check compatibility.
 * <p>
 * Implement this bean to provide a custom API version.
 */
@ApplicationScoped
public interface IApiVersion {

  /**
   * Returns a version string consisting of three components:
   * <p>
   * <code>[major].[minor].[micro]</code>
   * <ul>
   * <li><b>[major]</b>: A change in this component indicates a breaking API change (e.g. field renamed or removed).
   * <li><b>[minor]</b>: A change in this component indicates a non-breaking API change (e.g. field added).
   * <li><b>[micro]</b>: A change in this component indicates internal bug fixes without API changes.
   * </ul>
   */
  String getVersion();
}
