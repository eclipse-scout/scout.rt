/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.dataobject.id.IdCodec.IIdCodecFlag;

/**
 * Annotation used to define the unique type name for an {@link IId} class, used when serializing or deserializing
 * instances.
 *
 * @see IdCodec#toQualified(IId, IIdCodecFlag...)
 * @see IdCodec#fromQualified(String, IIdCodecFlag...)
 * @see TypedId
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface IdTypeName {

  /**
   * Unique type name used when serializing an instance of the annotated {@link IId} class. Must not be {@code null} or
   * empty.
   */
  String value();
}
