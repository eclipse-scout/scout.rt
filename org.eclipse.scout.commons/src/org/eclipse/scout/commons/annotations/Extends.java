/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.scout.commons.ClassIdentifier;

/**
 * Annotation to declare the owner of an {@link IExtension} or contribution.<br>
 * This annotation is used by the {@link IExtensionRegistry} service to automatically calculate the owner of
 * {@link IExtension}s or contributions.
 * Furthermore it is used by the Scout SDK to automatically create DTOs for contributions.
 *
 * @see IExtensionRegistry
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
