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
package org.eclipse.scout.rt.client;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public class JobChangeAdapterEx extends JobChangeAdapter implements IJobChangeListenerEx {

  @Override
  public void blockingConditionEnd(IJobChangeEvent event) {
  }

  @Override
  public void blockingConditionStart(IJobChangeEvent event) {
  }
}
