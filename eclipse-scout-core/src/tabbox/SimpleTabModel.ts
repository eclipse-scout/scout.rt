/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {SimpleTabView, Status, WidgetModel} from '../index';

export interface SimpleTabModel<TView extends SimpleTabView = SimpleTabView> extends WidgetModel {
  view?: TView;
  title?: string;
  subTitle?: string;
  iconId?: string;
  closable?: boolean;
  saveNeeded?: boolean;
  saveNeededVisible?: boolean;
  status?: Status;
  selected?: boolean;
}
