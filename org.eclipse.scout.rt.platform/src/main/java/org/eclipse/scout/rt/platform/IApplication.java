/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

/**
 * The application can be registered in the MANIFEST.MF (ensure the manifest to be on the classpath). Exactly one
 * application can be registered. If there are multiple applications registered an exception will be thrown.
 */
public interface IApplication {
  /**
   * The Manifest.MF entry to provide an application.</br>
   *
   * <pre>
   * MANIFEST.MF
   * Scout-Application: fullyQuallifiedApplicationName
   * </pre>
   */
  static String MANIFEST_APPLICATION_ENTRY = "Scout-Application";

  void start();

  void stop();
}
