/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;

/**
 * Annotation to declare the owner of an <code>org.eclipse.scout.rt.shared.extension.IExtensions&lt;OWNER&gt;</code> or
 * contribution.<br>
 * This annotation is used by the <code>org.eclipse.scout.rt.shared.extension.IExtensionRegistry</code> service to
 * automatically calculate the owner of extensions or contributions. Furthermore it is used by the Scout SDK to
 * automatically create DTOs for contributions.
 *
 * @since 4.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extends {

  /**
   * Describes the object that is extended. In other words: the owner of the extension or contribution.
   *
   * @return The owner class.
   */
  Class<?> value();

  /**
   * References {@link ClassIdentifier}
   *
   * @return
   */
  Class[] pathToContainer() default {};
}
