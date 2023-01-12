/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FileChooser, RemoteEvent, scout} from '../../src/index';

describe('FileChooserAdapter', () => {
  let session: SandboxSession;

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
