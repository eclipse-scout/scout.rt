/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {idCodec} from '../src/index';

describe('idCodec', () => {

  describe('fromUnqualified', () => {
    it('parses correctly', () => {
      expect(idCodec.fromUnqualified(null, null)).toBeNull();
      expect(idCodec.fromUnqualified('', null)).toBeNull();
      expect(idCodec.fromUnqualified('testId', null)).toEqual({
        typeName: null,
        value: 'testId',
        elements: ['testId'],
        signature: null
      });
      expect(idCodec.fromUnqualified('testId1;testId2;testId3_-~SIG~-_sig', 'testTypeName')).toEqual({
        typeName: 'testTypeName',
        value: 'testId1;testId2;testId3',
        elements: ['testId1', 'testId2', 'testId3'],
        signature: 'sig'
      });
    });
  });

  describe('fromQualified', () => {
    it('parses correctly', () => {
      expect(idCodec.fromQualified(null)).toBeNull();
      expect(idCodec.fromQualified('')).toBeNull();
      expect(idCodec.fromQualified('testId')).toEqual({
        typeName: null,
        value: 'testId',
        elements: ['testId'],
        signature: null
      });
      expect(idCodec.fromQualified('testTypeName:testId1;testId2;testId3_-~SIG~-_sig')).toEqual({
        typeName: 'testTypeName',
        value: 'testId1;testId2;testId3',
        elements: ['testId1', 'testId2', 'testId3'],
        signature: 'sig'
      });
    });
  });

  describe('toUnqualified', () => {
    it('works correctly', () => {
      expect(idCodec.toUnqualified(null)).toBeNull();
      expect(idCodec.toUnqualified({value: 'testId'})).toBe('testId');
      expect(idCodec.toUnqualified({
        typeName: 'testTypeName',
        value: 'testId1;testId2;testId3',
        signature: 'sig'
      })).toBe('testId1;testId2;testId3_-~SIG~-_sig');
    });
  });

  describe('toQualified', () => {
    it('works correctly', () => {
      expect(idCodec.toQualified(null)).toBeNull();
      expect(idCodec.toQualified({value: 'testId'})).toBe('testId');
      expect(idCodec.toQualified({
        typeName: 'testTypeName',
        value: 'testId1;testId2;testId3',
        signature: 'sig'
      })).toBe('testTypeName:testId1;testId2;testId3_-~SIG~-_sig');
    });
  });
});
