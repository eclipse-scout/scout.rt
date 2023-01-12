/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectModel, Status, StatusOrModel, StatusSeverity, StatusSeverityNames} from '../index';

export interface StatusModel extends ObjectModel<Status> {
  message?: string;
  severity?: StatusSeverity | StatusSeverityNames;
  iconId?: string;
  code?: number;
  children?: StatusOrModel[];
  deletable?: boolean;
  uiState?: string;
}
