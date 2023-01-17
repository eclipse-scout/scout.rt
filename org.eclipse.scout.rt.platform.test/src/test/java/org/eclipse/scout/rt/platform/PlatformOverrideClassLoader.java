/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

/**
 * This {@link ClassLoader} is used to mock a {@link IPlatform} that is used in {@link Platform#get()}.
 * <p>
 * Therefore it emulates a <code>META-INF/services/org.eclipse.scout.rt.platform.IPlatform</code> resource file with the
 * desired class name as content.
 * <p>
 * In order to use this {@link ClassLoader} it must be set into context, for example using
 * <code>Thread.currentThread().setContextClassLoader()</code>
 *
 * @author imo
 */
public class PlatformOverrideClassLoader extends ServiceLoaderClassLoaderMock {

  /**
   * @param parent
   *          class loader
   * @param platformOverrideClass
   *          the {@link IPlatform} class to be used in {@link Platform#get()}
   */
  public PlatformOverrideClassLoader(ClassLoader parent, Class<? extends IPlatform> platformOverrideClass) {
    super(parent, IPlatform.class, platformOverrideClass);
  }
}
