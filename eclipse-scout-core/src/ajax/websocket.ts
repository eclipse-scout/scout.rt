/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectFactory} from '../ObjectFactory';

let sockets: Set<WebSocket> = new Set<WebSocket>();
export const websocket = {

  open() {
    let location = window.location;
    let protocol = location.protocol.replace('http', 'ws');
    let connection = ObjectFactory.get().createUniqueId();
    let url = `${protocol}//${location.host}${location.pathname}ws/${connection}`;
    const socket = new WebSocket(url);
    sockets.add(socket);

    socket.addEventListener('open', event => {
      socket.send('Hello Server!');
    });
    socket.addEventListener('message', event => {
      console.log(`Message from socket ${socket.url}: ${event.data}`);
    });
    socket.addEventListener('close', event => {
      console.log('Socket closed: ' + socket.url);
      sockets.delete(socket);
    });

    console.log(`Socket opened: ${socket.url}`);
    return socket;
  },

  closeAll() {
    for (const socket of sockets) {
      socket.close();
    }
  }
};
