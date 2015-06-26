/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Adapter for the {@link Handler} class.
 *
 * @since 5.0-M2
 */
public class HandlerAdapter extends Handler {

  @Override
  public void publish(LogRecord record) {
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
  }

}
