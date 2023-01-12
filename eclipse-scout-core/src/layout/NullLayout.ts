/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, HtmlComponent} from '../index';
import $ from 'jquery';

export class NullLayout extends AbstractLayout {

  override layout($container: JQuery) {
    $container.children().each(function() {
      let htmlComp = HtmlComponent.optGet($(this));
      if (htmlComp) {
        htmlComp.revalidateLayout();
      }
    });
  }
}
