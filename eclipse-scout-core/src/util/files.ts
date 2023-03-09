/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, numbers, RoundingMode, Session} from '../index';

export const files = {
  /**
   * Checks if the combined total size of the given files does not exceed the maximum upload size.
   *
   * @param files array of files
   * @param maximumUploadSize maximum combined file size. null disables the size check
   * @returns true if the total size does not exceed maximumUploadSize, otherwise false
   */
  validateMaximumUploadSize(files: Blob | Blob[], maximumUploadSize: number): boolean {
    files = arrays.ensure(files);
    if (files.length === 0 || maximumUploadSize == null) {
      return true;
    }

    let totalSize = files.reduce((total, file) => total + file.size, 0);
    return totalSize <= maximumUploadSize;
  },

  fileListToArray(fileList: FileList): File[] {
    let files = [];
    for (let i = 0; i < fileList.length; i++) {
      files.push(fileList[i]);
    }
    return files;
  },

  getErrorMessageMaximumUploadSizeExceeded(session: Session, maximumUploadSize: number): string {
    if (maximumUploadSize < 1024) {
      return session.text('ui.FileSizeLimit', maximumUploadSize, session.text('ui.Bytes'));
    }
    let maximumUploadSizeKB = maximumUploadSize / 1024;
    if (maximumUploadSizeKB < 1024) {
      return session.text('ui.FileSizeLimit', numbers.round(maximumUploadSizeKB, RoundingMode.HALF_UP, 2), 'KB');
    }
    let maximumUploadSizeMB = maximumUploadSizeKB / 1024;
    if (maximumUploadSizeMB < 1024) {
      return session.text('ui.FileSizeLimit', numbers.round(maximumUploadSizeMB, RoundingMode.HALF_UP, 2), 'MB');
    }
    return session.text('ui.FileSizeLimit', numbers.round(maximumUploadSizeMB / 1024, RoundingMode.HALF_UP, 2), 'GB');
  }
};
