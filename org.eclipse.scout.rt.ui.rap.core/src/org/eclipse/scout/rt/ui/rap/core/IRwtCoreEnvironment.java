/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.core;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.swt.widgets.Display;

public interface IRwtCoreEnvironment {

  Display getDisplay();

  IClientSession getClientSession();

  LayoutValidateManager getLayoutValidateManager();

  /**
   * calling from swt thread
   * <p>
   * The job is only run when it reaches the model within the cancelTimeout. This means if the job is delayed longer
   * than cancelTimeout millis when the model job runs it, then the job is ignored.
   * 
   * @return the created and scheduled job, a {@link ClientJob}
   */
  JobEx invokeScoutLater(Runnable job, long cancelTimeout);

  void invokeUiLater(Runnable job);

  void addEnvironmentListener(IRwtEnvironmentListener listener);

  void removeEnvironmentListener(IRwtEnvironmentListener listener);

}
