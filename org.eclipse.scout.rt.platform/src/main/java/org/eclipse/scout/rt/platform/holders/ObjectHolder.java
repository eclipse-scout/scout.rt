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

public class ObjectHolder extends Holder<Object> {
  private static final long serialVersionUID = 1L;

  public ObjectHolder() {
    super(Object.class);
  }

  public ObjectHolder(Object value) {
    super(Object.class, value);
  }

}
