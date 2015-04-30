/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.inventory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Modifier;

/**
 * @since 5.1
 */
public interface IClassInfo {

  /** Declared public; may be accessed from outside its package. */
  public static final int ACC_PUBLIC = Modifier.PUBLIC;
  /** Declared final; no subclasses allowed. */
  public static final int ACC_FINAL = Modifier.FINAL;
  /** Is an interface, not a class. */
  public static final int ACC_INTERFACE = Modifier.INTERFACE;
  /** Declared abstract; must not be instantiated. */
  public static final int ACC_ABSTRACT = Modifier.ABSTRACT;
  /** Declared synthetic; not present in the source code. */
  public static final int ACC_SYNTHETIC = 0x1000;
  /** Declared as an annotation type. */
  public static final int ACC_ANNOTATION = 0x2000;
  /** Declared as an enum type. */
  public static final int ACC_ENUM = 0x4000;

  String name();

  int flags();

  boolean hasNoArgsConstructor();

  /**
   * Returns <code>true</code> if the class represented by this class info is <b>directly</b> annotated with the given
   * annotation. Otherwise <code>false</code>.
   * </p>
   * <b>Note</b>: Other than {@link Class#isAnnotationPresent(Class)} this method does not respect the {@link Inherited}
   * annotation.
   */
  boolean hasAnnotation(Class<? extends Annotation> annotationType);

  Class<?> resolveClass();

  /**
   * must be public, not abstract, not interface, not inner member type
   */
  boolean isInstanciable();

  boolean isPublic();

  boolean isFinal();

  boolean isInterface();

  boolean isAbstract();

  boolean isSynthetic();

  boolean isAnnotation();

  boolean isEnum();
}
