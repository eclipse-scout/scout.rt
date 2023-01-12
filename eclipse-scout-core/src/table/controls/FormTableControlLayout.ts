/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, FormTableControl, graphics, scrollbars, TabBox} from '../../index';
import $ from 'jquery';

export class FormTableControlLayout extends AbstractLayout {
  control: FormTableControl;

  constructor(control: FormTableControl) {
    super();
    this.control = control;
  }

  override layout($container: JQuery) {
    if (!this.control.contentRendered || !this.control.form) {
      return;
    }

    let form = this.control.form,
      htmlForm = form.htmlComp,
      controlContentSize = graphics.size(this.control.tableFooter.$controlContent),
      formSize = controlContentSize.subtract(htmlForm.margins());

    htmlForm.setSize(formSize);

    // special case: when the control is opened/resized and there is not enough space, ensure that the active element is
    // visible by scrolling to it
    if (form.rootGroupBox.controls[0] instanceof TabBox) {
      let tabBox = form.rootGroupBox.controls[0];
      let tab = tabBox.selectedTab;
      let activeElement = document.activeElement as HTMLElement;
      if (tab && tab.scrollable && activeElement && tab.$body.has(activeElement)) {
        scrollbars.scrollTo(tab.$body, $(activeElement));
      }
    }
  }
}
