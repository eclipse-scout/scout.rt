/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * All possible menu types of a value field menu. These menu types are used by
 * {@link AbstractMenu#getConfiguredMenuTypes()} on any {@link IValueField}.
 */
public enum ValueFieldMenuType implements IMenuType {
  Null,
  NotNull
}
