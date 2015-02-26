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

import org.eclipse.scout.rt.platform.cdi.IBeanContributor;

/**
 * A {@link IModule} can be used to take part at the scout platform's lifecycle.
 * To register a module add add it to the CDI context using {@link IBeanContributor}.
 */
public interface IModule {

  /**
   * Is called during startup of the {@link Platform}. The CDI context is set up and available at this time.
   */
  void start();

  /**
   * Is called before the {@link Platform} shut down.
   */
  void stop();
}
