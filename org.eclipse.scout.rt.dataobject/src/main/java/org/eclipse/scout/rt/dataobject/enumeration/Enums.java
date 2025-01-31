/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.enumeration;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Various helper methods dealing with {@link IEnum}.
 */
public final class Enums {

  private Enums() {
  }

  /**
   * Null-safe version of {@link IEnum#stringValue()}.
   *
   * @return {@link IEnum#stringValue()} if given {@code enumInstnce} is not {@code null}, returns {@code null}
   * otherwise.
   */
  public static String toStringValue(IEnum enumInstance) {
    if (enumInstance == null) {
      return null;
    }
    return enumInstance.stringValue();
  }

  /**
   * @return {@link IEnum} instance for given {@code enumClass} and {@code stringValue}
   */
  public static <ENUM extends IEnum> ENUM resolve(Class<ENUM> enumClass, String stringValue) {
    return BEANS.get(EnumResolver.class).resolve(enumClass, stringValue);
  }
}
