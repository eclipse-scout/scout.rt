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
import static org.eclipse.scout.rt.platform.util.TypeCastUtility.getGenericsParameterClass;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Base implementation for {@link IToDoFunction}. This toDoFunction accepts a source if it is an instance of
 * EXPLICIT_SOURCE and creates a target data object of the type EXPLICIT_TARGET. Subclasses can change this behaviour.
 */
public abstract class AbstractToDoFunction<EXPLICIT_SOURCE extends SOURCE, EXPLICIT_TARGET extends TARGET, SOURCE, TARGET extends IDoEntity> implements IToDoFunction<SOURCE, TARGET> {

  private final Class<EXPLICIT_SOURCE> m_explicitSourceClass;
  private final Class<EXPLICIT_TARGET> m_explicitTargetClass;

  protected AbstractToDoFunction() {
    //noinspection unchecked
    m_explicitSourceClass = assertNotNull(getGenericsParameterClass(getClass(), AbstractToDoFunction.class, 0));
    //noinspection unchecked
    m_explicitTargetClass = assertNotNull(getGenericsParameterClass(getClass(), AbstractToDoFunction.class, 1));
  }

  protected Class<EXPLICIT_SOURCE> getExplicitSourceClass() {
    return m_explicitSourceClass;
  }

  protected Class<EXPLICIT_TARGET> getExplicitTargetClass() {
    return m_explicitTargetClass;
  }

  protected boolean accept(SOURCE source) {
    return getExplicitSourceClass().isInstance(source);
  }

  protected EXPLICIT_TARGET createDo() {
    return BEANS.get(getExplicitTargetClass());
  }

  @Override
  public TARGET apply(SOURCE source) {
    if (!accept(source)) {
      return null;
    }
    EXPLICIT_TARGET doEntity = createDo();
    if (getExplicitSourceClass().isInstance(source)) {
      apply(getExplicitSourceClass().cast(source), doEntity);
    }
    return doEntity;
  }

  public abstract void apply(EXPLICIT_SOURCE source, EXPLICIT_TARGET target);
}
