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
 * The command that decides how to continue after calling a contribution
 */
public enum ContributionCommand {
  /**
   * returning this value in a contributed method is the default and execution continues and calls the next extension.
   */
  Continue,
  /**
   * returning this value in a contributed method, disables calling further extensions
   * <p>
   * Be careful when returning this value, since it prevents other extensions from being called.
   */
  Stop,
}
