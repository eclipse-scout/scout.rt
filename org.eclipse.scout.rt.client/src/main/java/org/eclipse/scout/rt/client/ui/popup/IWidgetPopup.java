/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.client.ui.IWidget;

/**
 * Interface for a popup containing a widget.
 *
 * @since 9.0
 */
public interface IWidgetPopup<T extends IWidget> extends IPopup {

  String PROP_CONTENT = "content";
  String PROP_CLOSABLE = "closable";
  String PROP_MOVABLE = "movable";
  String PROP_RESIZABLE = "resizable";

  T getContent();

  void setClosable(boolean closable);

  boolean isClosable();

  void setMovable(boolean movable);

  boolean isMovable();

  void setResizable(boolean resizable);

  boolean isResizable();
}
