/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
