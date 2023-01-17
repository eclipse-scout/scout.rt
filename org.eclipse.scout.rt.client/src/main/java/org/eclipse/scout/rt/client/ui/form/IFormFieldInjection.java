/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * Instances of this interface are used with FormFieldInjectionThreadLocal to define a context within form fields are
 * created. {@link AbstractForm} and {@link AbstractCompositeField} use this mechanism to contribute fields using the
 * {@link InjectFieldTo} annotation.
 *
 * @since 3.8.1
 */
public interface IFormFieldInjection {
  /**
   * @param container
   *          is the container field that is being added potential injected fields
   * @param fields
   *          live and mutable collection of currently (configured) fields, not yet initialized or added to the
   *          container field
   */
  void injectFields(IFormField container, OrderedCollection<IFormField> fields);

  /**
   * @param container
   *          is the container field the given field classes are created for
   * @param fieldList
   *          live and mutable list of configured field classes (i.e. yet not instantiated)
   * @since 3.8.2 (moved to IFormFieldInjection in 3.10.0-M3)
   */
  void filterFields(IFormField container, List<Class<? extends IFormField>> fieldList);

}
