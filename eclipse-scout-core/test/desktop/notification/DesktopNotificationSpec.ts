/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopNotification, scout, Status, Widget} from '../../../src/index';

describe('DesktopNotification', () => {
  let session: SandboxSession, $sandbox: JQuery,
    parent = new Widget();

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = session.$entryPoint;
    parent.session = session;
  });

  it('will fade in, be added to the desktop and be rendered upon show() ', () => {
    let notification = scout.create(DesktopNotification, {
      parent: parent
    });
    spyOn(notification, 'fadeIn').and.callThrough();
    notification.show();
    expect(notification.rendered).toBe(true);
    expect(notification.fadeIn).toHaveBeenCalled();
    expect(session.desktop.notifications[0]).toBe(notification);
  });

  it('will fade out and be removed from the desktop upon hide()', () => {
    let notification = scout.create(DesktopNotification, {
      parent: parent
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
    let notification = scout.create(DesktopNotification, {
      parent: parent,
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

    function grantPermission() {
      spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
        resolve('granted');
      }));
      spyOnProperty(Notification, 'permission', 'get').and.returnValue('granted');
    }

    describe('nativeNotificationVisibility', () => {

      beforeEach(() => {
        grantPermission();
      });

      it('background: shows native notification only when document is hidden', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeNotificationVisibility: 'background',
          message: 'bar'
        });

        spyOn(notification, '_isDocumentHidden').and.returnValue(true);

        notification.render($sandbox);
        setTimeout(() => {
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          notification.hide();
          done();
        }, 10);
      });

      it('background: dont show native notification when document is not hidden', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeNotificationVisibility: 'background',
          message: 'bar'
        });

        spyOn(notification, '_isDocumentHidden').and.returnValue(false);

        notification.render($sandbox);
        setTimeout(() => {
          expect(notification.nativeNotification).toBeNull();
          notification.hide();
          done();
        }, 10);
      });

      it('always: shows native notification even when document is focused', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeNotificationVisibility: 'always',
          message: 'bar'
        });

        spyOn(notification, '_isDocumentHidden').and.returnValue(false);

        notification.render($sandbox);
        setTimeout(() => {
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          done();
        }, 10);
      });

      it('none: never shows native notification', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeNotificationVisibility: 'none',
          message: 'bar'
        });
        notification.render($sandbox);

        spyOn(notification, '_isDocumentHidden').and.returnValue(true);

        setTimeout(() => {
          expect(notification.nativeNotification).toBeNull();
          expect(notification.visible).toBeTruthy();
          done();
        }, 10);
      });
    });

    describe('nativeOnly', () => {

      beforeEach(() => {
        grantPermission();
      });

      it('true shows only the native notification', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeOnly: true,
          nativeNotificationVisibility: 'always',
          message: 'bar'
        });

        notification.render($sandbox);
        setTimeout(() => {
          expect(notification.visible).toBeFalsy();
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          notification.hide();
          done();
        }, 10);
      });

      it('false shows both, the desktop and the native notification', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeOnly: false,
          nativeNotificationVisibility: 'always',
          message: 'bar'
        });

        notification.render($sandbox);

        setTimeout(() => {
          expect(notification.visible).toBeTruthy();
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');
          notification.hide();
          done();
        }, 10);
      });
    });

    describe('destroy', () => {

      beforeEach(() => {
        grantPermission();
      });

      it('destroying the notification also destroys the native one', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          nativeNotificationVisibility: 'always',
          message: 'bar'
        });

        notification.show();

        setTimeout(() => {
          expect(notification.destroyed).toBe(false);
          expect(notification.session.desktop.notifications.length).toBe(1);
          expect(notification.nativeNotification).not.toBeNull();
          expect(notification.nativeNotification.body).toBe('bar');

          spyOn(notification.nativeNotification, 'close').and.callThrough();
          notification.hide();
          notification.$container.trigger('animationend');
        }, 10);

        setTimeout(() => {
          expect(notification.destroyed).toBe(true);
          expect(notification.session.desktop.notifications.length).toBe(0);
          // Close event handler is not triggered on Chrome Headless, so notification.nativeNotification won't be set to null. Looks like a Chrome bug
          expect(notification.nativeNotification.close).toHaveBeenCalled();
          done();
        }, 20);
      });

      it('native notification is destroyed later if duration is > 0, along with the regular notification', done => {
        let notification = scout.create(DesktopNotification, {
          parent: parent,
          duration: 100,
          nativeOnly: true,
          nativeNotificationVisibility: 'always',
          message: 'bar'
        });

        notification.show();

        setTimeout(() => {
          expect(notification.destroyed).toBeFalsy();
          expect(notification.session.desktop.notifications[0]).toBe(notification);
          expect(notification.visible).toBeFalsy();
          expect(notification.nativeNotification).not.toBeNull();
          spyOn(notification.nativeNotification, 'close').and.callThrough();
        }, 10);

        setTimeout(() => {
          expect(notification.destroyed).toBe(true);
          expect(notification.session.desktop.notifications.length).toBe(0);
          expect(notification.nativeNotification.close).toHaveBeenCalled();
          done();
        }, 200);
      });
    });

    it('shows no native notification if permission is denied', done => {
      let notification = scout.create(DesktopNotification, {
        parent: parent,
        nativeNotificationVisibility: 'always',
        message: 'bar'
      });

      spyOn(Notification, 'requestPermission').and.returnValue(new Promise((resolve, reject) => {
        resolve('denied');
      }));

      notification.show();

      setTimeout(() => {
        expect(notification.nativeNotification).toBeNull();
        notification.hide();
        done();
      }, 10);
    });
  });

  describe('aria properties', () => {

    it('has aria role alert', () => {
      let notification = scout.create(DesktopNotification, {
        parent: parent,
        closable: true,
        status: {
          message: 'bar',
          severity: Status.Severity.OK
        }
      });

      notification.render($sandbox);
      expect(notification.$container).toHaveAttr('role', 'alert');
    });
  });
});
