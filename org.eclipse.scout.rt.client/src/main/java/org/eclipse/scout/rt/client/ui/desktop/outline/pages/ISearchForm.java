/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Marker interface for a form used as search form.
 * <p>
 * A search form is started by {@link IForm#start()}. Thereto, the search handler must be installed during
 * initialization, or by overriding {@link #start()}.
 */
public interface ISearchForm extends IForm {
}
