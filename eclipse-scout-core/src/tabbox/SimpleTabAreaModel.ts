/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {SimpleTab, SimpleTabAreaDisplayStyle, SimpleTabView, WidgetModel} from '../index';

export interface SimpleTabAreaModel<TView extends SimpleTabView = SimpleTabView> extends WidgetModel {
  displayStyle?: SimpleTabAreaDisplayStyle;
  tabs?: SimpleTab<TView>[];
}
