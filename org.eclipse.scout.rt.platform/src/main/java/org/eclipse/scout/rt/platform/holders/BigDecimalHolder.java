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

import java.math.BigDecimal;

/**
 * @since 3.0
 */

public class BigDecimalHolder extends Holder<BigDecimal> {
  private static final long serialVersionUID = 1L;

  public BigDecimalHolder() {
    super(BigDecimal.class);
  }

  public BigDecimalHolder(BigDecimal value) {
    super(BigDecimal.class, value);
  }
}
