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
import {Form, ObjectOrChildModel, ObjectOrModel, Page, TreeModel} from '../../index';

export interface OutlineModel extends TreeModel {
  nodes?: ObjectOrModel<Page>[];
  compact?: boolean;
  defaultDetailForm?: ObjectOrChildModel<Form>;
  embedDetailContent?: boolean;
  iconId?: string;
  title?: string;
  iconVisible?: boolean;
  navigateButtonsVisible?: boolean;
  outlineOverviewVisible?: boolean;
  titleVisible?: boolean;
  nodeMenuBarVisible?: boolean;
  detailMenuBarVisible?: boolean;
}
