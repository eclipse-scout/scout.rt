/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BasicField, FormField, graphics, HtmlComponent, HtmlEnvironment} from '../../../src/index';
import {FormSpecHelper} from '@eclipse-scout/testing';


describe('FormFieldLayout', function() {
  var session;
  var helper;
  // Sizes are with margin, static widths and heights are without margin
  var formField, rowHeight, mandatoryWidth, statusWidth, labelWidth, labelHeight;
  var mandatorySize, mandatoryMargins, labelSize, labelMargins, fieldSize, fieldMargins, statusSize, statusMargins;
  var origPrefSize;

  class CustomFormField extends BasicField {
    _render() {
      this.addContainer(this.$parent, 'form-field');
      this.addLabel();
      this.$label.css({
        display: 'inline-block',
        position: 'absolute',
        minHeight: 30,
        minWidth: 80,
        margin: 5
      });

      this.addMandatoryIndicator();
      this.$mandatory.css({
        display: 'inline-block',
        position: 'absolute',
        minHeight: 25,
        minWidth: 5,
        margin: 2
      });

      this.addField(this.$parent.makeDiv());
      this.$fieldContainer.css({
        display: 'inline-block',
        position: 'absolute',
        minHeight: 40,
        minWidth: 100,
        margin: 5
      });

      this.addStatus();
      this.$status.css({
        display: 'inline-block',
        position: 'absolute',
        minHeight: 30,
        minWidth: 40,
        margin: 5
      });
    }
  }


  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    rowHeight = HtmlEnvironment.get().formRowHeight;
    mandatoryWidth = HtmlEnvironment.get().fieldMandatoryIndicatorWidth;
    statusWidth = HtmlEnvironment.get().fieldStatusWidth;
    labelWidth = HtmlEnvironment.get().fieldLabelWidth;
    // Label is always as height as a row if label position is set to LEFT
    labelHeight = rowHeight;

    formField = new CustomFormField();
    formField.init({
      parent: session.desktop,
      label: 'abc'
    });
    formField.render();
  });

  function readSizes() {
    mandatorySize = graphics.prefSize(formField.$mandatory, true);
    mandatoryMargins = graphics.margins(formField.$mandatory);
    labelSize = graphics.prefSize(formField.$label, true);
    labelMargins = graphics.margins(formField.$label);
    fieldSize = graphics.prefSize(formField.$fieldContainer, true);
    fieldMargins = graphics.margins(formField.$fieldContainer);
    statusSize = graphics.prefSize(formField.$status, true);
    statusMargins = graphics.margins(formField.$status);
  }

  /**
   * Calls the given function when graphics.prefSize is called for the $fieldContainer
   */
  function spyForWidthHint(func) {
    if (origPrefSize) {
      unspyForWidthHint();
    }
    origPrefSize = graphics.prefSize;
    graphics.prefSize = function($elem, options) {
      if ($elem[0] === formField.$fieldContainer[0]) {
        func(options.widthHint, options.heightHint);
      }
      return origPrefSize($elem, options);
    };
  }

  function unspyForWidthHint() {
    if (!origPrefSize) {
      return;
    }
    graphics.prefSize = origPrefSize;
    origPrefSize = null;
  }

  describe('prefSize', function() {

    describe('labelPosition', function() {

      describe('default', function() {
        it('returns the max height of the parts', function() {
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widhts of the parts', function() {
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('adjusts widthHint', function() {
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          var widthHint;
          spyForWidthHint(function(spiedWidthHint) {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });

        it('does not adjust widthHint or heightHint if it is not set', function() {
          readSizes();
          var widthHint;
          var heightHint;
          spyForWidthHint(function(spiedWidthHint, spiedHeightHint) {
            widthHint = spiedWidthHint;
            heightHint = spiedHeightHint;
          });
          formField.htmlComp.prefSize();
          expect(widthHint).toBe(undefined);
          expect(heightHint).toBe(undefined);
          unspyForWidthHint();
        });

        it('does not adjust widthHint or heightHint even if margins are negative', function() {
          formField.$fieldContainer.css({
            margin: -5
          });
          readSizes();
          var widthHint;
          var heightHint;
          spyForWidthHint(function(spiedWidthHint, spiedHeightHint) {
            widthHint = spiedWidthHint;
            heightHint = spiedHeightHint;
          });
          formField.htmlComp.prefSize();
          expect(widthHint).toBe(undefined);
          expect(heightHint).toBe(undefined);
          unspyForWidthHint();
        });

        it('ignores label if label is invisible', function() {
          formField.setLabelVisible(false);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width;
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('does not adjust widthHint or heightHint if container contains an html component', function() {
          HtmlComponent.install(formField.$fieldContainer, session);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          var widthHint;
          spyForWidthHint(function(spiedWidthHint) {
            widthHint = spiedWidthHint;
          });
          var prefSize = formField.htmlComp.prefSize({widthHint: 400});
          expect(prefSize.width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });
      });

      describe('top', function() {
        it('adds label and field height if label is on top', function() {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          var expectedHeight = labelSize.height + fieldSize.height;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', function() {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', function() {
          formField.setLabelVisible(false);
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          var expectedHeight = fieldSize.height;
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          var expectedHeight = labelSize.height + fieldSize.height;
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });

      describe('on_field', function() {
        it('returns the max height of the parts', function() {
          formField.setLabelPosition(FormField.LabelPosition.ON_FIELD);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', function() {
          formField.setLabelPosition(FormField.LabelPosition.ON_FIELD);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelPosition(FormField.LabelPosition.ON_FIELD);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, fieldSize.height);
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });
    });

    describe('labelWidthInPixel', function() {

      describe('UI', function() {
        it('returns the max height of the parts', function() {
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widhts of the parts with label\'s preferred width', function() {
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', function() {
          formField.setLabelVisible(false);
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width;
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });
      });

      describe('custom', function() {
        it('returns the max height of the parts', function() {
          formField.setLabelWidthInPixel(123);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', function() {
          formField.setLabelWidthInPixel(123);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + 123 + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', function() {
          formField.setLabelVisible(false);
          formField.setLabelWidthInPixel(123);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelWidthInPixel(123);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height);
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + 123 + labelMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });
    });

    describe('statusPosition', function() {

      describe('top without label position top', function() {
        it('sums up the widths', function() {
          formField.setLabelPosition(FormField.LabelPosition.LEFT);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          // Status is only pulled up if label is on top as well
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('does not adjust widthHint', function() {
          formField.setLabelPosition(FormField.LabelPosition.LEFT);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          var widthHint;
          spyForWidthHint(function(spiedWidthHint) {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });
      });

      describe('top with label position top', function() {
        it('sums up the widths without label and status', function() {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('adjusts widthHint', function() {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          var expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          var widthHint;
          spyForWidthHint(function(spiedWidthHint) {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });
      });
    });
  });

  describe('layout', function() {

    describe('labelPosition', function() {

      describe('top', function() {

        it('positions the label on top of the field', function() {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          formField.htmlComp.validateLayout();
          expect(formField.$fieldContainer.cssTop()).toBe(labelSize.height);
        });
      });
    });

    describe('labelWidthInPixel', function() {

      describe('UI', function() {

        it('makes the label as width as its content', function() {
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          formField.htmlComp.validateLayout();
          expect(formField.$label.outerWidth(true)).toBe(labelSize.width);
          expect(formField.$fieldContainer.cssLeft()).toBe(labelSize.width + mandatoryWidth + mandatoryMargins.horizontal());
        });
      });
    });
  });
});
