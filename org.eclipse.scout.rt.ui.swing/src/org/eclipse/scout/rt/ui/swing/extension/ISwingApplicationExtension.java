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
package org.eclipse.scout.rt.ui.swing.extension;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.app.IApplicationContext;
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
   * @return the unique ID of this swing application extension.
   */
  String getExtensionId();

  /**
   * Instantiates swing environment (called by the Swing / AWT thread).
   */
  void initializeSwing();

  /**
   * Hook method to execute something when the <code>start</code> methods runs.
   * 
   * @param context
   * @param progressMonitor
   * @throws Exception
   * @return exitCode. when null, the execStart method of the next extension is called, when != null the start up
   *         exits with the given exitCode, no other extensions are called.
   */
  Object execStart(IApplicationContext context, IProgressMonitor progressMonitor) throws Exception;

  /**
   * Hook method to execute something when the <code>startInSubject</code> method runs. This methods is called
   * within a <code>Subject.doAs()</code> call. By default you'd create an instance of a client session here.
   * 
   * @param context
   * @param progressMonitor
   * @throws Exception
   * @return exitCode. when null, the execStartInSubject method of the next extension is called, when != null the
   *         start up exits with the given exitCode, no other extensions are called.
   */
  Object execStartInSubject(IApplicationContext context, IProgressMonitor progressMonitor) throws Exception;

  /**
   * @return the client session instance.
   */
  IClientSession getClientSession();

  /**
   * @return the desktop instance.
   */
  IDesktop getDesktop();

  /**
   * @return the swing environment instance.
   */
  ISwingEnvironment getEnvironment();

}
