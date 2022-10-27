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
import {Status, WidgetModel} from '../index';
import {SimpleTabView} from './SimpleTab';

export default interface SimpleTabModel<TView extends SimpleTabView = SimpleTabView> extends WidgetModel {
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
