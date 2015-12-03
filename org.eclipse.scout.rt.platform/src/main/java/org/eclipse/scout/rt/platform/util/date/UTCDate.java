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
package org.eclipse.scout.rt.platform.util.date;

import java.util.Date;

/**
 * UTC date marker subclass as an exception to StaticDate conversion in {@link ServiceTunnelObjectReplacer}.
 */
public class UTCDate extends Date {
  private static final long serialVersionUID = 1L;

  public UTCDate() {
    super();
  }

  public UTCDate(long utcTime) {
    super(utcTime);
  }
}
