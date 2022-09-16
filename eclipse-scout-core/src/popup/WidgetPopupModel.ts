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
import {PopupModel} from '../index';
import {ResizableMode} from '../resizable/Resizable';

export default interface WidgetPopupModel extends PopupModel {
  /**
   * Default is false.
   */
  closable?: boolean;
  /**
   * Default is false.
   */
  movable?: boolean;
  /**
   * Default is false.
   */
  resizable?: boolean;
  /**
   * Default none.
   */
  resizeModes?: ResizableMode[];
}