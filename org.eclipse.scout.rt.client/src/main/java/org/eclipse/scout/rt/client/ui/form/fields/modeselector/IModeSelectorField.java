/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.modeselector;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;

public interface IModeSelectorField<T> extends IValueField<T> {

  String PROP_MODES = "modes";

  List<IMode<T>> getModes();

  IMode<T> getModeFor(T value);
}
