/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.holders;

import java.util.Date;

/**
 * @since 3.0
 */

public class DateArrayHolder extends Holder<Date[]> {
  private static final long serialVersionUID = 1L;

  public DateArrayHolder() {
    super(Date[].class);
  }

  public DateArrayHolder(Date[] value) {
    super(Date[].class, value);
  }
}
