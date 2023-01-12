/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayParentModel, Form, ObjectOrChildModel, ObjectOrModel, Page, TreeModel} from '../../index';

export interface OutlineModel extends TreeModel, DisplayParentModel {
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
