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
import {PageModel} from '../../../index';

export interface PageWithTableModel extends PageModel {
  /**
   * Configures whether a default child page should be created for each table row if no page is created. Default is false.
   */
  alwaysCreateChildPage?: boolean;
}
