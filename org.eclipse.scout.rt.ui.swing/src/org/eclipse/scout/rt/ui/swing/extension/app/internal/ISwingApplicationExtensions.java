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
package org.eclipse.scout.rt.ui.swing.extension.app.internal;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 * Instances of this interface manage the contributed swing application extensions (see extension-point
 * org.eclipse.scout.rt.ui.swing.appextensions).
 * 
 * @author awe
 */
public interface ISwingApplicationExtensions {

  /**
   * Reads contributed swing application extensions (SAE) from the extension points and sets the default SAE,
   * which is the SAE with the greatest ranking. Calls the <code>start()</code> method of each SAE.
   */
  void start();

  /**
   * Returns the swing environment of the currently active extension.
   * 
   * @return
   */
  ISwingEnvironment getEnvironment(); // TODO AWE: (swingAppExt) vermutlich kann man diese methode entfernen (getEnvironment)

  /**
   * Returns the client session of the currently active extension.
   * 
   * @return
   */
  IClientSession getClientSession(); // TODO AWE: (swingAppExt) vermutlich kann man diese methode entfernen (getClientSession)
  // wird eigentlich nur für den splash screen gebraucht. Die regel könnte einfach sein, dass die extension mit dem höchsten
  // ranking als default und somit für diese zwecke verwendet wird.

  /**
   * Shows the GUI of all registered swing app extensions.
   */
  void showGUI();

  /**
   * Whether or not start up has been successful. If not, getStartUpError() will return the cause of the error.
   * 
   * @return
   */
  boolean startUpSuccessful();

  /**
   * When startUpSuccessful returns false this method returns the cause of the start up error or null otherwise.
   * 
   * @return
   */
  Throwable getStartUpError();

  /**
   * Stops all registered swing application extensions.
   */
  void stop();

}
