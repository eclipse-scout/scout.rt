/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {Form, graphics, ObjectFactory, scout, Status, Tooltip} from '../../src/index';

describe('WidgetPopup', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function createPopupWithFormAnd2Fields(initialFocus) {
    return scout.create('WidgetPopup', {
      parent: session.desktop,
      widget: {
        objectType: 'Form',
        displayHint: Form.DisplayHint.VIEW,
        modal: false,
        initialFocus: initialFocus,
        rootGroupBox: {
          objectType: 'GroupBox',
          fields: [{
            id: 'First Field',
            objectType: 'StringField'
          }, {
            id: 'Second Field',
            objectType: 'StringField'
          }]
        }
      }
    });
  }

  describe('withFocusContext', () => {
    it('focuses the first focusable element when opened', () => {
      let popup = createPopupWithFormAnd2Fields();
      popup.animateOpening = false;
      popup.open();
      expect(popup.widget.widget('First Field').isFocused()).toBe(true);
    });

    it('focuses the element specified by an inner element', () => {
      let popup = createPopupWithFormAnd2Fields('Second Field');
      popup.animateOpening = false;
      popup.open();
      expect(popup.widget.widget('Second Field').isFocused()).toBe(true);
    });

    it('reverts focus correctly when popup is closed', () => {
      let field1 = scout.create('StringField', {
        parent: session.desktop
      });
      field1.render();
      let field2 = scout.create('StringField', {
        parent: session.desktop
      });
      field2.render();
      field2.focus();

      let popup = createPopupWithFormAnd2Fields('Second Field');
      popup.animateRemoval = false;
      popup.animateOpening = false;
      popup.open();
      expect(popup.widget.widget('Second Field').isFocused()).toBe(true);

      popup.close();
      expect(field2.isFocused()).toBe(true);
    });
  });

  describe('open', () => {

    beforeEach(() => {
      $('<style>' +
        '.popup { position: absolute; }' +
        '.tooltip { position: absolute; }' +
        '</style>').appendTo($('#sandbox'));
      ObjectFactory.get().register('Tooltip', () => new SpecTooltip());
    });

    afterEach(() => {
      ObjectFactory.get().register('Tooltip', () => new Tooltip());
    });

    it('positions tooltip correctly', () => {
      const popup = createPopupWithFormAnd2Fields();
      const field = popup.widget.rootGroupBox.fields[0];

      field.addErrorStatus(Status.error('I am an error!!!'));
      popup.open();
      jasmine.clock().tick(1000);
      popup.validateLayoutTree();

      const anchorPoint = graphics.offsetBounds(field.fieldStatus.tooltip.$anchor).point();
      const tooltipPoint = graphics.offsetBounds(field.fieldStatus.tooltip.$container).point();

      expect(tooltipPoint).toEqual(anchorPoint);
    });
  });

  class SpecTooltip extends Tooltip {
    position() {
      const {x, y} = this._getOrigin();
      this.$container
        .cssLeft(x)
        .cssTop(y);
    }
  }
});
