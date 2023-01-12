/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

/**
 * Describes the action that should be used by the UI to handle the URI in the desktop's "open URI" feature.
 */
@FunctionalInterface
public interface IOpenUriAction {

  /**
   * @return the identifier (known to the UI) for this action.
   */
  String getIdentifier();
}
