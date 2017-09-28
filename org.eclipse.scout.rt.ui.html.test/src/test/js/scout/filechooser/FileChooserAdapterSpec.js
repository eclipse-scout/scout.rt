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
describe('FileChooserAdapter', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('cancel', function() {
    it('does not close the chooser but sends a cancel event', function() {
      var fileChooser = scout.create('FileChooser', {
        parent: session.desktop
      });
      linkWidgetAndAdapter(fileChooser, 'FileChooserAdapter');
      fileChooser.open();
      expect(fileChooser.rendered).toBe(true);
      expect($('.file-chooser').length).toBe(1);

      fileChooser.cancel();
      expect(fileChooser.rendered).toBe(true);
      expect($('.file-chooser').length).toBe(1);

      sendQueuedAjaxCalls();
      var event = new scout.RemoteEvent(fileChooser.id, 'cancel');
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });
  });

});
