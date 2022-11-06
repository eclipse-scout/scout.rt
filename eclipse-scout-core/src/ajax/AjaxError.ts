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
import $ from 'jquery';
import {InitModelOf} from '../scout';

export interface AjaxErrorModel {
  jqXHR: JQuery.jqXHR;
  textStatus: JQuery.Ajax.ErrorTextStatus;
  errorThrown: string;
  requestOptions: JQuery.AjaxSettings;
}

export default class AjaxError implements AjaxErrorModel {
  declare model: AjaxErrorModel;

  jqXHR: JQuery.jqXHR;
  textStatus: JQuery.Ajax.ErrorTextStatus;
  errorThrown: string;
  requestOptions: JQuery.AjaxSettings;

  constructor(model: InitModelOf<AjaxError>) {
    this.jqXHR = null;
    this.textStatus = null;
    this.errorThrown = null;
    this.requestOptions = null;
    $.extend(this, model);
  }
}
