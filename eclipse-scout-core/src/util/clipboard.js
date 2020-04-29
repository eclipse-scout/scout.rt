/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FocusRule, scout, Status} from '../index';
import $ from 'jquery';

/**
 * Copies the given text to the clipboard. To make this work, the method must be called inside
 * a "user action" (i.e. mouse or keyboard event handler). For security reasons, the access to
 * the clipboard is blocked by the browser in other contexts (e.g. asynchronous callbacks).
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------------------------
 * text                     -               The text to write to the clipboard.
 *
 * parent                   -               Widget that wants to copy the text. Recommended.
 *                                          Used to retrieve the session and the document.
 *
 * session                  -               Scout session object, used to resolve texts and access the
 *                                          focus manager. Only required when "parent" is not set.
 *
 * document                 -               The DOM node for the current document. Used to copy to the
 *                                          clipboard in older browsers. Only required when "parent" is
 *                                          not set. If this option is missing, the global "document"
 *                                          object is used, which might cause security exceptions when
 *                                          called from a different document (especially in IE).
 *
 * showNotification         true            If true, a desktop notification is shown when copying has
 *                                          been completed. Requires the "parent" option to be present.
 *                                          If this is true, the method returns null. Otherwise, it
 *                                          returns a promise that is resolved or rejected when the
 *                                          copying is complete.
 *
 * @param options
 *          mandatory, see table above for valid attributes
 * @return a promise or null, see description of "showNotification" option
 */
export function copyText(options) {
  scout.assertParameter('options', options);
  if (options.parent && !options.session) {
    options.session = options.parent.session;
  }
  scout.assertProperty(options, 'session');

  let promise = _copyText(options);

  if (options.parent && scout.nvl(options.showNotification, true)) {
    _showNotification(options, promise);
    return null;
  }
  return promise;
}

export function _copyText(options) {
  // Modern clipboard API
  // https://developer.mozilla.org/en-US/docs/Web/API/Clipboard_API
  if (navigator.clipboard) {
    return navigator.clipboard.writeText(options.text);
  }

  // Fallback for browsers that don't support the modern clipboard API (IE, Safari, Chrome < 66, Firefox < 63)
  // Create invisible textarea field and use document command "copy" to copy the text to the clipboard
  let doc = (options.parent && options.parent.rendered ? options.parent.document(true) : document);
  let f = doc.createElement('textarea');
  f.style.position = 'fixed';
  f.style.opacity = '0.0';
  f.value = options.text;
  doc.body.appendChild(f);
  // Preserve focus
  let $f = $(f);
  options.session.focusManager.installFocusContext($f, FocusRule.AUTO);
  f.select(); // cannot use jquery select(), because that is overridden by jquery-scout

  let deferred = $.Deferred();
  try {
    let successful = doc.execCommand('copy');
    if (successful) {
      deferred.resolve();
    } else {
      deferred.reject();
    }
  } catch (err) {
    deferred.reject(err);
  } finally {
    // Restore focus
    options.session.focusManager.uninstallFocusContext($f);
    doc.body.removeChild(f);
  }
  return deferred.promise();
}

export function _showNotification(options, promise) {
  let status = _successStatus(options.parent.session);
  promise
    .catch(() => {
      status = _failedStatus(options.parent.session);
    })
    .then(() => {
      showNotification(options.parent, status);
    });
}

export function _successStatus(session) {
  return new Status({
    message: session.text('ui.CopyToClipboardSuccessStatus'),
    severity: Status.Severity.INFO
  });
}

export function _failedStatus(session) {
  return new Status({
    message: session.text('ui.CopyToClipboardFailedStatus'),
    severity: Status.Severity.WARNING
  });
}

/**
 * Shows a short desktop notification. By default, it informs the user that the content
 * has been copied to the clipboard successfully. By passing a different status, the
 * message can be changed.
 *
 * @param parent
 *          Widget that wants show the notification. Mandatory. Required for NLS texts.
 */
export function showNotification(parent, status) {
  scout.assertParameter('parent', parent);
  let notification = scout.create('DesktopNotification', {
    parent: parent,
    closable: false,
    duration: 1234,
    status: status || _successStatus(parent.session)
  });
  notification.show();
}

export default {
  copyText,
  showNotification
};
