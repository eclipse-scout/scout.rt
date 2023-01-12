/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Marker interface for model elements which have the characteristics of an anchor to attach Forms, message boxes and
 * file choosers. Typical parents are {@link IDesktop}, {@link IOutline} or a {@link IForm}.
 *
 * @since 5.1
 */
public interface IDisplayParent {
}
