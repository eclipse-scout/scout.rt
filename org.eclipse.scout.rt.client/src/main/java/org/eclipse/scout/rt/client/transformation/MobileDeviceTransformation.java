/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.transformation;

/**
 * @since 3.9.0
 */
public enum MobileDeviceTransformation implements IDeviceTransformation {
  MAKE_DESKTOP_COMPACT,
  MOVE_FIELD_LABEL_TO_TOP,
  MOVE_FIELD_STATUS_TO_TOP,
  MAKE_FIELD_SCALEABLE,
  MAKE_MAINBOX_SCROLLABLE,
  MAKE_OUTLINE_ROOT_NODE_VISIBLE,
  REDUCE_GROUPBOX_COLUMNS_TO_ONE,
  HIDE_PLACEHOLDER_FIELD,
  HIDE_FIELD_STATUS,
  DISABLE_FORM_CANCEL_CONFIRMATION,
  USE_DIALOG_STYLE_FOR_VIEW,
  AUTO_CLOSE_SEARCH_FORM,
  MAXIMIZE_DIALOG,
  SET_SEQUENCEBOX_UI_HEIGHT
}
