/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
