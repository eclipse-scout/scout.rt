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
import {Button, FormField, graphics, HtmlEnvironment} from '../../../../src/index';


describe('ButtonLayout', function() {
  var session;

  class CustomButton extends Button {
    _render() {
      super._render();
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
    }
  }


  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('prefSize', function() {
    var button, statusWidth;
    var fieldSize, statusMargins;

    beforeEach(function() {
      statusWidth = HtmlEnvironment.get().fieldStatusWidth;

      button = new CustomButton();
      button.init({
        parent: session.desktop,
        label: 'abc'
      });
      button.render();
    });

    function readSizes() {
      fieldSize = graphics.prefSize(button.$fieldContainer, true);
      statusMargins = graphics.margins(button.$status);
    }

    describe('statusPosition', function() {
      describe('top', function() {
        it('increases width because status is always on the right side', function() {
          button.setStatusVisible(true);
          button.setLabelPosition(FormField.LabelPosition.TOP);
          button.setStatusPosition(FormField.StatusPosition.TOP);
          readSizes();

          // Status is still on the right side
          var expectedWidth = fieldSize.width + statusWidth + statusMargins.horizontal();
          expect(button.htmlComp.prefSize().width).toBe(expectedWidth);
        });
      });
    });
  });
});
