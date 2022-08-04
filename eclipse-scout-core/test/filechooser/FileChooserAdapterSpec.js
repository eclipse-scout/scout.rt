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
import {FileChooser, RemoteEvent, scout} from '../../src/index';

describe('FileChooserAdapter', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('cancel', () => {
    it('does not close the chooser but sends a cancel event', () => {
      let fileChooser = scout.create(FileChooser, {
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
      let event = new RemoteEvent(fileChooser.id, 'cancel');
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });
  });

});
