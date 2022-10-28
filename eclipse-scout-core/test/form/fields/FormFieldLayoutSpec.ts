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
import {BasicField, Dimension, FormField, graphics, HtmlComponent, HtmlEnvironment, Insets} from '../../../src/index';
import {FormSpecHelper} from '../../../src/testing/index';
import {PrefSizeOptions} from '../../../src/layout/graphics';

describe('FormFieldLayout', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  // Sizes are with margin, static widths and heights are without margin
  let formField: CustomFormField, rowHeight: number, mandatoryWidth: number, statusWidth: number, labelWidth: number, labelHeight: number;
  let mandatorySize: Dimension, mandatoryMargins: Insets, labelSize: Dimension, labelMargins: Insets, fieldSize: Dimension, fieldMargins: Insets, statusSize: Dimension, statusMargins: Insets;
  let origPrefSize;

  class CustomFormField extends BasicField<any> {
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

  beforeEach(() => {
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
    graphics.prefSize = ($elem: JQuery, options: PrefSizeOptions) => {
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

  describe('prefSize', () => {

    describe('labelPosition', () => {

      describe('default', () => {
        it('returns the max height of the parts', () => {
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widhts of the parts', () => {
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('adjusts widthHint', () => {
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          let expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          let widthHint;
          spyForWidthHint(spiedWidthHint => {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });

        it('does not adjust widthHint or heightHint if it is not set', () => {
          readSizes();
          let widthHint;
          let heightHint;
          spyForWidthHint((spiedWidthHint, spiedHeightHint) => {
            widthHint = spiedWidthHint;
            heightHint = spiedHeightHint;
          });
          formField.htmlComp.prefSize();
          expect(widthHint).toBe(undefined);
          expect(heightHint).toBe(undefined);
          unspyForWidthHint();
        });

        it('does not adjust widthHint or heightHint even if margins are negative', () => {
          formField.$fieldContainer.css({
            margin: -5
          });
          readSizes();
          let widthHint;
          let heightHint;
          spyForWidthHint((spiedWidthHint, spiedHeightHint) => {
            widthHint = spiedWidthHint;
            heightHint = spiedHeightHint;
          });
          formField.htmlComp.prefSize();
          expect(widthHint).toBe(undefined);
          expect(heightHint).toBe(undefined);
          unspyForWidthHint();
        });

        it('ignores label if label is invisible', () => {
          formField.setLabelVisible(false);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          let expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('ignores status if status is invisible', () => {
          formField.setStatusVisible(false);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width;
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('does not adjust widthHint or heightHint if container contains an html component', () => {
          HtmlComponent.install(formField.$fieldContainer, session);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          let expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          let widthHint;
          spyForWidthHint(spiedWidthHint => {
            widthHint = spiedWidthHint;
          });
          let prefSize = formField.htmlComp.prefSize({widthHint: 400});
          expect(prefSize.width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });
      });

      describe('top', () => {
        it('adds label and field height if label is on top', () => {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          let expectedHeight = labelSize.height + fieldSize.height;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', () => {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', () => {
          formField.setLabelVisible(false);
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          let expectedHeight = fieldSize.height;
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', () => {
          formField.setStatusVisible(false);
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          let expectedHeight = labelSize.height + fieldSize.height;
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });

      describe('on_field', () => {
        it('returns the max height of the parts', () => {
          formField.setLabelPosition(FormField.LabelPosition.ON_FIELD);
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', () => {
          formField.setLabelPosition(FormField.LabelPosition.ON_FIELD);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', () => {
          formField.setStatusVisible(false);
          formField.setLabelPosition(FormField.LabelPosition.ON_FIELD);
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, fieldSize.height);
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });
    });

    describe('labelWidthInPixel', () => {

      describe('UI', () => {
        it('returns the max height of the parts', () => {
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widhts of the parts with label\'s preferred width', () => {
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', () => {
          formField.setLabelVisible(false);
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width + statusWidth + statusMargins.horizontal();
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('ignores status if status is invisible', () => {
          formField.setStatusVisible(false);
          formField.setLabelWidthInPixel(FormField.LabelWidth.UI);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelSize.width + fieldSize.width;
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });
      });

      describe('custom', () => {
        it('returns the max height of the parts', () => {
          formField.setLabelWidthInPixel(123);
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height, statusSize.height);
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
        });

        it('sums up the widths without label', () => {
          formField.setLabelWidthInPixel(123);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + 123 + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores label if label is invisible', () => {
          formField.setLabelVisible(false);
          formField.setLabelWidthInPixel(123);
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, fieldSize.height, statusSize.height);
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('ignores status if status is invisible', () => {
          formField.setStatusVisible(false);
          formField.setLabelWidthInPixel(123);
          readSizes();
          let expectedHeight = Math.max(mandatorySize.height, labelHeight, fieldSize.height);
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + 123 + labelMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().height).toBe(expectedHeight);
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });
    });

    describe('statusPosition', () => {

      describe('top without label position top', () => {
        it('sums up the widths', () => {
          formField.setLabelPosition(FormField.LabelPosition.LEFT);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          // Status is only pulled up if label is on top as well
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('does not adjust widthHint', () => {
          formField.setLabelPosition(FormField.LabelPosition.LEFT);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + labelWidth + labelMargins.horizontal() + fieldSize.width + statusWidth + statusMargins.horizontal();
          let expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          let widthHint;
          spyForWidthHint(spiedWidthHint => {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });
      });

      describe('top with label position top', () => {
        it('sums up the widths without label and status', () => {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          expect(formField.htmlComp.prefSize().width).toBe(expectedWidth);
        });

        it('adjusts widthHint', () => {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          formField.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();
          let expectedWidth = mandatoryWidth + mandatoryMargins.horizontal() + fieldSize.width;
          let expectedWidthHint = 400 - (expectedWidth - fieldSize.width + fieldMargins.horizontal());
          let widthHint;
          spyForWidthHint(spiedWidthHint => {
            widthHint = spiedWidthHint;
          });
          expect(formField.htmlComp.prefSize({widthHint: 400}).width).toBe(400);
          expect(widthHint).toBe(expectedWidthHint);
          unspyForWidthHint();
        });
      });
    });
  });

  describe('layout', () => {

    describe('labelPosition', () => {

      describe('top', () => {

        it('positions the label on top of the field', () => {
          formField.setLabelPosition(FormField.LabelPosition.TOP);
          readSizes();
          formField.htmlComp.validateLayout();
          expect(formField.$fieldContainer.cssTop()).toBe(labelSize.height);
        });
      });
    });

    describe('labelWidthInPixel', () => {

      describe('UI', () => {

        it('makes the label as width as its content', () => {
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
