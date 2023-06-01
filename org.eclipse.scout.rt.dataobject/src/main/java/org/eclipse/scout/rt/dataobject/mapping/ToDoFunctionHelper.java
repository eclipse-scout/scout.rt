/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.mapping;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Objects;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

@ApplicationScoped
public class ToDoFunctionHelper {

  /**
   * Look for the first (see {@link BEANS#all}) {@link IToDoFunction} implementing the given functionClass that maps the
   * source into an object (i.e. does not return null).
   */
  public <S, T extends IDoEntity> T toDo(S source, Class<? extends IToDoFunction<S, ? extends T>> functionClass) {
    if (source == null) {
      return null;
    }
    assertNotNull(functionClass, "functionClass is required");
    return BEANS.all(functionClass).stream()
        .map(f -> f.apply(source))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new AssertionException("Source {} was not mapped.", source));
  }
}
