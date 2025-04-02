/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * A {@link ClassId} of a UI column.
 */
@IdTypeName("scout.ColumnClassId")
public final class ColumnClassId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private ColumnClassId(String id) {
    super(id);
  }

  public static ColumnClassId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new ColumnClassId(id);
  }

  /**
   * Applies the given function and converts the resulting value of type {@link String} to a {@link ColumnClassId}.
   */
  public static <E> Function<E, ColumnClassId> toId(Function<E, String> function) {
    return source -> ColumnClassId.of(function.apply(source));
  }

  /**
   * Converts the value of type {@link ColumnClassId} to a {@link String} and applies the given consumer.
   */
  public static <E> BiConsumer<E, ColumnClassId> toString(BiConsumer<E, String> consumer) {
    return (target, value) -> consumer.accept(target, IIds.toString(value));
  }
}
