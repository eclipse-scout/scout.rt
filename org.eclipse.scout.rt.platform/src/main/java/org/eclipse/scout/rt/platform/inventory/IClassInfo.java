/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.InjectBean;

/**
 * Information about a {@link Class}. This information may be available, before the class is loaded.
 *
 * @since 5.1
 */
public interface IClassInfo {

  /** Declared public; may be accessed from outside its package. */
  int ACC_PUBLIC = Modifier.PUBLIC;
  /** Declared final; no subclasses allowed. */
  int ACC_FINAL = Modifier.FINAL;
  /** Is an interface, not a class. */
  int ACC_INTERFACE = Modifier.INTERFACE;
  /** Declared abstract; must not be instantiated. */
  int ACC_ABSTRACT = Modifier.ABSTRACT;
  /** Declared synthetic; not present in the source code. */
  int ACC_SYNTHETIC = 0x1000;
  /** Declared as an annotation type. */
  int ACC_ANNOTATION = 0x2000;
  /** Declared as an enum type. */
  int ACC_ENUM = 0x4000;

  /**
   * @return class name
   */
  String name();

  /**
   * @return class flags composed of {@link #ACC_PUBLIC},{@link #ACC_FINAL},{@link #ACC_INTERFACE},
   *         {@link #ACC_ABSTRACT},{@link #ACC_SYNTHETIC},{@link #ACC_ANNOTATION},{@link #ACC_ENUM}
   */
  int flags();

  /**
   * @return <code>true</code>, if the class has a constructor without any arguments.
   */
  boolean hasNoArgsConstructor();

  /**
   * @return <code>true</code>, if the class has a constructor with a {@link InjectBean} annotation.
   */
  boolean hasInjectableConstructor();

  /**
   * Returns <code>true</code> if the class represented by this class info is <b>directly</b> annotated with the given
   * annotation. Otherwise <code>false</code>.
   * </p>
   * <b>Note</b>: Other than {@link Class#isAnnotationPresent(Class)} this method does not respect the {@link Inherited}
   * annotation.
   */
  boolean hasAnnotation(Class<? extends Annotation> annotationType);

  /**
   * Loads the class, if necessary and returns it.
   *
   * @return loaded class
   */
  Class<?> resolveClass();

  /**
   * must be public, not abstract, not interface, not inner member type
   */
  boolean isInstanciable();

  /**
   * @return <code>true</code>, if declared public, <code>false</code> otherwise
   */
  boolean isPublic();

  /**
   * @return <code>true</code>, if declared final, <code>false</code> otherwise
   */
  boolean isFinal();

  /**
   * @return <code>true</code>, it is an interface (not a class), <code>false</code> otherwise
   */
  boolean isInterface();

  /**
   * @return <code>true</code>, it is an abstract, <code>false</code> otherwise
   */
  boolean isAbstract();

  /**
   * @return <code>true</code>, if declared synthetic; not present in the source code, <code>false</code> otherwise
   */
  boolean isSynthetic();

  /**
   * @return <code>true</code>, if declared as an annotation type, <code>false</code> otherwise
   */
  boolean isAnnotation();

  /**
   * @return <code>true</code>, if declared as an enum type, <code>false</code> otherwise
   */
  boolean isEnum();
}
