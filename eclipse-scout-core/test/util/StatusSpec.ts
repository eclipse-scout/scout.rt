/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DefaultStatus, NotificationBadgeStatus, ParsingFailedStatus, scout, Status, ValidationFailedStatus} from '../../src/index';

describe('Status', () => {

  describe('clone', () => {
    it('creates the correct instance', () => {
      const orig = scout.create(NotificationBadgeStatus, {
        severity: Status.Severity.WARNING,
        message: 'test'
      });
      const copy = orig.clone();
      expect(copy).not.toBe(orig);
      expect(copy).toBeInstanceOf(NotificationBadgeStatus);
      expect(copy.severity).toEqual(Status.Severity.WARNING);
      expect(copy.message).toEqual('test');
    });

    it('creates a deep copy', () => {
      const orig = scout.create({
        objectType: ValidationFailedStatus,
        severity: Status.Severity.WARNING,
        message: 'deep-test'
      });
      orig.children = [scout.create(ParsingFailedStatus, {severity: Status.Severity.INFO, message: 'nested-test'})];
      expect(orig).toBeInstanceOf(ValidationFailedStatus);
      expect(orig.children[0]).toBeInstanceOf(ParsingFailedStatus);

      const copy = orig.clone();
      expect(copy).not.toBe(orig);
      expect(copy).toBeInstanceOf(ValidationFailedStatus);

      const childCopy = copy.children[0];
      expect(childCopy).toBeInstanceOf(ParsingFailedStatus);
      expect(childCopy.severity).toBe(Status.Severity.INFO);
      expect(childCopy.message).toBe('nested-test');

      childCopy.severity = Status.Severity.WARNING;
      expect(orig.children[0].severity).toBe(Status.Severity.INFO);
      expect(copy.children[0].severity).toBe(Status.Severity.WARNING);
    });
  });

  describe('convenience functions', () => {

    it('create valid status objects', () => {
      let status: Status;

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

      status = new Status({children: [Status.error('error')]});
      expect(status.severity).toBe(Status.Severity.ERROR);
      expect(status.message).toBe('error');
    });

  });

  it('addStatus / hasChildren', () => {
    let status = Status.error('root');
    expect(status.hasChildren()).toBe(false);
    status.addStatus(Status.info('foo'));
    expect(status.hasChildren()).toBe(true);
    status.removeAllStatus(Status);
    expect(status.hasChildren()).toBe(false);
  });

  it('addStatuses', () => {
    let status = Status.error('root').addStatuses(Status.info('info'), Status.warning('warning'));
    expect(status.severity).toBe(Status.Severity.WARNING);

    status.addStatuses(Status.info('info'), Status.error({severity: Status.Severity.ERROR, message: 'error', code: 100}), Status.warning('warning'));
    expect(status.severity).toBe(Status.Severity.ERROR);
    expect(status.children.length).toBe(5);
    expect(status.message).toBe('error');
    expect(status.code).toBe(100);

    status.addStatuses();
    expect(status.children.length).toBe(5);

    status = Status.ok().addStatuses(Status.warning().addStatuses(Status.error('error'), Status.warning('warning')));
    expect(status.severity).toBe(Status.Severity.ERROR);
  });

  it('removeAllStatus', () => {
    let status = Status.error('root');
    status.addStatus(ParsingFailedStatus.error('foo'));
    status.addStatus(Status.error('bar'));
    expect(status.children.length).toEqual(2);
    status.removeAllStatus(Status);
    expect(status.hasChildren()).toBe(false); // because Status is the base-class of all status

    // only remove status with type DefaultStatus
    status.addStatus(ParsingFailedStatus.error('foo'));
    status.addStatus(DefaultStatus.error('bar'));
    status.removeAllStatus(DefaultStatus);
    expect(status.children.length).toEqual(1);
    expect(status.children[0].message).toEqual('foo');
  });

  it('containsStatus', () => {
    let status = Status.error('root');

    expect(status.containsStatus(ParsingFailedStatus)).toBe(false);
    status.addStatus(ParsingFailedStatus.error('foo'));
    expect(status.containsStatus(ParsingFailedStatus)).toBe(true);

    expect(status.containsStatus(DefaultStatus)).toBe(false);
    status.addStatus(DefaultStatus.error('bar'));
    expect(status.containsStatus(DefaultStatus)).toBe(true);
  });

  it('updateProperties', () => {
    let status = Status.ok('root');
    status.addStatus(ParsingFailedStatus.error('foo'));
    status.addStatus(DefaultStatus.warning('bar'));

    expect(status.message).toEqual('foo');
    expect(status.severity).toEqual(Status.Severity.ERROR);

    // use properties from last remaining status (DefaultStatus)
    status.removeAllStatus(ParsingFailedStatus);
    expect(status.message).toEqual('bar');
    expect(status.severity).toEqual(Status.Severity.WARNING);

    // ParsingFailed should have the higher priority than DefaultStatus
    status = Status.ok('root');
    status.addStatus(DefaultStatus.error('baz'));
    status.addStatus(ParsingFailedStatus.error('foo'));
    expect(status.message).toEqual('foo');
    expect(status.severity).toEqual(Status.Severity.ERROR);

    // when the last child is removed
    status.removeAllStatus(Status);
    expect(status.message).toEqual(null);
    expect(status.severity).toEqual(Status.Severity.OK);
  });

  it('equals', () => {
    let a = Status.ok('root');
    let b = Status.ok('root');
    expect(a.equals(b)).toBe(true);
    expect(b.equals(a)).toBe(true);

    // make sure property 'children' is checked
    a.addStatus(Status.error('foo'));
    expect(a.equals(b)).toBe(false);
    expect(b.equals(a)).toBe(false);

    b.addStatus(Status.error('foo'));
    expect(a.equals(b)).toBe(true);
    expect(b.equals(a)).toBe(true);
  });

  describe('ensureChildren', () => {

    it('status with no children should be transformed in a status with children', () => {
      let status = Status.ok('foo');
      expect(status.children).toBe(null);
      let newStatus = status.ensureChildren();
      expect(newStatus.children.length).toEqual(1);
      expect(status).toBe(newStatus.children[0]);
    });

    it('status with children should return a clone', () => {
      let status = Status.ensure({
        children: [{message: 'foo'}]
      });
      expect(status.children.length).toEqual(1);
      let newStatus = status.ensureChildren();
      expect(newStatus.children.length).toEqual(1);
      expect(status).not.toBe(newStatus);
    });

  });

  describe('asFlatList', () => {
    it('sorts NotificationBadgeStatus to the front', () => {
      const numericNotificationBadgeStatus = new NotificationBadgeStatus({message: '42'}),
        alphanumericNotificationBadgeStatus = new NotificationBadgeStatus({message: 'lorem ipsum dolor'}),
        errorStatus = Status.error(),
        ms = new Status({
          children: [
            numericNotificationBadgeStatus,
            {
              children: [
                errorStatus,
                alphanumericNotificationBadgeStatus
              ]
            }
          ]
        });

      expect(ms.asFlatList()).toEqual([numericNotificationBadgeStatus, alphanumericNotificationBadgeStatus, errorStatus]);
    });
  });
});
