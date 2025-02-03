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

public class StringArrayHolder extends Holder<String[]> {
  private static final long serialVersionUID = 1L;

  public StringArrayHolder() {
    super(String[].class);
  }

  public StringArrayHolder(String[] value) {
    super(String[].class, value);
  }
}
