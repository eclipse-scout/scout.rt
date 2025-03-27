/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, Page} from '../index';

export interface BookmarkDoBuilderModel {
  desktop: Desktop;
  page?: Page;

  // builder options
  createOutline?: boolean;
  persistableRequired?: boolean;
  fallbackAllowed?: boolean;
  createTitle?: boolean;
  createDescription?: boolean;
  createTablePreferences?: boolean;
  createTableRowSelections?: boolean;
}
