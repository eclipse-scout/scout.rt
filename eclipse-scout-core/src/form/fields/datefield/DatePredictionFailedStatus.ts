/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Status} from '../../../index';

/**
 * This status is used as a marker class to distinct between regular errors and errors thrown by the predict* functions of the DateField.
 */
export class DatePredictionFailedStatus extends Status {
}
