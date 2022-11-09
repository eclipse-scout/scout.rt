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
import {Comparator, Group, GroupCollapseStyle, ObjectOrChildModel, WidgetModel} from '../index';

export interface AccordionModel extends WidgetModel {
  /**
   * The comparator used to sort the groups. If no comparator is set, the groups will be displayed according to theirs insertion order.
   *
   * Default is false.
   */
  comparator?: Comparator<Group>;
  collapseStyle?: GroupCollapseStyle;
  /**
   * If true, only one group can be expanded at a time. If one group gets expanded, all other groups will be collapsed automatically.
   *
   * Default is true.
   */
  exclusiveExpand?: boolean;
  groups?: ObjectOrChildModel<Group>[];
  scrollable?: boolean;
}
