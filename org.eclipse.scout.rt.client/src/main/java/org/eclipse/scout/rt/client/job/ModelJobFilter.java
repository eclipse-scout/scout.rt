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
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.job.IJobChangeEvent;
import org.eclipse.scout.commons.job.IJobChangeEventFilter;
import org.eclipse.scout.rt.platform.cdi.OBJ;

/**
 *
 */
public class ModelJobFilter implements IJobChangeEventFilter {

  public static final IJobChangeEventFilter INSTANCE = new ModelJobFilter();

  private ModelJobFilter() {
  }

  @Override
  public boolean accept(IJobChangeEvent event) {
    return OBJ.get(IModelJobManager.class) == event.getSourceManager();
  }

}
