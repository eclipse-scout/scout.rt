/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LayoutData, ObjectOrChildModel, SimpleTabArea, SimpleTabBoxController, SimpleTabView, WidgetModel} from '../index';

export interface SimpleTabBoxModel<TView extends SimpleTabView = SimpleTabView> extends WidgetModel {
  tabArea?: ObjectOrChildModel<SimpleTabArea<TView>>;
  viewStack?: TView[];
  currentView?: TView;
  controller?: SimpleTabBoxController<TView>;
  layoutData?: LayoutData;
}
