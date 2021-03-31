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
import {DesktopNotification, scout, Status, Widget} from '../../../src/index';

describe('DesktopNotification', () => {
  let session, $sandbox,
    parent = new Widget();

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = session.$entryPoint;
    parent.session = session;
  });

  it('will fade in, be added to the desktop and be renderd upon show() ', () => {
    let notification = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: -1
    });
    spyOn(notification, 'fadeIn').and.callThrough();
    notification.show();
    expect(notification.rendered).toBe(true);
    expect(notification.fadeIn).toHaveBeenCalled();
    expect(session.desktop.notifications[0]).toBe(notification);
  });

  it('will fade out and be removed from the dektop upon hide()', () => {
    let notification = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: -1
    });
    spyOn(notification, 'fadeOut').and.callThrough();
    notification.show();
    notification.hide();
    expect(notification.fadeOut).toHaveBeenCalled();
    expect(session.desktop.notifications[0]).toBe(undefined);
  });

  it('_init copies properties from event (model)', () => {
    let notification = new DesktopNotification();
    notification.init({
      parent: parent,
      id: 'foo',
      duration: 123,
      closable: true,
      status: {
        message: 'bar',
        severity: Status.Severity.OK
      }
    });
    expect(notification.id).toBe('foo');
    expect(notification.duration).toBe(123);
    expect(notification.closable).toBe(true);
    expect(notification.status.message).toBe('bar');
    expect(notification.status.severity).toBe(Status.Severity.OK);
  });

  it('has close-icon when notification is closable', () => {
    let notification = scout.create('DesktopNotification', {
      parent: parent,
      id: 'foo',
      duration: 123,
      closable: true,
      status: {
        message: 'bar',
        severity: Status.Severity.OK
      }
    });

    notification.render($sandbox);
    expect(notification.$container.find('.closer').length).toBe(1);
    expect(notification.$container.find('.desktop-notification-content').text()).toBe('bar');
    expect(notification.$container.hasClass('ok')).toBe(true);
  });

  describe('native notification', () => {
    describe('nativeNotificationVisibility', () => {

      it('background: shows native notification only when document is hidden', done => {
        let notification = scout.create('DesktopNotification', {
          parent: parent,
          id: 'foo',
          duration: -1,
          nativeOnly: false,
          nativeNotificationVisibility: 'background',
          closable: true,
          status: {
            message: 'bar',
            severity: Status.Severity.OK
          }
        });

        spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
          resolve('granted');
        }));

        spyOn(notification, '_isDocumentHidden').and.returnValue(true);

        notification.render($sandbox);

        setTimeout(() => {
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          done();
        }, 10);
      });

      it('background: dont show native notification when document is not hidden', () => {
        let notification = scout.create('DesktopNotification', {
          parent: parent,
          id: 'foo',
          duration: 123,
          nativeOnly: false,
          nativeNotificationVisibility: 'background',
          closable: true,
          status: {
            message: 'bar',
            severity: Status.Severity.OK
          }
        });

        spyOn(notification, '_isDocumentHidden').and.returnValue(false);

        notification.render($sandbox);
        expect(notification.nativeNotification).toBeNull();
      });

      it('always: shows native notification even when document is focused', done => {
        let notification = scout.create('DesktopNotification', {
          parent: parent,
          id: 'foo',
          duration: -1,
          nativeOnly: true,
          nativeNotificationVisibility: 'always',
          closable: true,
          status: {
            message: 'bar',
            severity: Status.Severity.OK
          }
        });

        spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
          resolve('granted');
        }));

        notification.render($sandbox);

        setTimeout(() => {
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          done();
        }, 10);
      });

      it('none: never shows native notification', () => {
        let notification = scout.create('DesktopNotification', {
          parent: parent,
          id: 'foo',
          duration: -1,
          nativeOnly: false,
          nativeNotificationVisibility: 'none',
          closable: true,
          status: {
            message: 'bar',
            severity: Status.Severity.OK
          }
        });
        notification.render($sandbox);
        expect(notification.nativeNotification).toBeNull();
        expect(notification.isVisible()).toBeTruthy();
      });
    });
    describe('nativeOnly', () => {
      it('true shows only the native notification', done => {
        let notification = scout.create('DesktopNotification', {
          parent: parent,
          id: 'foo',
          duration: -1,
          nativeOnly: true,
          nativeNotificationVisibility: 'always',
          closable: true,
          status: {
            message: 'bar',
            severity: Status.Severity.OK
          }
        });

        spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
          resolve('granted');
        }));

        notification.render($sandbox);
        setTimeout(() => {
          expect(notification.isVisible()).toBeFalsy();
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          done();
        }, 10);
      });

      it('false shows both, the desktop and the native notification', done => {
        let notification = scout.create('DesktopNotification', {
          parent: parent,
          id: 'foo',
          duration: -1,
          nativeOnly: false,
          nativeNotificationVisibility: 'always',
          closable: true,
          status: {
            message: 'bar',
            severity: Status.Severity.OK
          }
        });

        spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
          resolve('granted');
        }));

        notification.render($sandbox);

        setTimeout(() => {
          expect(notification.isVisible()).toBeTruthy();
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          done();
        }, 10);
      });
    });

    it('is disposed immediately if duration is set to infinite', done => {
      let notification = scout.create('DesktopNotification', {
        parent: parent,
        id: 'foo',
        duration: -1,
        nativeOnly: true,
        nativeNotificationVisibility: 'always',
        closable: true,
        status: {
          message: 'bar',
          severity: Status.Severity.OK
        }
      });

      spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
        resolve('granted');
      }));

      spyOn(notification, 'fadeOut').and.callThrough();
      notification.show();

      setTimeout(() => {
        expect(notification.destroyed).toBeTruthy();
        expect(notification.session.desktop.notifications.length).toBe(0);
        expect(notification.isVisible()).toBeFalsy();
        expect(notification.nativeNotification).not.toBeNull();
        done();
      }, 10);
    });

    it('is disposed later if duration is > 0', done => {
      let notification = scout.create('DesktopNotification', {
        parent: parent,
        id: 'foo',
        duration: 100,
        nativeOnly: true,
        nativeNotificationVisibility: 'always',
        closable: true,
        status: {
          message: 'bar',
          severity: Status.Severity.OK
        }
      });

      spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
        resolve('granted');
      }));

      spyOn(notification, 'fadeOut').and.callThrough();
      notification.show();

      setTimeout(() => {
        expect(notification.destroyed).toBeFalsy();
        expect(session.desktop.notifications[0]).toBe(notification);
        expect(notification.isVisible()).toBeFalsy();
        expect(notification.nativeNotification).not.toBeNull();
      }, 10);

      setTimeout(() => {
        expect(notification.session.desktop.notifications.length).toBe(0);
        done();
      }, 200);
    });

    it('show no native notification if permission is denied', done => {
      let notification = scout.create('DesktopNotification', {
        parent: parent,
        id: 'foo',
        duration: -1,
        nativeOnly: true,
        nativeNotificationVisibility: 'always',
        closable: true,
        status: {
          message: 'bar',
          severity: Status.Severity.OK
        }
      });

      spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
        resolve('denied');
      }));

      spyOn(notification, 'fadeOut').and.callThrough();
      notification.show();

      setTimeout(() => {
        expect(notification.isVisible()).toBeFalsy();
        expect(notification.nativeNotification).toBeNull();
        done();
      }, 10);
    });
  });
});
