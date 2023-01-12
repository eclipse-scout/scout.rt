/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action.keystroke;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;

public abstract class AbstractKeyStrokeExtension<OWNER extends AbstractKeyStroke> extends AbstractActionExtension<OWNER> implements IKeyStrokeExtension<OWNER> {

  public AbstractKeyStrokeExtension(OWNER owner) {
    super(owner);
  }
}
