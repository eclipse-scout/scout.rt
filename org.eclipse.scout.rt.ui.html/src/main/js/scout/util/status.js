/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.status = {

  Severity: {
    OK: 0x01,
    INFO: 0x100,
    WARNING: 0x10000,
    ERROR: 0x1000000
  },

  animateStatusMessage: function($status, message) {
    if (scout.strings.endsWith(message, '...')) {
      var $elipsis = $status.makeSpan('elipsis');
      for (var i = 0; i < 3; i++) {
        $elipsis.append($status.makeSpan('animate-dot delay-' + i, '.'));
      }
      message = message.substring(0, message.length - 3);
      $status.empty().text(message).append($elipsis);
    } else {
      $status.text(message);
    }
  }

};
