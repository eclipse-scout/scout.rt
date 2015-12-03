/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.job;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Provider used in 'shared' Plug-Ins to work on concrete <code>RunContexts</code>, e.g. for lookup calls.<br/>
 * TODO [dwi] try to eliminate this class
 *
 * @since 5.1
 */
@ApplicationScoped
public interface IRunContextProvider {

  /**
   * Creates a "snapshot" of the current calling context.
   */
  RunContext copyCurrent();

  /**
   * Creates an empty <code>RunContext</code>.
   */
  RunContext empty();
}
