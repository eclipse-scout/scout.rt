/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {InitModelOf} from '../index';

export interface AjaxErrorModel {
  jqXHR: JQuery.jqXHR;
  textStatus: JQuery.Ajax.ErrorTextStatus;
  errorThrown: string;
  requestOptions: JQuery.AjaxSettings;
}

export class AjaxError implements AjaxErrorModel {
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
