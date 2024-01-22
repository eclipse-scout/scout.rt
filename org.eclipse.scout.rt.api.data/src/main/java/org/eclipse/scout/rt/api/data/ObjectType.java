/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.dataobject.TypeName;

/**
 * Specifies the {@value ApiExposeHelper#OBJECT_TYPE_ATTRIBUTE_NAME} for a Scout element. It is used when creating the
 * corresponding element in the Scout TypeScript code on the browser. This allows to customize the class that will be
 * instantiated when creating the element in the Browser.
 * <p>
 * It has nothing to do with the {@link TypeName} annotation which must not control anything on the client and only
 * supports data objects.
 */
@Inherited
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface ObjectType {
  /**
   * @return The {@value ApiExposeHelper#OBJECT_TYPE_ATTRIBUTE_NAME} value.
   */
  String value();
}
