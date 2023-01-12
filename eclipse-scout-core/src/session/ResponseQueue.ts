/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {RemoteRequest, RemoteResponse, Session, strings} from '../index';

export class ResponseQueue {
  session: Session;
  queue: RemoteResponse[];
  lastProcessedSequenceNo: number;
  nextExpectedSequenceNo: number;
  force: boolean;
  forceTimeoutId: number;

  constructor(session: Session) {
    this.session = session;
    this.queue = [];
    this.lastProcessedSequenceNo = 0;
    this.nextExpectedSequenceNo = 1;
    this.force = false;
    this.forceTimeoutId = null;
  }

  /**
   * in milliseconds
   */
  static FORCE_TIMEOUT = 10 * 1000;

  add(response?: RemoteResponse) {
    let sequenceNo = response && response['#'];

    // Ignore responses that were already processed (duplicate detection)
    if (sequenceNo && sequenceNo <= this.lastProcessedSequenceNo) {
      return;
    }

    // "Fast-forward" the expected sequence no. when a combined response is received
    if (sequenceNo && response.combined) {
      this.lastProcessedSequenceNo = Math.max(sequenceNo - 1, this.lastProcessedSequenceNo);
      this.nextExpectedSequenceNo = Math.max(sequenceNo, this.nextExpectedSequenceNo);
    }

    if (!sequenceNo || this.queue.length === 0) { // Handle messages without sequenceNo in the order they were received
      this.queue.push(response);
    } else {
      // Insert at correct position (ascending order)
      let newQueue = [];
      let responseToInsert = response;
      for (let i = 0; i < this.queue.length; i++) {
        let el = this.queue[i];
        if (el['#']) {
          if (responseToInsert && el['#'] > sequenceNo) {
            // insert at position
            newQueue.push(response);
            responseToInsert = null;
          }
          if (el['#'] <= this.lastProcessedSequenceNo) {
            // skip obsolete elements (may happen when a combined response is added to the queue)
            continue;
          }
        }
        newQueue.push(el);
      }
      if (responseToInsert) {
        // no element with bigger seqNo found -> insert as last element
        newQueue.push(responseToInsert);
      }
      this.queue = newQueue;
    }
  }

  process(response?: RemoteResponse): boolean {
    if (response) {
      this.add(response);
    }

    // Process the queue in ascending order
    let responseSuccess = true;
    let missingResponse = false;
    let nonProcessedResponses = [];
    for (let i = 0; i < this.queue.length; i++) {
      let el = this.queue[i];
      let sequenceNo = el['#'];

      // For elements with a sequence number, check if they are in the expected order
      if (sequenceNo) {
        if (this.nextExpectedSequenceNo && !this.force && !missingResponse) {
          missingResponse = (this.nextExpectedSequenceNo !== sequenceNo);
        }
        if (missingResponse) {
          // Sequence is not complete, process those messages later
          nonProcessedResponses.push(el);
          continue;
        }
      }

      // Handle the element
      let success = this.session.processJsonResponseInternal(el);
      // Only return success value of the response that was passed to the process() call
      if (response && el === response) {
        responseSuccess = success;
      }

      // Update the expected next sequenceNo
      if (sequenceNo) {
        this.lastProcessedSequenceNo = sequenceNo;
        this.nextExpectedSequenceNo = sequenceNo + 1;
      }
    }
    // Keep non-processed events (because they are not in sequence) in the queue
    this.queue = nonProcessedResponses;

    this._checkTimeout();

    return responseSuccess;
  }

  size(): number {
    return this.queue.length;
  }

  protected _checkTimeout() {
    // If there are non-processed elements, schedule a job that forces the processing of those
    // elements after a certain timeout to prevent the "blocked forever syndrome" if a response
    // was lost on the network.
    if (this.queue.length === 0) {
      clearTimeout(this.forceTimeoutId);
      this.forceTimeoutId = null;
    } else if (!this.forceTimeoutId) {
      this.forceTimeoutId = setTimeout(() => {
        try {
          let s = '[';
          for (let i = 0; i < this.queue.length; i++) {
            if (i > 0) {
              s += ', ';
            }
            s += (strings.box('#', this.queue[i]['#'] + '') || '<none>');
          }
          s += ']';
          this.session.sendLogRequest('Expected response #' + this.nextExpectedSequenceNo + ' still missing after ' +
            ResponseQueue.FORCE_TIMEOUT + ' ms. Forcing response queue to process ' + this.size() + ' elements: ' + s);
        } catch (error) {
          // nop
        }
        this.force = true;
        try {
          this.process();
        } finally {
          this.force = false;
          this.forceTimeoutId = null;
        }
      }, ResponseQueue.FORCE_TIMEOUT);
    }
  }

  prepareRequest(request: RemoteRequest) {
    request['#ACK'] = this.lastProcessedSequenceNo;
  }

  prepareHttpRequest(ajaxOptions: JQuery.AjaxSettings) {
    ajaxOptions.headers = ajaxOptions.headers || {};
    ajaxOptions.headers['X-Scout-#ACK'] = this.lastProcessedSequenceNo + '';
  }
}
