/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.messagebox;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;

/**
 * Filter to accept file choosers attached to a specific {@link IDisplayParent}.
 */
public class DisplayParentFileChooserFilter implements Predicate<IFileChooser> {

  private final IDisplayParent m_displayParent;

  public DisplayParentFileChooserFilter(final IDisplayParent displayParent) {
    m_displayParent = displayParent;
  }

  @Override
  public boolean test(final IFileChooser fileChooser) {
    return m_displayParent == fileChooser.getDisplayParent();
  }
}
