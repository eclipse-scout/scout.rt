/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
