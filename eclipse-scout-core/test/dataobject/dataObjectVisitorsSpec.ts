/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, DataObjectVisitHelper, dataObjectVisitors, numbers, scout, TreeVisitResult, typeName} from '../../src';

describe('dataObjectVisitors', () => {

  describe('forEach', () => {

    it('visits only requested types', () => {
      let visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const mixedTypes = {
        a: 1,
        b: true,
        c: 'some',
        f: scout.create(FooDo, {foo: 'FOO'}),
        l: scout.create(LoremDo, {lorem: 'LOREM'})
      };

      dataObjectVisitors.forEach(mixedTypes, null, consumer);
      expect(visitedNodes).toEqual([mixedTypes]);

      visitedNodes = [];
      dataObjectVisitors.forEach(mixedTypes, FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo, {foo: 'FOO'})]);

      visitedNodes = [];
      dataObjectVisitors.forEach(mixedTypes, LoremDo, consumer);
      expect(visitedNodes).toEqual([scout.create(LoremDo, {lorem: 'LOREM'})]);
    });

    it('does visit non matched objects recursively', () => {
      const visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const lorem = scout.create(LoremDo, {foo: scout.create(FooDo)});

      dataObjectVisitors.forEach(lorem, FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo)]);
    });

    it('does not visit matched objects recursively', () => {
      const visitedFoos = [];
      const consumer = (foo: FooDo) => visitedFoos.push(foo.foo);
      const lorem = scout.create(LoremDo, {
        foo: scout.create(FooDo, {
          foo: 'FOO1',
          lorem: scout.create(LoremDo, {
            foo: scout.create(FooDo, {
              foo: 'FOO2'
            })
          })
        })
      });

      dataObjectVisitors.forEach(lorem, FooDo, consumer);
      expect(visitedFoos).toEqual(['FOO1']);
    });
  });

  describe('forEachRec', () => {

    it('visits only requested types', () => {
      let visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const mixedTypes = {
        a: 1,
        b: true,
        c: 'some',
        f: scout.create(FooDo, {foo: 'FOO'}),
        l: scout.create(LoremDo, {lorem: 'LOREM'})
      };

      dataObjectVisitors.forEachRec(mixedTypes, null, consumer);
      expect(visitedNodes).toEqual([mixedTypes, 1, true, 'some', scout.create(FooDo, {foo: 'FOO'}), 'FOO', scout.create(LoremDo, {lorem: 'LOREM'}), 'LOREM']);

      visitedNodes = [];

      dataObjectVisitors.forEachRec(mixedTypes, FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo, {foo: 'FOO'})]);

      visitedNodes = [];
      dataObjectVisitors.forEachRec(mixedTypes, LoremDo, consumer);
      expect(visitedNodes).toEqual([scout.create(LoremDo, {lorem: 'LOREM'})]);
    });

    it('does visit non matched objects recursively', () => {
      const visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const lorem = scout.create(LoremDo, {foo: scout.create(FooDo)});

      dataObjectVisitors.forEachRec(lorem, FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo)]);
    });

    it('does visit matched objects recursively', () => {
      const visitedFoos = [];
      const consumer = (foo: FooDo) => visitedFoos.push(foo.foo);
      const lorem = scout.create(LoremDo, {
        foo: scout.create(FooDo, {
          foo: 'FOO1',
          lorem: scout.create(LoremDo, {
            foo: scout.create(FooDo, {
              foo: 'FOO2'
            })
          })
        })
      });

      dataObjectVisitors.forEachRec(lorem, FooDo, consumer);
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);
    });
  });

  describe('forEachRecWhile', () => {

    it('visits only requested types', () => {
      let visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };
      const mixedTypes = {
        a: 1,
        b: true,
        c: 'some',
        f: scout.create(FooDo, {foo: 'FOO'}),
        l: scout.create(LoremDo, {lorem: 'LOREM'})
      };

      dataObjectVisitors.forEachRecWhile(mixedTypes, null, visitor);
      expect(visitedNodes).toEqual([mixedTypes, 1, true, 'some', scout.create(FooDo, {foo: 'FOO'}), 'FOO', scout.create(LoremDo, {lorem: 'LOREM'}), 'LOREM']);

      visitedNodes = [];

      dataObjectVisitors.forEachRecWhile(mixedTypes, FooDo, visitor);
      expect(visitedNodes).toEqual([scout.create(FooDo, {foo: 'FOO'})]);

      visitedNodes = [];
      dataObjectVisitors.forEachRecWhile(mixedTypes, LoremDo, visitor);
      expect(visitedNodes).toEqual([scout.create(LoremDo, {lorem: 'LOREM'})]);
    });

    it('does visit non matched objects recursively', () => {
      const visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };
      const lorem = scout.create(LoremDo, {foo: scout.create(FooDo)});

      dataObjectVisitors.forEachRecWhile(lorem, FooDo, visitor);
      expect(visitedNodes).toEqual([scout.create(FooDo)]);
    });

    it('visits matched objects like the visitor demands', () => {
      let visitedFoos = [];
      const lorem = scout.create(LoremDo, {
        foo: scout.create(FooDo, {
          foo: 'FOO1',
          lorem: scout.create(LoremDo, {
            foo: scout.create(FooDo, {
              foo: 'FOO2',
              lorem: scout.create(LoremDo, {
                foo: scout.create(FooDo, {
                  foo: 'FOO3'
                })
              })
            })
          })
        })
      });
      const foos = [scout.create(FooDo, {foo: 'FOO1'}), scout.create(FooDo, {foo: 'FOO2'}), scout.create(FooDo, {foo: 'FOO3'})];

      dataObjectVisitors.forEachRecWhile(
        lorem,
        FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.SKIP_SUBTREE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);

      visitedFoos = [];
      dataObjectVisitors.forEachRecWhile(
        lorem,
        FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);

      visitedFoos = [];
      dataObjectVisitors.forEachRecWhile(
        foos,
        FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.SKIP_SUBTREE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2', 'FOO3']);

      visitedFoos = [];
      dataObjectVisitors.forEachRecWhile(
        foos,
        FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);
    });
  });

  describe('forEachIf', () => {

    it('visits only requested types', () => {
      let visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const mixedTypes = {
        a: 1,
        b: true,
        c: 'some',
        f: scout.create(FooDo, {foo: 'FOO'}),
        l: scout.create(LoremDo, {lorem: 'LOREM'})
      };

      dataObjectVisitors.forEachIf(mixedTypes, null, consumer);
      expect(visitedNodes).toEqual([mixedTypes]);

      visitedNodes = [];

      dataObjectVisitors.forEachIf(mixedTypes, c => numbers.isNumber(c), consumer);
      expect(visitedNodes).toEqual([1]);

      visitedNodes = [];
      dataObjectVisitors.forEachIf(mixedTypes, c => typeof c === 'boolean', consumer);
      expect(visitedNodes).toEqual([true]);

      visitedNodes = [];
      dataObjectVisitors.forEachIf(mixedTypes, c => typeof c === 'string', consumer);
      expect(visitedNodes).toEqual(['some', 'FOO', 'LOREM']);

      visitedNodes = [];
      dataObjectVisitors.forEachIf(mixedTypes, c => c instanceof FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo, {foo: 'FOO'})]);

      visitedNodes = [];
      dataObjectVisitors.forEachIf(mixedTypes, c => c instanceof LoremDo, consumer);
      expect(visitedNodes).toEqual([scout.create(LoremDo, {lorem: 'LOREM'})]);
    });

    it('does visit non matched objects recursively', () => {
      const visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const lorem = scout.create(LoremDo, {foo: scout.create(FooDo)});

      dataObjectVisitors.forEachIf(lorem, c => c instanceof FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo)]);
    });

    it('does not visit matched objects recursively', () => {
      const visitedFoos = [];
      const consumer = (foo: FooDo) => visitedFoos.push(foo.foo);
      const lorem = scout.create(LoremDo, {
        foo: scout.create(FooDo, {
          foo: 'FOO1',
          lorem: scout.create(LoremDo, {
            foo: scout.create(FooDo, {
              foo: 'FOO2'
            })
          })
        })
      });

      dataObjectVisitors.forEachIf(lorem, c => c instanceof FooDo, consumer);
      expect(visitedFoos).toEqual(['FOO1']);
    });
  });

  describe('forEachIfRec', () => {

    it('visits only requested types', () => {
      let visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const mixedTypes = {
        a: 1,
        b: true,
        c: 'some',
        f: scout.create(FooDo, {foo: 'FOO'}),
        l: scout.create(LoremDo, {lorem: 'LOREM'})
      };

      dataObjectVisitors.forEachIfRec(mixedTypes, null, consumer);
      expect(visitedNodes).toEqual([mixedTypes, 1, true, 'some', scout.create(FooDo, {foo: 'FOO'}), 'FOO', scout.create(LoremDo, {lorem: 'LOREM'}), 'LOREM']);

      visitedNodes = [];

      dataObjectVisitors.forEachIfRec(mixedTypes, c => numbers.isNumber(c), consumer);
      expect(visitedNodes).toEqual([1]);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRec(mixedTypes, c => typeof c === 'boolean', consumer);
      expect(visitedNodes).toEqual([true]);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRec(mixedTypes, c => typeof c === 'string', consumer);
      expect(visitedNodes).toEqual(['some', 'FOO', 'LOREM']);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRec(mixedTypes, c => c instanceof FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo, {foo: 'FOO'})]);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRec(mixedTypes, c => c instanceof LoremDo, consumer);
      expect(visitedNodes).toEqual([scout.create(LoremDo, {lorem: 'LOREM'})]);
    });

    it('does visit non matched objects recursively', () => {
      const visitedNodes = [];
      const consumer = node => visitedNodes.push(node);
      const lorem = scout.create(LoremDo, {foo: scout.create(FooDo)});

      dataObjectVisitors.forEachIfRec(lorem, c => c instanceof FooDo, consumer);
      expect(visitedNodes).toEqual([scout.create(FooDo)]);
    });

    it('does visit matched objects recursively', () => {
      const visitedFoos = [];
      const consumer = (foo: FooDo) => visitedFoos.push(foo.foo);
      const lorem = scout.create(LoremDo, {
        foo: scout.create(FooDo, {
          foo: 'FOO1',
          lorem: scout.create(LoremDo, {
            foo: scout.create(FooDo, {
              foo: 'FOO2'
            })
          })
        })
      });

      dataObjectVisitors.forEachIfRec(lorem, c => c instanceof FooDo, consumer);
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);
    });
  });

  describe('forEachIfRecWhile', () => {

    it('visits only requested types', () => {
      let visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };
      const mixedTypes = {
        a: 1,
        b: true,
        c: 'some',
        f: scout.create(FooDo, {foo: 'FOO'}),
        l: scout.create(LoremDo, {lorem: 'LOREM'})
      };

      dataObjectVisitors.forEachIfRecWhile(mixedTypes, null, visitor);
      expect(visitedNodes).toEqual([mixedTypes, 1, true, 'some', scout.create(FooDo, {foo: 'FOO'}), 'FOO', scout.create(LoremDo, {lorem: 'LOREM'}), 'LOREM']);

      visitedNodes = [];

      dataObjectVisitors.forEachIfRecWhile(mixedTypes, c => numbers.isNumber(c), visitor);
      expect(visitedNodes).toEqual([1]);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRecWhile(mixedTypes, c => typeof c === 'boolean', visitor);
      expect(visitedNodes).toEqual([true]);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRecWhile(mixedTypes, c => typeof c === 'string', visitor);
      expect(visitedNodes).toEqual(['some', 'FOO', 'LOREM']);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRecWhile(mixedTypes, c => c instanceof FooDo, visitor);
      expect(visitedNodes).toEqual([scout.create(FooDo, {foo: 'FOO'})]);

      visitedNodes = [];
      dataObjectVisitors.forEachIfRecWhile(mixedTypes, c => c instanceof LoremDo, visitor);
      expect(visitedNodes).toEqual([scout.create(LoremDo, {lorem: 'LOREM'})]);
    });

    it('does visit non matched objects recursively', () => {
      const visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };
      const lorem = scout.create(LoremDo, {foo: scout.create(FooDo)});

      dataObjectVisitors.forEachIfRecWhile(lorem, c => c instanceof FooDo, visitor);
      expect(visitedNodes).toEqual([scout.create(FooDo)]);
    });

    it('visits matched objects like the visitor demands', () => {
      let visitedFoos = [];
      const lorem = scout.create(LoremDo, {
        foo: scout.create(FooDo, {
          foo: 'FOO1',
          lorem: scout.create(LoremDo, {
            foo: scout.create(FooDo, {
              foo: 'FOO2',
              lorem: scout.create(LoremDo, {
                foo: scout.create(FooDo, {
                  foo: 'FOO3'
                })
              })
            })
          })
        })
      });
      const foos = [scout.create(FooDo, {foo: 'FOO1'}), scout.create(FooDo, {foo: 'FOO2'}), scout.create(FooDo, {foo: 'FOO3'})];

      dataObjectVisitors.forEachIfRecWhile(
        lorem,
        c => c instanceof FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.SKIP_SUBTREE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);

      visitedFoos = [];
      dataObjectVisitors.forEachIfRecWhile(
        lorem,
        c => c instanceof FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);

      visitedFoos = [];
      dataObjectVisitors.forEachIfRecWhile(
        foos,
        c => c instanceof FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.SKIP_SUBTREE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2', 'FOO3']);

      visitedFoos = [];
      dataObjectVisitors.forEachIfRecWhile(
        foos,
        c => c instanceof FooDo,
        foo => {
          visitedFoos.push(foo.foo);
          return foo.foo === 'FOO2' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedFoos).toEqual(['FOO1', 'FOO2']);
    });
  });

  describe('DataObjectVisitHelper', () => {

    it('does not visit null or undefined', () => {
      let visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };

      const helper = scout.create(DataObjectVisitHelper);

      helper.visit(null, visitor);
      expect(visitedNodes).toEqual([]);
      helper.visit(undefined, visitor);
      expect(visitedNodes).toEqual([]);

      helper.visit(0, visitor);
      expect(visitedNodes).toEqual([0]);

      visitedNodes = [];
      helper.visit(false, visitor);
      expect(visitedNodes).toEqual([false]);

      visitedNodes = [];
      helper.visit('', visitor);
      expect(visitedNodes).toEqual(['']);
    });

    it('visits primitives', () => {
      let visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };

      const helper = scout.create(DataObjectVisitHelper);

      helper.visit(42, visitor);
      expect(visitedNodes).toEqual([42]);

      visitedNodes = [];
      helper.visit(true, visitor);
      expect(visitedNodes).toEqual([true]);

      visitedNodes = [];
      helper.visit('foo', visitor);
      expect(visitedNodes).toEqual(['foo']);
    });

    it('visits a Set and all of its elements', () => {
      const visitedNodes = new Set();
      const helper = scout.create(DataObjectVisitHelper);
      const set = new Set([7, 13, 42]);

      helper.visit(
        set,
        node => {
          visitedNodes.add(node);
          return TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual(new Set([set, 7, 13, 42]));

      visitedNodes.clear();
      helper.visit(
        set,
        node => {
          visitedNodes.add(node);
          return TreeVisitResult.SKIP_SUBTREE;
        }
      );
      expect(visitedNodes).toEqual(new Set([set]));

      visitedNodes.clear();
      helper.visit(
        set,
        node => {
          visitedNodes.add(node);
          return node === 13 ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual(new Set([set, 7, 13]));
    });

    it('visits an array and all of its elements', () => {
      let visitedNodes = [];
      const helper = scout.create(DataObjectVisitHelper);
      const array = [7, 13, 42];

      helper.visit(
        array,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([array, 7, 13, 42]);

      visitedNodes = [];
      helper.visit(
        array,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.SKIP_SUBTREE;
        }
      );
      expect(visitedNodes).toEqual([array]);

      visitedNodes = [];
      helper.visit(
        array,
        node => {
          visitedNodes.push(node);
          return node === 13 ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([array, 7, 13]);
    });

    it('visits a Map and all of its elements', () => {
      let visitedNodes = [];
      const helper = scout.create(DataObjectVisitHelper);
      const map = new Map([
        ['foo', 7],
        ['bar', 13]
      ]);

      helper.visit(
        map,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([map, 'foo', 7, 'bar', 13]);

      visitedNodes = [];
      helper.visit(
        map,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.SKIP_SUBTREE;
        }
      );
      expect(visitedNodes).toEqual([map]);

      visitedNodes = [];
      helper.visit(
        map,
        node => {
          visitedNodes.push(node);
          return node === 7 ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([map, 'foo', 7]);

      visitedNodes = [];
      helper.visit(
        map,
        node => {
          visitedNodes.push(node);
          return node === 'bar' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([map, 'foo', 7, 'bar']);
    });

    it('visits a BaseDoEntity and all of its elements', () => {
      let visitedNodes = [];
      const helper = scout.create(DataObjectVisitHelper);
      const foo = scout.create(FooDo, {foo: 'FOO', bar: 42});

      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO', 42]);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.SKIP_SUBTREE;
        }
      );
      expect(visitedNodes).toEqual([foo]);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return node === 'FOO' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO']);

      const lorem = scout.create(LoremDo, {lorem: 'LOREM', ipsum: 13, dolor: true});
      foo.addContribution(lorem);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO', 42, lorem, 'LOREM', 13, true]);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return node === lorem ? TreeVisitResult.SKIP_SUBTREE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO', 42, lorem]);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return node === 13 ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO', 42, lorem, 'LOREM', 13]);
    });

    it('visits a pojo and all of its elements', () => {
      let visitedNodes = [];
      const helper = scout.create(DataObjectVisitHelper);
      const foo = {foo: 'FOO', bar: 42};

      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO', 42]);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return TreeVisitResult.SKIP_SUBTREE;
        }
      );
      expect(visitedNodes).toEqual([foo]);

      visitedNodes = [];
      helper.visit(
        foo,
        node => {
          visitedNodes.push(node);
          return node === 'FOO' ? TreeVisitResult.TERMINATE : TreeVisitResult.CONTINUE;
        }
      );
      expect(visitedNodes).toEqual([foo, 'FOO']);
    });

    it('keeps track of the parents while visiting', () => {
      const visitedContextParents = [];
      const visitor = (node, context) => {
        visitedContextParents.push(context.parents);
        return TreeVisitResult.CONTINUE;
      };

      const helper = scout.create(DataObjectVisitHelper);

      const foo = scout.create(FooDo, {foo: 'FOO'});
      const lorem = scout.create(LoremDo, {lorem: 'LOREM', foo});
      const set = new Set([true, lorem]);
      const map = new Map<string, BaseDoEntity>([
        ['foo', foo],
        ['lorem', lorem]
      ]);
      const object = {
        n: 42,
        set,
        map
      };

      helper.visit(object, visitor);
      expect(visitedContextParents).toEqual([
        [], // <- object
        [object],
        [object], // <- set
        [object, set],
        [object, set], // <- lorem
        [object, set, lorem],
        [object, set, lorem], // <- foo
        [object, set, lorem, foo],
        [object], // <- map
        [object, map],
        [object, map], // <- foo
        [object, map, foo],
        [object, map],
        [object, map], // <- lorem
        [object, map, lorem],
        [object, map, lorem], // <- foo
        [object, map, lorem, foo]
      ]);
    });

    it('detects reference cycles', () => {
      const visitedNodes = [];
      const visitor = node => {
        visitedNodes.push(node);
        return TreeVisitResult.CONTINUE;
      };

      const helper = scout.create(DataObjectVisitHelper);

      const foo = scout.create(FooDo, {foo: 'FOO'});
      const lorem = scout.create(LoremDo, {lorem: 'LOREM', foo});
      foo.lorem = lorem;

      expect(() => helper.visit(foo, visitor)).toThrowError('Unable to visit node. Reference cycle detected.');

      expect(visitedNodes).toEqual([foo, 'FOO', lorem, 'LOREM']);
    });
  });

  @typeName('scout.Foo')
  class FooDo extends BaseDoEntity {
    foo: string;
    bar: number;
    lorem: LoremDo;
  }

  @typeName('scout.Lorem')
  class LoremDo extends BaseDoEntity {
    lorem: string;
    ipsum: number;
    dolor: boolean;
    foo: FooDo;
  }
});
