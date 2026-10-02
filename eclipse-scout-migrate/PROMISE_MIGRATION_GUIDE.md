# Migrate a Scout JS project from jQuery Promise/Deferred to native Promise

**Context:** This project builds on `@eclipse-scout/core` (Scout JS), which has already completed this same migration internally — its `Deferred` class, `ajax.*` helpers, `Call`/`AjaxCall`, `Widget.when()`, etc. are all native-Promise-based now. Your job is to bring this project's *own* code in line: eliminate `JQuery.Promise`/`JQuery.Deferred` types, `$.Deferred()`, and `.done()/.fail()/.always()` usage, replacing them with native `Promise`/`Deferred` (imported from `@eclipse-scout/core`) and `.then()/.catch()/.finally()`.

You cannot assume you can build or run tests in this environment — verify everything by reading code and cross-checking against the already-migrated base classes in `@eclipse-scout/core`, and report your changes clearly so a human can run the real test suite afterward.

## 1. Survey properly before estimating scope

Grep for **all** of these patterns, not just type annotations — a file can use `.done()/.fail()/.always()` on a promise with no `JQuery.Promise` type anywhere in sight, and a narrower grep will silently miss it (this happened during the reference migration: an initial pass using only `JQuery\.(Promise|Deferred)|\$\.Deferred\(` missed several real files with raw `.done()/.fail()`):

```
JQuery\.(Promise|Deferred)|JQueryPromise|JQueryDeferred|\$\.Deferred\(|\$\.when\(|\$\.promiseAll\(|\.done\(|\.fail\(|\.always\(|\.state\(\)|JQuery\.jqXHR
```

Even this pattern is not enough on its own. It also misses these, which were found in later migrations:

- **Legacy global types `JQueryPromise<T>` / `JQueryDeferred<T>`** (without the `JQuery.` namespace). They are easy to overlook because they don't match `JQuery\.Promise`.
- **`$.when(...)`** — see §4a.
- **`$.promiseAll(...)` without `asArray`** — its resolve value changed shape, see §4.
- **Callbacks with several parameters or rest parameters** on `.then/.catch`, e.g. `.then((a, b) => ...)`, `.catch((...args) => ...)`, `.fail((error, ...args) => ...)`. Find them with:
  ```
  \.(then|catch|finally)\(\s*(function\s*)?\(\s*(\.\.\.|[a-zA-Z_$][\w$]*\s*(:\s*[^,()]+)?\s*,)
  ```

Run this across every module before giving a file/effort estimate. Fix foundational/shared modules first (whatever other modules in this project depend on), then work outward.

### Use the TypeScript compiler as your main survey and verification tool

A plain `tsc --noEmit` run against the already-migrated `@eclipse-scout/core` sources is the most reliable way to find what still needs migrating. It is far more precise than grep. Every call site that still expects a jQuery promise shows
up as an error, for example:

- `TS2740: Type 'Promise<X>' is missing the following properties from type 'Promise<X, any, any>': always, done, fail, progress...`
- `TS2739: ... missing ... finally, [Symbol.toStringTag]`
- `TS2416: Property '_load' ... is not assignable to the same property in base type`

Workflow:

1. Save a baseline error list per module *before* changing anything.
2. Diff against it after each module; the goal is zero promise-related errors and no new errors.
3. Also type-check downstream modules. Errors often surface in a module that consumes an unmigrated API rather than in the module that defines it.

If `node_modules` symlinks are broken in your environment (e.g. they point to a Windows path that isn't mounted), don't give up on type-checking. Generate a throwaway tsconfig outside the repo that:

- extends the project's base tsconfig,
- maps every workspace package with `compilerOptions.paths` (`"@eclipse-scout/core": [".../eclipse-scout-core/src/index.ts"]`, `"<pkg>/testing"`, `"<pkg>/src/*"`, plus `jquery` → `@types/jquery`),
- sets `typeRoots`/`types` for jquery and jasmine.

Errors for missing third-party modules (TS2307) are expected in that setup and can be ignored.

The compiler cannot see everything. Callbacks typed `any` or annotated with the wrong shape (e.g. `.then((result: ResultDo) => result)` on a `Promise<any>`) type-check fine but are wrong at runtime. So always combine tsc with the grep
patterns above.

## 2. Mechanical vs. manual changes — don't treat them the same

- **Type-only** (`JQuery.Promise<T>` → `Promise<T>`) is safe to batch with `sed` across a whole file, **except** the 2-argument jQuery form `JQuery.Promise<TResolve, TReject>` — drop the second type param entirely (native `Promise<T>` has no typed-rejection equivalent); handle those by hand.
- **Any real `$.Deferred()`, `.done()`, `.fail()`, `.always()` usage needs individual, careful review** — see the translation rules below. Do not regex-replace these.
- For every `protected override` method whose return type you're touching, verify the **exact** signature against the base class in `@eclipse-scout/core` (e.g. `Form._load()`, `Form._save()`, `PageWithTable._loadTableData()`,
  `ValueField._validateValue()`, `BasicField.acceptInput()`, `App._init()`/`_load()`/`_defaultBootstrappers()`, `UiCallbackHandler.handle()`) — TS allows a narrower override, but confirm it, don't assume.
- **Not everything named `JQuery.Promise` should be migrated.** jQuery *animation* promises (`$elem.animate(...).promise()`, e.g. `TileGridLayout._animateTiles(): JQuery.Promise<JQuery>[]`) remain jQuery promises in the base framework;
  overrides must keep that type. Also leave vendored third-party code alone (e.g. a bundled editor library with its own `.done()/.fail()` API).
- **Type errors can expose pre-existing bugs.** Example: `_installEditor(): Promise<Editor>` whose `.then(editor => { this.editor = editor; })` never returned the value. jQuery's typings didn't complain; native `Promise<void>` vs
  `Promise<Editor>` does. Fix the implementation (`return editor;`), not the declared type, after checking which consumers rely on the value.
- **Loosen parameter types that accept promises from not-yet-migrated callers.** For example, change a loader parameter typed `() => JQuery.jqXHR` to `() => PromiseLike<T>`, and wrap its result with `Promise.resolve(loader())`. That way
  downstream repositories that still pass a jqXHR keep compiling and working.
- **Collapse jQuery bridge methods.** A common pre-migration pattern was a public method returning a jQuery promise that only wrapped a protected `async` implementation:
  `activate(): JQuery.Promise<void> { return $.when(this._activateAsync()); }`. Now the
  public method can simply be `async` itself; drop the protected `…Async` variant and move its overrides to the public method. Scout did this for `BookmarkSupport` and `ChartTableControlConfigHelper` (see below).

### Scout APIs whose shape changed

Besides `JQuery.Promise<T>` → `Promise<T>`, these `@eclipse-scout/core` APIs changed in a way that project overrides and callers must follow:

| API                               | Before                                                                                                                          | Now                                                                                                                                                                                                                        |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Call` (base of `AjaxCall`)       | `_setResultDone(...args)`, `_setResultFail(...args)`, `_onCallDone(...args)`, `_onCallFail(...args)`, `_nextRetryImpl(...args)` | `_setResultDone`/`_setResultFail` removed (the result is set by `_call()`); `_onCallDone(result)`, `_onCallFail(error)`, `_nextRetryImpl(error)` receive a single value                                                    |
| `AjaxCall`                        | rejected with `(jqXHR, textStatus, errorThrown)`; `pendingCall` was the jqXHR                                                   | rejected with a single `AjaxError` (created in `_callImpl()`); `pendingCall` is a native promise; the jqXHR of the current or most recent request is available as `xhr` (e.g. for the HTTP status of a successful request) |
| `AjaxCall.isOfflineError()`       | `isOfflineError(jqXHR, textStatus, errorThrown)`                                                                                | `isOfflineError(error: AjaxError)`                                                                                                                                                                                         |
| `Session._processErrorResponse()` | `(jqXHR, textStatus, errorThrown, request)`                                                                                     | `(error: AjaxError, request)`                                                                                                                                                                                              |
| `BookmarkSupport`                 | `activateBookmark()`, `activateBookmarkPath()`, `applyBookmarkToPageAndReload()` wrapping protected `_…Async()` methods         | the public methods are `async`, the `_…Async()` methods are removed: override the public methods                                                                                                                           |
| `ChartTableControlConfigHelper`   | `exportConfig()`/`importConfig()` wrapping protected `_exportConfig()`/`_importConfig()`                                        | `exportConfig()`/`importConfig()` are `async`, the protected variants are removed                                                                                                                                          |
| `Page`                            | –                                                                                                                               | new `when(type)`, like `Widget.when()`                                                                                                                                                                                     |

## 3. `.done()/.fail()/.always()` vs. `.then()/.catch()/.finally()` — the semantic differences

These are not interchangeable one-for-one. Know the actual differences before translating any call site:

**a) `.done()/.fail()` do not chain return values; `.then()` does.** Each `.done()` in a chain receives the *original* resolved value, not the previous `.done()`'s return value:
```js
// jQuery — every .done() gets the SAME original value:
promise.done(x => { console.log(x); return 123; })
       .done(x => console.log(x))
       .done(x => console.log(x));
// -> abc, abc, abc  (assuming promise resolves with 'abc')

// native — each .then() gets the PREVIOUS .then()'s return value:
promise.then(x => { console.log(x); return 123; })
       .then(x => console.log(x))
       .then(x => console.log(x));
// -> abc, 123, undefined
```
When migrating a `.done()` chain that relied on every callback seeing the same original value, either make each `.then()` explicitly `return x` to pass it along, or restructure so only the first callback needs the value.

**b) All `.fail()` handlers run; only the first `.catch()` does.** Chained `.fail()`s are independent registrations on the same deferred and all fire. A `.catch()`, if it doesn't rethrow, turns the chain back into a *resolved* one, so a second chained `.catch()` never sees anything to catch:
```js
$.Deferred().reject('abc')
  .fail(e => console.log('fail', e))
  .fail(e => console.log('fail2', e))
  .always(x => console.log('always', x));
// -> fail abc, fail2 abc, always abc

await Promise.reject('abc')
  .catch(e => console.log('catch', e))
  .catch(e => console.log('catch2', e))
  .finally(x => console.log('finally', x));
// -> catch abc, finally   (catch2 never runs)
```
If code depends on multiple independent failure handlers all seeing the rejection, don't chain `.catch()`s — attach them all directly to the *original* promise instead (`promise.catch(a); promise.catch(b);`), or consolidate into one handler.

**c) `.finally()` receives no arguments; `.always()` receives the resolved/rejected value.** This is easy to miss because both look like "run regardless of outcome" callbacks. Any `.always(fn)` where `fn` actually reads its argument needs that logic moved into `.then()`/`.catch()` — it cannot become `.finally(fn)` unchanged. Watch especially for a *bound* function passed to `.always()`, where the promise's value becomes an extra trailing argument:
```js
// original:
returnValue.always(this._resizeToFit.bind(this, column, maxWidth));
// _resizeToFit(column, maxWidth, calculatedSize) -- calculatedSize comes from .always()'s value.

// WRONG migration — calculatedSize would always be undefined:
returnValue.finally(this._resizeToFit.bind(this, column, maxWidth));

// correct: move the value-dependent logic into then()/catch(), or capture it explicitly:
returnValue.then(
  calculatedSize => this._resizeToFit(column, maxWidth, calculatedSize),
  calculatedSize => this._resizeToFit(column, maxWidth, calculatedSize)
);
```
So the rule for `.always(fn)` → `.finally(fn)` (from earlier migration work in this codebase) needs this extra condition: safe only if the result is returned/chained onward, `fn` never throws, **and `fn` doesn't need the resolved/rejected value**.

A bound function can also receive the value *by accident*. In `promise.always(this._postProcess.bind(this))`, where `_postProcess(zoomFactor?: number)` falls back to a default for a missing argument, the old code passed `undefined` on
success but the rejection *string* on failure as `zoomFactor`. Translate this to what was actually intended, e.g. `.finally(() => this._postProcess(null))`, not to a literal equivalent.

**c2) A detached `.always()`/`.fail()` branch becomes an unhandled rejection.** Pattern:

```js
promise.always(() => this.setBusy(false));   // separate statement, result discarded
return promise.then(...);
```

Under jQuery the discarded branch was harmless. Natively, `promise.finally(...)` returns a *new* promise that rejects whenever `promise` rejects. Nobody observes that new promise, so it surfaces as an unhandled rejection even though the
caller handles the main chain. Fold the cleanup into the chain that is actually returned, stored or awaited (`return promise.finally(() => this.setBusy(false)).then(...)`) instead of translating the statement one-to-one.

**c3) `.then(onDone).catch(onFail)` is not the same as `.done(onDone).fail(onFail)`.** The chained `.catch()` also catches errors thrown by `onDone`, e.g. a bug in the success handler then gets reported as "request failed". If the
failure handler is meant for the original promise only, use the two-argument form:

```ts
// catches errors of the request AND of _onPostDone:
ajax.call(options).then(data => this._onPostDone(data)).catch(error => this._onPostFail(error));
// catches errors of the request only (like .done()/.fail()); an error in _onPostDone stays unhandled and is reported:
ajax.call(options).then(data => this._onPostDone(data), error => this._onPostFail(error));
```

Use the chained form only if the handler should really cover both.

**d) A `.done()` callback throwing does not invoke `.fail()`/`.always()` — it's just an uncaught exception.** A `.then()` callback throwing correctly invokes the chain's `.catch()`. If migrated code seems to have swallowed errors that used to escape loudly (or vice versa), check whether the original relied on this asymmetry.

## 4. Multi-argument resolve/reject has no native equivalent

jQuery's `deferred.resolve(a, b, c)` supports multiple arguments; native `Promise` resolves with exactly one value. This affects several common patterns:

- `deferred.resolve(a, b, c)` → must resolve with a single value (wrap in an array/object), and every `.done((a, b, c) => ...)` consumer of that specific deferred needs updating to destructure it instead.
- `$.when(p1, p2, p3).done((a, b, c) => ...)` → `Promise.all([p1, p2, p3]).then(([a, b, c]) => ...)` — arguments move from separate parameters to a destructured array.
- The same applies to any `$.promiseAll()`-style helper — note the *arity* change, not just the function name:
  ```js
  // old:
  $.promiseAll([...]).then((...statusArr) => { ... });
  // new — statusArr is already the array, no rest-spread:
  $.promiseAll([...]).then(statusArr => { ... });
  ```
- **The dangerous `$.promiseAll` case is the one that still compiles:** a callback that only wanted the *first* value, e.g. `.then((result: ResultDo) => result)`. Under jQuery that received the first resolved value. Now it silently
  receives (and returns) the whole array. Write `.then(([result]) => result)`. Only calls **without** `asArray` changed; `$.promiseAll(promises, true)` always resolved with an array and needs no change.
- Ajax success handlers used to receive `(data, textStatus, jqXHR)` as separate arguments — the native-Promise-based `ajax.*` helpers only ever resolve with the response body. If you need the status/jqXHR/error details, check whether the framework wraps them in something like `AjaxError` on the rejection path — use that instead of expecting extra success-handler arguments.
  - Watch for indexed access that compensated for this. When a jqXHR went through `$.when`/`$.promiseAll`, its three resolve values were collapsed into an array, so code read `response[0].items` (typed e.g. `[ResponseDo, any, any]`).
    That must now be `response.items`.
- **Rejections are single-valued too.** Replace `.fail((error, ...args) => this._handleError(error, ...args))` / `.catch((...args) => errorHandler.handle(...args))` with `.catch(error => ...)`. The ajax helpers reject with a single
  `AjaxError` (with `jqXHR`, `textStatus`, `errorThrown`, `requestOptions`). If a **public** callback receives the argument list (e.g. `onError(args: any[])`), keep its signature stable and pass `[error]`, and check the downstream
  implementations. Don't silently change a public API's argument shape.

### 4a. `$.when(...)`

- `$.when(this._asyncMethod())`, typically used to turn an `async` function's native Promise into a jQuery promise for an override: drop the wrapper and return the promise directly.
- `$.when(...promises)` used as "wait for all": use `Promise.all(promises).then(() => undefined)` when the result is `Promise<void>`, or destructure the array if the values are used (§4).
- `$.when(value)` with a non-promise value: `Promise.resolve(value)` / `$.resolvedPromise(value)`.

## 5. `promise.state()` / `deferred.state()` don't exist on native Promise

There is no synchronous way to query whether a native `Promise` is pending/resolved/rejected. If code relies on `.state()`, check whether the framework's own `Deferred` class (`promises.ts` in `@eclipse-scout/core`) exposes an equivalent wrapper (`state(): 'pending' | 'resolved' | 'rejected'`) — use that instead of a raw native Promise if you need to introspect state, or track the state yourself alongside the promise.

When the promise comes from somewhere else (e.g. an ajax call), track the state explicitly. Guard the update with an identity check (§8) so a superseded promise can't overwrite the state of the current one:

```ts
protected
_trackState(promise
:
Promise<void>
)
{
  this._promiseState = 'pending';
  promise.then(
    () => this._updateState(promise, 'resolved'),
    () => this._updateState(promise, 'rejected')); // error is reported by the main chain
}

protected
_updateState(promise
:
Promise<void>, state
:
'resolved' | 'rejected'
)
{
  if (this._currentPromise === promise) {
    this._promiseState = state;
  }
}
```

Keep in mind that the tracked state flips one microtask *after* settlement, while jQuery flipped it synchronously. Code that checks the state immediately after resolving (in the same tick) sees `'pending'`.

In specs, don't track the state yourself. Use Jasmine's async matchers: `expect(promise.state()).toBe('pending')` becomes `await expectAsync(promise).toBePending()`, and `toBe('resolved')` / `toBe('rejected')` become
`await expectAsync(promise).toBeResolved()` / `toBeRejected()`. Note that `toBeResolved()` waits for the promise, so it also replaces the wait that used to precede the state check.

**Framework `Deferred` is not a thenable.** A jQuery Deferred *was* a promise, so code could return or await the deferred itself. The core `Deferred` is a separate holder object. Always hand out `deferred.promise()`, e.g.
`spyOn(x, 'load').and.callFake(() => deferred.promise())` and `await deferred.promise()`. Awaiting the `Deferred` object itself resolves immediately with the object and doesn't wait for anything. Give it a precise type parameter (
`new Deferred<string>()`), otherwise `resolve()` accepts anything and the returned promise is `Promise<unknown>`.

Native typings are stricter in other places too. For example, `new Deferred<ArrayBuffer>()` resolved with `fileReader.result` needs a cast (`fileReader.result as ArrayBuffer`), because `result` is typed `string | ArrayBuffer`.

## 6. The trap that costs the most time: fake timers and microtasks

**First, the foundational fact — `jasmine.clock().tick()` never drains microtasks, only fake timers:**
```js
jasmine.clock().install();

// jQuery: $.resolvedPromise()'s .then() is scheduled via setTimeout, so it IS a fake timer:
$.resolvedPromise('abc').then(x => console.log(x));
jasmine.clock().tick(0);
console.log('after');
// -> 'abc', 'after'

// native: Promise.resolve()'s .then() is a microtask, tick() cannot touch it:
Promise.resolve('abc').then(x => console.log(x));
jasmine.clock().tick(0);
console.log('after');
// -> 'after', 'abc'   (the native .then() only fires once the current synchronous code
//                      finishes and the microtask queue is drained — e.g. after an `await`)
```
`jasmine.clock()` genuinely cannot be used to drive native-Promise-based code. Prefer awaiting something concrete (a returned promise, `widget.when('propertyChange:xyz')`) or `await sleep(...)`.

**Second, the subtler version of the same problem — jQuery's own `.then()` becomes a fake timer too:**
- `.done()/.fail()/.always()` fire **synchronously**, even on an already-settled deferred — this has never changed.
- `.then()/.catch()` on a jQuery Deferred (jQuery ≥ 3, Promises/A+ compliance) are **always deferred via `window.setTimeout`** — a real macrotask, not a microtask. (Verify in `node_modules/jquery/dist/jquery.js` if in doubt — search for `window.setTimeout( process )` inside `Deferred.then`.)

Consequence: once `jasmine.clock().install()` is active, any code that still calls `.then()/.catch()` on a raw jQuery Deferred/jqXHR has that reaction turned into a **fake timer**, invisible to a microtask flush like
`await Promise.resolve()` (which only drains microtasks). A single ajax response's full processing can require alternating fake-timer hops (jQuery's internal `.then()`) and native-microtask hops (the rest of your Promise chain), sometimes
several rounds deep if success also schedules a *new* timer (e.g. a poller re-arming itself). Manually guessing combinations of N microtask flushes and `jasmine.clock().tick(M)` is fragile and was the source of most of the flakiness in the
reference migration.

**Fix: use `jasmine.clock().autoTick()`** (jasmine-core ≥ 5.7) instead of manual `tick()`/microtask-flush pairs. Only install the fake clock in the specs that really need fake time (e.g. a debounce or retry delay), not in a `beforeEach` for
a whole file. Most specs don't need it at all once they
await the right promise or event. Call `autoTick()` once, right after `install()`:
```ts
beforeEach(() => {
  jasmine.clock().install();
  jasmine.clock().autoTick();
});
afterEach(() => {
  jasmine.clock().uninstall(); // also cleanly stops the auto-tick loop
});
```

It continuously fires the next due fake timer and yields to a real macrotask in between (letting any native-Promise chain — and anything it schedules — run to completion) automatically, without you needing to know the hop count. It still
fast-forwards long fake delays (e.g. a 30s retry interval) without the test actually waiting 30 real seconds. Combine it with real `await sleep(N)` (a small real wait, e.g. 50ms) at points where you just need to let things settle with no
specific event to await, and with precise signals (`widget.when('propertyChange:xyz')`, `desktop.when('propertyChange:messageBoxes')`, `form.whenClose()`/`whenLoad()`, a returned promise) wherever the code exposes one — always prefer a
precise signal over a guessed wait when one is available.

When wrapping a raw `$.ajax()`/jqXHR directly, prefer this project's/framework's own `ajax.getJson()`/`ajax.postJson()`/`AjaxCall` (native-Promise, already migrated) over hand-rolling a new wrapper. If you must wrap a raw jqXHR yourself, use the **single two-argument** `jqXHR.then(onFulfilled, onRejected)`, not `jqXHR.then(onFulfilled).catch(onRejected)`. The chained form costs the rejection path *two* jQuery-internal scheduling hops instead of one — a real asymmetry that can flip the relative order in which two competing async operations (e.g. an aborted call vs. a fresh one) settle, compared to what the pre-migration code guaranteed.

**Third: timer steps that are still there, and why specs now overtake them.** Even the migrated framework still defers some work via `setTimeout`:

- `AjaxCall` wraps the jqXHR with jQuery's `.then()`, so every ajax response costs one timer step (this includes every `RestLookupCall`);
- `StaticLookupCall` resolves its results in a `setTimeout`;
- `BatchCall` collects calls and executes the batch in a `setTimeout`.

Before the migration, the *spec's own* chain (`widget.whenReady().then(...).then(...)`) was jQuery-based too. Each step was a `setTimeout` queued *behind* those timers, so a lookup started during the action had always finished by the time
the assertions ran. Natively the spec's chain continues as microtasks and **overtakes** them. So the assertion sees the state from *before* the lookup finished. Typical symptoms:

- labels missing the part that comes from a lookup ('Company' instead of 'Company: Commercial');
- empty display texts of smart fields;
- values or counts that are exactly one step behind (`Expected 1 to be 2`);
- a different widget type than expected, because a mode switch that the lookup triggers hasn't happened yet.

Production code is usually fine here, since it always worked asynchronously. Fix the spec by awaiting the property the deferred work writes (see §9 for how).

Similar hidden deferrals in core widgets:

- `Table.startCellEdit()` defers opening the cell editor until the table's `updateBuffer` completes, if it is buffering. After `focusCell()` the editor field may already exist in `table.children` while the popup isn't open and
  `startCellEdit` hasn't fired. Wait with `table.when('startCellEdit')`, registered *before* `focusCell()`.
- Widgets that expose a busy counter or a `whenReady()` method give an exact "the async processing is done" signal, e.g. a widget that calls `setBusy(true)` synchronously when it starts processing a change and `setBusy(false)` in the
  chain's `.finally()`, with `whenReady()` resolving once the counter is back to 0. After an action that changes such a widget (e.g. closing a sub-form that writes a value into it), add `.then(() => widget.whenReady())` before asserting.

**Audit your own test helpers for the same disease.** Any shared test utility that "flushes" queued ajax calls or their responses using `jasmine.clock().tick()` alone (implicitly assuming synchronous jQuery-Deferred resolution — e.g. a
`sendQueuedAjaxCalls()`-style helper) will likely stop working once the production code it drives switches from `.done()` to `.then()`. `tick()` cannot flush the resulting native-Promise chain. Write an `async` equivalent that awaits a real
completion signal instead (e.g. the framework's `session.whenRequestsDone()`), convert every caller from the sync helper to the async one, and mark those tests `async`. Scout's own
spec helpers now offer `sendQueuedAjaxCallsAsync(session)` for this.

## 7. The other big class of bug: fire-and-forget promises and unhandled rejections

Native Promises trigger a global "unhandled rejection" error when nothing ever attaches a `.catch()`/`.then(_, onRejected)`/`.finally()`-with-rethrow to them — Karma/Jasmine treats this as a failing test. jQuery has no such mechanism at all, so every place a Promise-returning call's result was neither returned, stored, nor awaited was **silently swallowed under jQuery** and is now a genuine, loud failure. This is not a new bug you're introducing — it's a pre-existing gap the migration makes visible. Concrete places this shows up:

- **Expected cancellations don't need local handling.** Aborting an in-flight lookup call rejects with `{abort: true}`, and widgets cancel chains with `AbortError`. Both are ignored by the global handler (§7a), so don't add local `.catch()`
  filters for them.
- **Stray timers firing after a test (or the object) is gone.** A `setTimeout` scheduled by, say, a debounced "send" method can fire after the test ends and hit a partially torn-down mock, producing an unhandled rejection in an unrelated later test's `afterAll`. Any `destroy()`-style method should explicitly `clearTimeout` whatever it scheduled (check the base framework's own `Session.destroy()`/`HybridManager.destroy()`-equivalents for the pattern, and make sure your test teardown actually calls `destroy()`/cancels those timers between tests).
- **A leftover ajax call executing after the test ends.** Make sure `jasmine.Ajax.install()`/`uninstall()` bracket every test that can trigger a request, so a request that fires later doesn't escape as a real network call.
- **A spec that doesn't return (or await) its promise chain.** E.g. `.then(() => { ...; SpecUtil.checkReimport(widget, expected); })` without `return`. Under jQuery the chain ran on and nobody noticed. Natively the spec finishes
  first, and `afterEach` tears down state the rest of the chain still needs (permissions, mocks, the session). The chain then fails with a *misleading* error, reported as an unhandled rejection "in afterAll" of an unrelated spec. In the
  reference migration an `assertValue()` failed with `Missing value`, because the permission that made a UI element available had already been uninstalled by the spec's `afterEach`. When an error surfaces "in afterAll", first look for an
  unreturned chain in the specs that ran just before.
- **Timers that used to die with the fake clock.** Pending fake timers are discarded by `jasmine.clock().uninstall()`. Specs that used to run under the clock therefore never executed deferred work, such as a `BatchCall` (batched cell-text
  lookups), a debounced update, or a `StaticLookupCall`. With the clock gone, that work now runs for real, possibly after the rows/widgets it refers to are gone (e.g. `TypeError: Cannot read properties of undefined` inside a batch
  callback). If the same can happen in production (rows replaced before the batch fires), make the callback tolerate it (e.g. `cellByValue.get(value)?.config`); otherwise destroy/cancel the owner in the spec's cleanup.
- **For genuinely expected rejections, write `expectAsync(promise).toBeRejected()`** rather than letting the rejection go unhandled or wrapping it in a try/catch that swallows the assertion value. But note the timing trap: **the call itself is what attaches the handler**; the `await` of its result can happen later. If you call `expectAsync(...)` only at the end of a test, after the promise already rejected earlier in the test body, Karma has already flagged it as unhandled by the time you get there:
  ```ts
  let promise = doSomethingThatWillReject();
  let rejected = expectAsync(promise).toBeRejected(); // attach now
  // ... rest of the test, including whatever triggers the rejection ...
  await rejected; // await later
  ```

**Don't reach for a bare `.catch(() => {})` / `.then(fn, () => {})` as the default fix for a fire-and-forget call site (typical for UI event handlers where nobody awaits the result).** That was jQuery's actual behavior — silently dropping the error — and reproducing it mechanically throws away exactly the diagnostic value native Promise gives you over jQuery. Before adding a no-op guard, decide which of these two cases you're in:

- **The error is already reported somewhere in the call chain** (e.g. an inner call already goes through the framework's `ErrorHandler`, or an event that already gets to a caller that handles it, before rethrowing/settling as rejected). Here an empty guard purely to prevent an unhandled-rejection failure is fine — add a one-line comment saying so, so the next reader doesn't mistake it for a silent swallow.
- **There is genuinely no other reporting path.** Don't paper over this with a local `.catch(error => someErrorHandler.handle(error))` at every call site either. That just trades one form of clutter (silent swallow) for another (
  per-call-site error-handling boilerplate). Scout has a global unhandled-rejection handler analogous to its synchronous one (see §7a), so the correct migration is usually to **do nothing**: leave the rejection unhandled and let it surface.
  That's the notification, and it's exactly what the global handler is meant to catch. Ask the project owner before inventing a per-site reporting convention.

### 7a. Expected cancellations (`AbortError`, `{abort: true}`) are ignored globally

Much Scout code rejects *on purpose* to cancel the remainder of a chain:

- `throw new AbortError()` when a widget was destroyed in the meantime (e.g. a `cancelIfDestroyed(widget)` utility called at the start of each `.then()` step), or error helpers that pass an `AbortError` on as a rejection to skip later
  `.then()`s;
- `LookupCall.abort()` implementations reject with `{abort: true}` (`RestLookupCall`, `StaticLookupCall`, …).

jQuery silently dropped these at the end of every chain. Natively each one becomes an unhandled rejection, which in a real test run means hundreds of `Unhandled promise rejection: [object Object] thrown ... in afterAll` errors. **Don't add
a per-site abort filter to every chain end.** The framework handles this centrally:

- `ErrorHandler.isIgnorableRejection(reason)` returns `true` for `AbortError` and `{abort: true}`.
- `App._installErrorHandler()` registers `errorHandler.unhandledRejectionHandler` for `unhandledrejection`, and it calls `preventDefault()` for ignorable rejections.
- In Karma/Jasmine the app's handlers are not installed (`TestingApp` overrides `_installErrorHandler()`), and Jasmine fails the spec on any unhandled rejection. Jasmine's own listener is registered when its env boots, before any spec or
  app code runs. So a listener added later can't stop it, not even with `stopImmediatePropagation()`, and in the reference migration a capture-phase listener didn't work either. The solution:
  - `karma-jasmine-scout` provides the framework `jasmine-scout-preload`. It loads `unhandledRejectionFilter.js` *before* `jasmine.js`, so its listener is registered first.
  - That listener calls `window.jasmineScoutUnhandledRejectionFilter(event)`, which `TestingApp` sets (delegating to `errorHandler.isIgnorableRejection()`). If the filter returns `true`, it calls `preventDefault()` +
    `stopImmediatePropagation()`, so Jasmine never sees the rejection.
  - `jasmine-scout-preload` must be listed **after** `jasmine` in the Karma `frameworks` (Karma initializes frameworks in order and `jasmine` prepends its files). `karma-defaults.js` does this; a module that sets its own `frameworks` must
    add it too.

Real errors are unaffected and still fail the spec.

Two related patterns to get right while you're in this territory:

- **Cleanup that must run regardless of success/failure, without swallowing the error:** don't write `promise.then(cleanup, cleanup)` — if `cleanup` doesn't rethrow, that silently turns a rejected chain into a resolved one. Use `promise.finally(cleanup)` instead: it still runs unconditionally, but the rejection (if any) propagates to the promise `.finally()` returns, so it can still be caught further up or surface as unhandled if nobody's watching.
- **Drop "catch now, rethrow later" constructs.** jQuery code sometimes caught errors in `.done()`/`.fail()` callbacks (`try { ... } catch (err) { jsError = err; }`) so that the `.always()` cleanup still ran, and rethrew them at the end.
  With native promises, `.finally()` already runs after a failing `.then()`. Chain `.then(onDone, onFail).finally(onAlways)` and let errors propagate. Where nobody else can ever see the promise (e.g. a central request queue), end the chain
  with one terminal `.catch(error => App.get().errorHandler.handle(error))` instead of a per-callback try/catch.
- **A `.then(fn).catch(fn)` used only to avoid an unhandled rejection on the returned promise** (rather than for genuine multi-stage handling) is usually a sign the fire-and-forget question above wasn't asked yet — resolve it the same way rather than defaulting to that shape.

If you do end up adding a no-op guard for the "already reported elsewhere" case, keep it a plain `.catch(() => {})` / `.then(fn, () => {})` with a comment — don't wrap it in a shared helper function; that's exactly the clutter a future global handler is meant to remove.

## 8. Staleness/supersession checks: replace counters with identity checks

Watch for patterns like:
```ts
if (this._pendingCounter === 1) {
  this._onSuccess(value);
}
```
used to detect "is this the *last* of several overlapping async operations, ignore the superseded ones." This relied on old jQuery's `.done()/.fail()` firing **synchronously**, which made superseded (e.g. aborted) operations reliably settle before the current one. Under native Promise there's no such guarantee — whichever settles first depends on microtask/macrotask interleaving, which can differ per call site and even reverse under mocked-vs-real timing. Replace the counter check with an **identity check** against the most-recently-started operation:
```ts
this._pendingOperation = myPromise;
myPromise.then(
  value => { if (this._pendingOperation === myPromise) this._onSuccess(value); ... },
  error => { if (this._pendingOperation === myPromise) this._onFailure(error); ... }
);
```
This is correct regardless of settle order. If you find this bug, also check whether the same class (or a subclass, e.g. `SmartField` extends `ValueField`) shares the affected method — the fix usually applies broadly.

The same applies to **"clear the pending promise when done"** cleanup, e.g. `.always(() => this._pendingPromise = null)` right after aborting the previous request. Under jQuery the aborted request's cleanup ran synchronously
during the abort, *before* the new promise was assigned. Natively it runs later and wipes out the *new* request's promise. Make the cleanup conditional:

```ts
const myPromise = deferred.promise();
this._pendingPromise = myPromise;
request.finally(() => {
  if (this._pendingPromise === myPromise) {
    this._pendingPromise = null;
  }
});
```

## 9. Test-only gotchas (recap)

- `FakeXMLHttpRequest already completed` from jasmine-ajax almost always means your synchronization assumption was wrong — some earlier async step you assumed had completed (e.g. a poller's next request being sent) actually hadn't yet, so `jasmine.Ajax.requests.mostRecent()` returns a stale, already-answered request. Fix by waiting for the actual effect (e.g. poll count increased), not a fixed tick/sleep amount.
- **Beware of the silent version of the same bug.** Scout's `receiveResponseForAjaxCall(request, response)` falls back to `jasmine.Ajax.requests.mostRecent()` when `request` is undefined. A helper like
  `receiveResponseForAjaxCall(arrays.last(jasmine.Ajax.requests.filter('api/unlock')), ...)` that runs *before* the request is sent (it is now sent a few microtasks later, after an earlier promise settles) therefore answers a *different*
  pending request, or does nothing. The spec then hangs until the jasmine timeout. Two fixes:
  - Assert that the request exists before responding, so the helper fails fast.
  - Better still, stub requests that are sent asynchronously up front with `jasmine.Ajax.stubRequest(...)` / `JasmineScoutUtil.mockRestCall(...)`. They are then answered whenever they are sent, and the test simply awaits the production
    promise.
- **`jasmine.clock().install()` → `tick()` → `uninstall()` blocks around code that is now native-Promise-based are usually just dead weight.** They don't drive the promise chain; they only turn timers into fake timers that are dropped on
  uninstall. Replace them with:
  - an `await` on the returned promise or on an event the framework already fires. There usually is one, so look for it before inventing a mechanism:
    - message box opened: `await session.desktop.when('propertyChange:messageBoxes')`, or `await form.when('propertyChange:messageBoxes')` if the form is the message box's display parent;
    - the promise the action itself returns: `await form.lifecycle.ok()`, `await form.lifecycle.cancel()`, `await form.load()`, instead of calling it and then waiting for a guessed number of ticks;
    - form lifecycle: `form.whenClose()`, `form.whenLoad()`, `form.whenSave()`;
    - page child loading: `await page._loadChildrenPromise` of the page whose children are being loaded (it's only set while a load is in progress). Calling `ensureLoadChildren()` would trigger a load itself;
    - pending/resolved checks: `await expectAsync(promise).toBePending()` / `.toBeResolved()` (§5);
    - any property: `widget.when('propertyChange:xyz')`. This also works on non-widgets that are a `PropertyEventEmitter`, e.g. a table `Column` (`column.when('propertyChange:text')`);
    - the property a lookup finally writes: `smartField.when('propertyChange:displayText')`, `column.when('propertyChange:text')`;
    - custom widget events the production code triggers at the end of a flow, e.g. `widget.when('valueApplied')`; or the value a flow restores, e.g. `field.when('propertyChange:value')`;
    - keep it plain: write the `when(...)` inline in the spec. Don't add wrapper helpers or "already resolved?" guards around it unless a spec actually needs one;
    - **when to register the listener:** *before* the action if the event can fire during it (e.g. `const p = widget.when('valueApplied'); formSpecHelper.closeMessageBoxes(MessageBox.Buttons.YES); await p;`). But register it *after* a
      synchronous call that fires
      an unrelated early change of the same property. E.g. a type switch first clears a smart field (`displayText` → `''`) and the lookup sets the real text later, so call `setValue()` first, then
      `await field.when('propertyChange:displayText')`;
    - the event must actually fire: `setProperty()` doesn't fire if the value doesn't change, so check that the action really changes it (e.g. a `setValue()` with the current value never triggers a lookup);
  - if the remaining work after that signal is only a known, short promise tail (e.g. one or two `.finally()` callbacks), a plain `await sleep()` per step, with a comment naming what it waits for (
    `await sleep(); // 2nd finally in MyService.ts`);
  - `autoTick()` if a long timeout needs fast-forwarding. Then `await sleep(N)` advances fake time deterministically, e.g. `sleep(500)` → "not yet expired", `sleep(510)` → "expired" for a 1s timeout. If the only asynchronous part is a
    lookup (no long timeout), you don't need the clock at all. Drop `install()`/`autoTick()`/`uninstall()` and await the property the lookup writes instead of `sleep(N)`.

  Don't write generic polling helpers (`waitFor(() => condition)`). They hide which signal the test actually depends on, and they are not wanted in this codebase.

  Once the clock is gone, timers the code under test schedules become *real*. Destroy the objects that own them (e.g. a message box with an auto-close timeout) in the test's cleanup, or they fire during a later spec.
- For "nothing happens" assertions (e.g. `expect(findMessageBoxes().size).toBe(0)` right after triggering an asynchronous handler), first give the handler a chance to run (`await sleep(50)`). Otherwise, the assertion is trivially true.
- **Karma "Disconnected, because no message in 30000 ms"** means no spec finished for 30s. A spec that merely waits on a promise that never resolves fails after the Jasmine timeout (default 5s) instead, unless it has a long custom timeout (
  `it(..., 100_000)`). In the reference migration the disconnect disappeared once the unhandled rejections (§7a) and the unreturned/overtaking chains (§6, §7) were fixed, without a dedicated fix. If it persists, narrow it down: put
  `fdescribe` on groups of spec files, or pause the debugger in the Karma debug tab when the log stalls.
- **Work through failures in rounds, noisiest first.** Fix the global abort handling (§7a) before anything else, because hundreds of abort rejections bury the real failures. Then re-run and fix the now-visible failures by root cause, one
  group at a time (lagging values, missing lookup texts, unreturned chains, …). A single root cause often explains a dozen failing specs.
- See §6 for `expectAsync` timing and §7 for the general unhandled-rejection checklist.

## 10. General workflow

1. Grep the whole codebase with the full pattern from §1 (including the multi-argument callback pattern); don't trust a narrower first pass.
2. Record a `tsc --noEmit` baseline per module (§1).
3. Fix foundational modules first.
4. For each file: bulk-swap pure type annotations, then hand-review every `$.Deferred()`/`$.when`/`$.promiseAll`/`.done/.fail/.always`/`.state()` site using the rules in §3–§5 and §8.
5. Grep again after each module with the same full pattern to catch anything missed (do this even for modules you think are "done" — re-run the wider pattern, not just what caught your eye), and diff the tsc output against the baseline,
   including downstream modules.
6. Modules reported as "already migrated" are not necessarily done. Re-check them with tsc and the full pattern. Leftovers there tend to be exactly the cases a narrow first pass misses (`$.when`, `$.promiseAll` arity, `.state()` in specs,
   indexed ajax results).
7. Report a clear diff summary; you likely can't run this project's test suite yourself, so flag anything you're less than fully confident about rather than asserting it's correct.
