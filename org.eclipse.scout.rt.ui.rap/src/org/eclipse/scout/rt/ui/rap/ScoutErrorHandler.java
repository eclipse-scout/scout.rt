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
package org.eclipse.scout.rt.ui.rap;

import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * Empty error handler as default to avoid exceptions thrown in WorkbenchErrorHandlerProxy during logging.
 */
public class ScoutErrorHandler extends AbstractStatusHandler {

  @Override
  public void handle(StatusAdapter statusAdapter, int style) {
  }

}
