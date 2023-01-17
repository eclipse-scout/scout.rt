/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * To be used with {@link IDoStructureMigrationTargetContextData} to specify for which raw ({@link #typeNames()}) or
 * typed ({@link #doEntityClasses()} data object such a context data should be created.
 * <p>
 * A data object is migrated before the corresponding context data is created, thus use new type name if a rename was
 * applied. A context data is created if the type name (for raw data objects) or data object class (for typed data
 * objects) matches the currently processed (and migrated) data object.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface DoStructureMigrationContextDataTarget {

  /**
   * @return Type names for which the annotated context data is created.
   */
  String[] typeNames() default {};

  /**
   * {@link IDoEntity} and {@link DoEntity} are not allowed values.
   *
   * @return Data object classes for which the annotated context data is created.
   */
  Class<? extends IDoEntity>[] doEntityClasses() default {};
}
