/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('ButtonLayout', function() {
  var session;
  var helper;

  var CustomButton = function() {
    CustomButton.parent.call(this);
  };
  scout.inherits(CustomButton, scout.Button);

  CustomButton.prototype._render = function() {
    CustomButton.parent.prototype._render.call(this);
    this.$fieldContainer.css({
      display: 'inline-block',
      minHeight: 40,
      minWidth: 100,
      margin: 5
    });

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
  });

  describe('prefSize', function() {
    var button, statusWidth;
    var fieldSize, statusMargins;

    beforeEach(function() {
      statusWidth = scout.HtmlEnvironment.fieldStatusWidth;

      button = new CustomButton();
      button.init({
        parent: session.desktop,
        label: 'abc'
      });
      button.render();
    });

    function readSizes() {
      fieldSize = scout.graphics.prefSize(button.$fieldContainer, true);
      statusMargins = scout.graphics.margins(button.$status);
    }

    describe('statusPosition', function() {
      describe('top', function() {
        it('increases width because status is always on the right side', function() {
          button.setStatusVisible(true);
          button.setLabelPosition(scout.FormField.LabelPosition.TOP);
          button.setStatusPosition(scout.FormField.StatusPosition.TOP);
          readSizes();

          // Status is still on the right side
          var expectedWidth = fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(button.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });
    });
  });
});
