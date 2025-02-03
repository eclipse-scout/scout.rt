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

/**
 * @since 3.0
 */

public class LongHolder extends Holder<Long> {
  private static final long serialVersionUID = 1L;

  public LongHolder() {
    super(Long.class);
  }

  public LongHolder(Long value) {
    super(Long.class, value);
  }

  public LongHolder(Integer value) {
    super(Long.class, (value != null ? value.longValue() : null));
  }
}
