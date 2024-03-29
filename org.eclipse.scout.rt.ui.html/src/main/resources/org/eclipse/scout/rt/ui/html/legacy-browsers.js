/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
var entries = document.getElementsByClassName('scout');
var root = null;
if (entries && entries.length) {
  root = entries[0];
} else {
  root = document.createElement('div');
  root.className = 'scout';
  root.tabIndex = 1;
  root.dataset.partid = '1';
  document.body.appendChild(root);
}

var loadingRoots = document.getElementsByClassName('application-loading-root');
for (var i = 0; i < loadingRoots.length; i++) {
  loadingRoots[i].classList.add('hidden');
}

var xmlHttp = new XMLHttpRequest();
// noinspection JSFunctionExpressionToArrowFunction
xmlHttp.onreadystatechange = function() {
  if (xmlHttp.readyState == 4 && xmlHttp.status >= 200 && xmlHttp.status < 300) {
    root.innerHTML = xmlHttp.responseText;
    var buttonBar = document.getElementsByClassName('button-bar');
    for (var i = 0; i < buttonBar.length; i++) {
      buttonBar[i].classList.add('hidden');
    }
  }
};
xmlHttp.open('GET', 'unsupported-browser.html', true);
xmlHttp.send(null);
