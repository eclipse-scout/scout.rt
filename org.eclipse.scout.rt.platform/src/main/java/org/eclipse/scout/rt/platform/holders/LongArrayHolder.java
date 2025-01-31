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

import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * @since 3.0
 */

public class LongArrayHolder extends Holder<Long[]> {
  private static final long serialVersionUID = 1L;

  public LongArrayHolder() {
    super(Long[].class);
  }

  public LongArrayHolder(Long[] value) {
    super(Long[].class, value);
  }

  public LongArrayHolder(Integer[] value) {
    super(Long[].class, (value != null ? TypeCastUtility.castValue(value, Long[].class) : null));
  }
}
