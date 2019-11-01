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
import {Status} from '../../src/index';


describe("scout.Status", function() {

  describe("convenience functions", function() {

    it("create valid status objects", function() {
      var status;

      // 1. Options argument (default)
      status = Status.error({
        message: 'Oops'
      });
      expect(status.severity).toBe(Status.Severity.ERROR);
      expect(status.message).toBe('Oops');
      expect(status.isError()).toBe(true);
      expect(status.isWarning()).toBe(false);
      expect(status.isInfo()).toBe(false);
      expect(status.isOk()).toBe(false);

      status = Status.warning({
        message: 'foo'
      });
      expect(status.severity).toBe(Status.Severity.WARNING);
      expect(status.message).toBe('foo');
      expect(status.isError()).toBe(false);
      expect(status.isWarning()).toBe(true);
      expect(status.isInfo()).toBe(false);
      expect(status.isOk()).toBe(false);

      status = Status.info({
        message: 'bar'
      });
      expect(status.severity).toBe(Status.Severity.INFO);
      expect(status.message).toBe('bar');
      expect(status.isError()).toBe(false);
      expect(status.isWarning()).toBe(false);
      expect(status.isInfo()).toBe(true);
      expect(status.isOk()).toBe(false);

      status = Status.ok({
        message: 'Okay'
      });
      expect(status.severity).toBe(Status.Severity.OK);
      expect(status.message).toBe('Okay');
      expect(status.isError()).toBe(false);
      expect(status.isWarning()).toBe(false);
      expect(status.isInfo()).toBe(false);
      expect(status.isOk()).toBe(true);

      // 2. String argument (convenience)
      status = Status.error('Oops');
      expect(status.severity).toBe(Status.Severity.ERROR);
      expect(status.message).toBe('Oops');

      status = Status.warning('foo');
      expect(status.severity).toBe(Status.Severity.WARNING);
      expect(status.message).toBe('foo');

      status = Status.info('bar');
      expect(status.severity).toBe(Status.Severity.INFO);
      expect(status.message).toBe('bar');

      status = Status.ok('Okay');
      expect(status.severity).toBe(Status.Severity.OK);
      expect(status.message).toBe('Okay');
    });

  });

});
