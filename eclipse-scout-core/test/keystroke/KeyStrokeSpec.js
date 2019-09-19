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
import {keys, KeyStroke} from '../../src/index';


describe("KeyStroke", function() {
  var session;

  var spyable = function() {
  };

  class TestingKeyStroke extends KeyStroke {
    constructor() {
      super();
      this.parseAndSetKeyStroke("enter");
    }

    handle() {
      expect(false).toBe(true);
      spyable();
    }
  }

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  afterEach(function() {
    session = null;
  });

  describe("unrepeatability", function() {

    var testImpl = function(keyDownCount) {
      var keyStroke = new TestingKeyStroke();

      session.desktop.keyStrokeContext.registerKeyStroke(keyStroke);

      spyOn(keyStroke, 'handle');

      while (keyDownCount-- > 0) {
        session.desktop.$container.triggerKeyDownCapture(keys.ENTER);
      }
      session.desktop.$container.triggerKeyUpCapture(keys.ENTER);

      expect(keyStroke.handle.calls.count())
        .toEqual(1, "because an unrepeatable keystroke should only be invoked once before the closing keyup event");
      expect(keyStroke._handleExecuted)
        .toBe(false, "because an unrepeatable keystroke should be reset after the closing keyup event");
    };

    it("means that an unrepeatable KeyStroke is triggered exactly once per keyup event, even given three keydown events", function() {
      testImpl(3);
    });

    it("means that an unrepeatable KeyStroke is triggered exactly once given the sequence (keydown, keyup)", function() {
      testImpl(1);
    });
  });
});
