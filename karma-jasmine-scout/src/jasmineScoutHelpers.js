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

window.mostRecentJsonRequest = function() {
  var req = jasmine.Ajax.requests.mostRecent();
  if (req) {
    return $.parseJSON(req.params);
  }
};

window.sandboxDesktop = function() {
  var $sandbox = window.sandbox();
  $sandbox.addClass('scout desktop');
  return $sandbox;
};

/**
 * Sends the queued requests and simulates a response as well.
 * @param response if not set an empty success response will be generated
 */
window.sendQueuedAjaxCalls = function(response, time) {
  time = time || 0;
  jasmine.clock().tick(time);

  receiveResponseForAjaxCall('', response);
};

window.receiveResponseForAjaxCall = function(request, response) {
  if (!response) {
    response = {
      status: 200,
      responseText: '{"events":[]}'
    };
  }
  if (!request) {
    request = jasmine.Ajax.requests.mostRecent();
  }
  if (request && request.onload) {
    request.respondWith(response);
  }
}

/**
 * Uninstalls 'beforeunload' and 'unload' events from window that were previously installed by session.start()
 */
window.uninstallUnloadHandlers = function(session) {
  $(window)
    .off('beforeunload.' + session.uiSessionId)
    .off('unload.' + session.uiSessionId);
};

window.createPropertyChangeEvent = function(model, properties) {
  return {
    target: model.id,
    properties: properties,
    type: 'property'
  };
};

/**
 * Returns a new object instance having two properties id, objectType from the given widgetModel.
 * this function is required because the model object passed to the scout.create() function is modified
 * --> model.objectType is changed to whatever string is passed as parameter objectType
 *
 * @param widgetModel
 */
window.createAdapterModel = function(widgetModel) {
  return {
    id: widgetModel.id,
    objectType: widgetModel.objectType,
    session: widgetModel.session
  };
};

window.stripCommentsFromJson = function(input) {
  if (!input || typeof input !== 'string') {
    return input;
  }
  var result = '';
  var whitespaceBuffer = '';
  for (var i = 0; i < input.length; i++) {
    var previousCharacter = input.charAt(i - 1);
    var currentCharacter = input.charAt(i);
    var nextCharacter = input.charAt(i + 1);

    // Add whitespace to a buffer (because me might want to ignore it at the end of a line)
    if (currentCharacter === ' ' || currentCharacter === '\t') {
      whitespaceBuffer += currentCharacter;
      continue;
    }
    // Handle end of line
    if (currentCharacter === '\r') {
      if (nextCharacter === '\n') {
        // Handle \r\n as \n
        continue;
      }
      // Handle \r as \n
      currentCharacter = '\n';
    }
    if (currentCharacter === '\n') {
      whitespaceBuffer = ''; // discard whitespace
      // Add line break (but not at the begin and not after another line break)
      if (result.charAt(result.length - 1) !== '\n') {
        result += currentCharacter;
      }
      continue;
    }

    // Handle strings
    if (currentCharacter === '"' && previousCharacter !== '\\') {
      // Flush whitespace to result
      result += whitespaceBuffer;
      whitespaceBuffer = '';
      result += currentCharacter;
      for (i++; i < input.length; i++) {
        previousCharacter = input.charAt(i - 1);
        currentCharacter = input.charAt(i);
        nextCharacter = input.charAt(i + 1);
        result += currentCharacter;
        if (currentCharacter === '"' && previousCharacter !== '\\') {
          break; // end of string
        }
      }
    }
    // Handle multi-line comments
    else if (currentCharacter === '/' && nextCharacter === '*') {
      for (i++; i < input.length; i++) {
        previousCharacter = input.charAt(i - 1);
        currentCharacter = input.charAt(i);
        nextCharacter = input.charAt(i + 1);
        if (currentCharacter === '/' && previousCharacter === '*') {
          break; // end of multi-line comment
        }
      }
    }
    // Handle single-line comment
    else if (currentCharacter === '/' && nextCharacter === '/') {
      for (i++; i < input.length; i++) {
        previousCharacter = input.charAt(i - 1);
        currentCharacter = input.charAt(i);
        nextCharacter = input.charAt(i + 1);
        if (nextCharacter === '\n' || nextCharacter === '\r') {
          break; // end of single-line comment
        }
      }
    }
    // regular character
    else {
      // Flush whitespace to result
      result += whitespaceBuffer;
      whitespaceBuffer = '';
      result += currentCharacter;
    }
  }
  return result;
};
