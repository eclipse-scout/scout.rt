/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('FormFieldLayout', function() {
  var session;
  var helper;

  var CustomFormField = function() {
    CustomFormField.parent.call(this);
  };
  scout.inherits(CustomFormField, scout.BasicField);

  CustomFormField.prototype._render = function() {
    this.addContainer(this.$parent, 'form-field');
    this.addLabel();
    this.$label.css({
      display: 'inline-block',
      minHeight: 30,
      minWidth: 80,
      margin: 5
    });

    this.addMandatoryIndicator();
    this.$mandatory.css({
      display: 'inline-block',
      minHeight: 25,
      minWidth: 10,
      margin: 2
    });

    this.addField(this.$parent.makeDiv());
    this.$fieldContainer.css({
      display: 'inline-block',
      minHeight: 40,
      minWidth: 100,
      margin: 5
    });

    this.addStatus();
    this.$status.css({
      display: 'inline-block',
      minHeight: 30,
      minWidth: 40,
      margin: 5
    });
  };

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  describe('prefSize', function() {
    var formField, rowHeight, mandatoryWidth, statusWidth, labelWidth,labelHeight;
    var mandatorySize, mandatoryMargins, labelSize, labelMargins, fieldSize, fieldMargins, statusSize, statusMargins;

    beforeEach(function() {
      rowHeight = scout.HtmlEnvironment.formRowHeight;
      mandatoryWidth = scout.HtmlEnvironment.fieldMandatoryIndicatorWidth;
      statusWidth = scout.HtmlEnvironment.fieldStatusWidth;
      labelWidth = scout.HtmlEnvironment.fieldLabelWidth;
      // Label is currently always as height as a row, which is expected by LogicalGridData.logicalRowHeightAddition
      // This may be changed in the future if multiline labels or smaller labels are demanded
      labelHeight = rowHeight;

      formField = new CustomFormField();
      formField.init({
        parent: session.desktop,
        label: 'abc',
        gridDataHints: {
          useUiHeight: true
        }
      });
      formField.render();
    });

    function readSizes() {
      mandatorySize = scout.graphics.prefSize(formField.$mandatory, true);
      mandatoryMargins = scout.graphics.margins(formField.$mandatory);
      labelSize = scout.graphics.prefSize(formField.$label, true);
      labelMargins = scout.graphics.margins(formField.$label);
      fieldSize = scout.graphics.prefSize(formField.$fieldContainer, true);
      fieldMargins = scout.graphics.margins(formField.$fieldContainer);
      statusSize = scout.graphics.prefSize(formField.$status, true);
      statusMargins = scout.graphics.margins(formField.$status);
    }

    function spyForWidthHint(func) {
      var spy = spyOn(scout.graphics, 'prefSize');
      spy.and.callFake(function($elem, options) {
        var widthHint;
        if ($elem[0] === formField.$fieldContainer[0]) {
          widthHint = options.widthHint;
        }
        spy.and.callThrough(); // Replace with original function again
        func(widthHint);
        return scout.graphics.prefSize($elem, options);
      });
    }

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
      });

      describe('top', function() {
        it('adds label and field height if label is on top', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.TOP);
          readSizes();
          var expectedHeight = labelHeight + fieldSize.height;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', function() {
          formField.setLabelVisible(false);
          formField.setLabelPosition(scout.FormField.LabelPosition.TOP);
          readSizes();
          var expectedHeight = fieldSize.height;
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelPosition(scout.FormField.LabelPosition.TOP);
          readSizes();
          var expectedHeight = labelHeight + fieldSize.height;
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });

      describe('on_field', function() {
        it('returns the max height of the parts', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.ON_FIELD);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.ON_FIELD);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelPosition(scout.FormField.LabelPosition.ON_FIELD);
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
          formField.setLabelWidthInPixel(scout.FormField.LabelWidth.UI);
          readSizes();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widhts of the parts with label\'s preferred width', function() {
          formField.setLabelWidthInPixel(scout.FormField.LabelWidth.UI);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', function() {
          formField.setLabelVisible(false);
          formField.setLabelWidthInPixel(scout.FormField.LabelWidth.UI);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('ignores status if status is invisible', function() {
          formField.setStatusVisible(false);
          formField.setLabelWidthInPixel(scout.FormField.LabelWidth.UI);
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
          formField.setLabelPosition(scout.FormField.LabelPosition.LEFT);
          formField.setStatusPosition(scout.FormField.StatusPosition.TOP);
          readSizes();
          // Status is only pulled up if label is on top as well
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('does not adjust widthHint', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.LEFT);
          formField.setStatusPosition(scout.FormField.StatusPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          var expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          var widthHint;
          spyForWidthHint(function(spiedWidthHint) {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
        });
      });

      describe('top with label position top', function() {
        it('sums up the widths without label and status', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.TOP);
          formField.setStatusPosition(scout.FormField.StatusPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('adjusts widthHint', function() {
          formField.setLabelPosition(scout.FormField.LabelPosition.TOP);
          formField.setStatusPosition(scout.FormField.StatusPosition.TOP);
          readSizes();
          var expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          var expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          var widthHint;
          spyForWidthHint(function(spiedWidthHint) {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
        });
      });
    });
  });
});
