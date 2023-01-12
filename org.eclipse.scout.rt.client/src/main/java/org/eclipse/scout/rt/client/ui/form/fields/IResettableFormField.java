/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;

/**
 * only used in {@link AbstractForm#doReset} to include IComposerField which has been moved to separate module
 */
public interface IResettableFormField extends IFormField {

  /**
   * set field value to initValue and clear all error flags
   */
  void resetValue();
}
