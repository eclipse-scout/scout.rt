/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {System} from '../../src';

describe('System', () => {
  describe('init', () => {
    it('correctly initializes', () => {
      let system = new System();
      system.init({
        name: 'test',
        baseUrl: 'abc/',
        hasUiBackend: true,
        endpointUrls: {
          a: null,
          b: undefined,
          c: '/rel/',
          d: 'rel2'
        }
      });
      expect(system.name).toBe('test');
      expect(system.baseUrl).toBe('abc/');
      expect(system.hasUiBackend).toBeTrue();
      expect(system.getEndpointUrl('a')).toBeNull();
      expect(system.getEndpointUrl('a', 'def')).toBe('abc/def');
      expect(system.getEndpointUrl('b')).toBeNull();
      expect(system.getEndpointUrl('c')).toBe('abc/rel');
      expect(system.getEndpointUrl('c', 'whatEver')).toBe('abc/rel');
      expect(system.getEndpointUrl('d', 'whatEver')).toBe('abc/rel2');
      expect(system.getEndpointUrl('notExisting')).toBeNull();
      expect(system.getEndpointUrl('notExisting', '/default/')).toBe('abc/default');
    });

    it('uses defaults for system name', () => {
      let system = new System();
      system.init({name: 'test'});
      expect(system.name).toBe('test');
      expect(system.baseUrl).toBe('api');
      expect(system.hasUiBackend).toBeFalse();
      expect(system.getEndpointUrl('a', 'def')).toBe('api/def');
      expect(system.getConfigEndpointUrls()).toEqual(['api/config-properties']);
    });

    it('uses defaults for main', () => {
      let system = new System();
      system.init({name: System.MAIN_SYSTEM});
      expect(system.name).toBe(System.MAIN_SYSTEM);
      expect(system.baseUrl).toBe('api');
      expect(system.hasUiBackend).toBeTrue();
      expect(system.getEndpointUrl('a', 'def')).toBe('api/def');
      expect(system.getConfigEndpointUrls()).toEqual(['res/config-properties.json']);
    });
  });

  describe('setEndpointUrl', () => {
    it('updates endpoint in map', () => {
      let system = new System();
      system.init({name: System.MAIN_SYSTEM});
      expect(system.getEndpointUrl('a', 'def')).toBe('api/def');
      expect(system.getEndpointUrl('a')).toBeNull();
      system.setEndpointUrl('a', 'a-url');
      expect(system.getEndpointUrl('a', 'def')).toBe('api/a-url');
      expect(system.getEndpointUrl('a')).toBe('api/a-url');
      system.setEndpointUrl('a', null);
      expect(system.getEndpointUrl('a', 'def')).toBe('api/def');
      expect(system.getEndpointUrl('a')).toBeNull();
    });
  });

  describe('getConfigEndpointUrls', () => {
    it('includes resource from UI', () => {
      let system = new System();
      system.init({name: 'test'});
      expect(system.hasUiBackend).toBeFalse();
      expect(system.getConfigEndpointUrls()).toEqual(['api/config-properties']);
      system.setHasUiBackend(true);
      expect(system.getConfigEndpointUrls()).toEqual(['res/config-properties.json']);
      system.setEndpointUrl('config-properties', 'test-config');
      expect(system.getConfigEndpointUrls()).toEqual(['res/config-properties.json', 'api/test-config']);
    });
  });
});
