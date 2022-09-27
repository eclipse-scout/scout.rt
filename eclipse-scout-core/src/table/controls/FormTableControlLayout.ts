/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, graphics, scrollbars, TabBox} from '../../index';
import $ from 'jquery';

export default class FormTableControlLayout extends AbstractLayout {

  constructor(control) {
    super();
    this.control = control;
  }

  layout($container) {
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
      if (tab && tab.scrollable && document.activeElement && tab.$body.has(document.activeElement)) {
        scrollbars.scrollTo(tab.$body, $(document.activeElement));
      }
    }
  }
}
