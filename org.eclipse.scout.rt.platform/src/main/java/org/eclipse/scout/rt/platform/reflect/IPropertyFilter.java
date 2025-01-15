/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

@FunctionalInterface
public interface IPropertyFilter {

  /**
   * If the given descriptor should be analyzed.
   *
   * @param descriptor
   * @return if accepts the given descriptor
   * @see AbstractForm.updateForm(...)
   */
  boolean accept(FastPropertyDescriptor descriptor);
}
