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
package org.eclipse.scout.rt.ui.swing.extension.app;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 * Instances of this interface are used to launch multiple scout swing-applications in a single eclipse .product.
 * Each swing application extension has its own client session, swing environment and desktop.
 * 
 * @author awe
 */
public interface ISwingApplicationExtension {

  /**
   * Returns the unique ID of this swing application extension.
   * 
   * @return
   */
  String getExtensionId();

  /**
   * Instantiates client session, swing environment and desktop.
   */
  void start();

  /**
   * Returns the client session instance.
   * 
   * @return
   */
  IClientSession getClientSession();

  /**
   * Returns the desktop instance.
   * 
   * @return
   */
  IDesktop getDesktop();

  /**
   * Returns the swing environment instance.
   * 
   * @return
   */
  ISwingEnvironment getEnvironment();

}
