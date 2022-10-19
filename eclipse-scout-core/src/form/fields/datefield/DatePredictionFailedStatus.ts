/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Status} from '../../../index';

/**
 * This status is used as a marker class to distinct between regular errors and errors thrown by the predict* functions of the DateField.
 */
export default class DatePredictionFailedStatus extends Status {

  constructor() {
    super();
  }
}
