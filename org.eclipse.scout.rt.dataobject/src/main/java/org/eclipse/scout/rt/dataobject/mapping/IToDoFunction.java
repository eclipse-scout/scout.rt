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

import java.util.function.Function;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * A function that creates a data object and applies values from an arbitrary source to it.<br>
 * This one way mapping mechanism might be useful when dealing with subclassed objects that need to be mapped to
 * subclassed data objects. If there are different {@link IToDoFunction}s for different types of sources
 * {@link ToDoFunctionHelper#toDo(Object, Class)} will find the first one that is able to handle a given source and
 * return its result.
 */
@ApplicationScoped
public interface IToDoFunction<SOURCE, TARGET extends IDoEntity> extends Function<SOURCE, TARGET> {
}
