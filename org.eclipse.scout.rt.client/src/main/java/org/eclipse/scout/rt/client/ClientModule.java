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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.platform.IModule;
import org.eclipse.scout.rt.platform.cdi.OBJ;

/**
 *
 */
public class ClientModule implements IModule {

  @Override
  public void start() {
  }

  @Override
  public void stop() {
    OBJ.one(IModelJobManager.class).shutdown();
  }

}
