/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing;

import java.io.InputStream;

/**
 * This interface is used to inject a a project specific configuration for the look and feel.
 * The look and feel needs to support it (for example: Rayo).
 * 
 * @author awe / jbr
 * @since 3.10.0-M2 (backported)
 */
public interface ILookAndFeelConfigurator {

  /**
   * Executed before the Look and Feel is applied. Allows to perform some global settings required for the look and
   * feel, like setting the anti-aliased properties.
   */
  void configure();

  /**
   * Returns the contents of a configuration file (in case the look and feel requires it).
   * 
   * @return file content
   */
  InputStream getConfigurationFile();

}
