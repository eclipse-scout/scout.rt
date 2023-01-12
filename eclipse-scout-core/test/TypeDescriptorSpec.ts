/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, TypeDescriptor} from '../src/index';

describe('ObjectInfo', () => {

  describe('parse', () => {

    it('parses objectType only - without namespace', () => {
      let info = TypeDescriptor.parse('StringField');
      expect(info.modelVariant).toBe(null);
      expect(info.objectType.name).toBe('StringField');
      expect(arrays.empty(info.objectType.namespaces)).toBe(true);
    });

    it('parses objectType only - with namespace', () => {
      let info = TypeDescriptor.parse('my.StringField');
      expect(info.modelVariant).toBe(null);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my']);
    });

    it('parses objectType and variant - without namespace', () => {
      let info = TypeDescriptor.parse('StringField:Variant');
      expect(info.modelVariant.name).toBe('Variant');
      expect(arrays.empty(info.modelVariant.namespaces)).toBe(true);
      expect(info.objectType.name).toBe('StringField');
      expect(arrays.empty(info.objectType.namespaces)).toBe(true);
    });

    it('parses objectType and variant - with namespace on objectType (model variant inherits namespace from object type)', () => {
      let info = TypeDescriptor.parse('my.StringField:Variant');
      expect(info.modelVariant.name).toBe('Variant');
      expect(info.modelVariant.namespaces).toEqual(['my']);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my']);
    });

    it('parses objectType and variant - different namespaces for model variant and  object type', () => {
      let info = TypeDescriptor.parse('my.StringField:your.Variant');
      expect(info.modelVariant.name).toBe('Variant');
      expect(info.modelVariant.namespaces).toEqual(['your']);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my']);
    });

    it('parses objectType with multiple nested namespaces', () => {
      let info = TypeDescriptor.parse('my.fair.StringField');
      expect(info.modelVariant).toBe(null);
      expect(info.objectType.name).toBe('StringField');
      expect(info.objectType.namespaces).toEqual(['my', 'fair']);
    });

  });

  describe('toString', () => {

    it('returns correct string', () => {
      let info = TypeDescriptor.parse('my.pretty.StringField:your.Variant');
      expect(info.objectType.toString()).toBe('my.pretty.StringField');
      expect(info.modelVariant.toString()).toBe('your.Variant');
    });

  });

});
