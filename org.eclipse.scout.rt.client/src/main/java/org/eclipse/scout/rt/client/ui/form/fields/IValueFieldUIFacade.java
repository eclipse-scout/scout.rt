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

import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * Common UI facade for value fields with value, displayText and error-status.
 *
 * @since 7.0
 */
public interface IValueFieldUIFacade<VALUE> {

  void setValueFromUI(VALUE value);

  void setDisplayTextFromUI(String text);

  void setErrorStatusFromUI(IStatus errorStatus);
}
