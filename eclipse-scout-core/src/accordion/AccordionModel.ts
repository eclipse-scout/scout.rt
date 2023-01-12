/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Comparator, Group, GroupCollapseStyle, ObjectOrChildModel, WidgetModel} from '../index';

export interface AccordionModel extends WidgetModel {
  /**
   * The comparator used to sort the groups. If no comparator is set, the groups will be displayed according to theirs insertion order.
   *
   * Default is false.
   */
  comparator?: Comparator<Group>;
  /**
   * Specifies where the collapse handle should be displayed.
   *
   * Default is {@link GroupCollapseStyle.LEFT}
   */
  collapseStyle?: GroupCollapseStyle;
  /**
   * If true, only one group can be expanded at a time. If one group gets expanded, all other groups will be collapsed automatically.
   *
   * Default is true.
   */
  exclusiveExpand?: boolean;
  /**
   * The groups to be displayed inside the accordion.
   *
   * Default is [].
   */
  groups?: ObjectOrChildModel<Group>[];
  scrollable?: boolean;
}
