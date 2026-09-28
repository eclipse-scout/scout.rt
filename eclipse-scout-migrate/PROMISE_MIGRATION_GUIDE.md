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

The compiler cannot see everything. Callbacks typed `any` or annotated with the wrong shape (e.g. `.then((setting: SettingDo) => setting)` on a `Promise<any>`) type-check fine but are wrong at runtime. So always combine tsc with the grep
patterns above.

## 2. Mechanical vs. manual changes — don't treat them the same

- **Type-only** (`JQuery.Promise<T>` → `Promise<T>`) is safe to batch with `sed` across a whole file, **except** the 2-argument jQuery form `JQuery.Promise<TResolve, TReject>` — drop the second type param entirely (native `Promise<T>` has no typed-rejection equivalent); handle those by hand.
- **Any real `$.Deferred()`, `.done()`, `.fail()`, `.always()` usage needs individual, careful review** — see the translation rules below. Do not regex-replace these.
- For every `protected override` method whose return type you're touching, verify the **exact** signature against the base class in `@eclipse-scout/core` (e.g. `Form._load()`, `Form._save()`, `PageWithTable._loadTableData()`,
  `ValueField._validateValue()`, `BasicField.acceptInput()`, `App._init()`/`_load()`/`_defaultBootstrappers()`, `UiCallbackHandler.handle()`) — TS allows a narrower override, but confirm it, don't assume.
- **Not everything named `JQuery.Promise` should be migrated.** jQuery *animation* promises (`$elem.animate(...).promise()`, e.g. `TileGridLayout._animateTiles(): JQuery.Promise<JQuery>[]`) remain jQuery promises in the base framework;
  overrides must keep that type. Also leave vendored third-party code alone (e.g. a bundled editor library with its own `.done()/.fail()` API).
- **Type errors can expose pre-existing bugs.** Example: `_installMonaco(): Promise<Monaco>` whose `.then(monaco => { this.monaco = monaco; })` never returned the value. jQuery's typings didn't complain; native `Promise<void>` vs
  `Promise<Monaco>` does. Fix the implementation (`return monaco;`), not the declared type, after checking which consumers rely on the value.
- **Loosen parameter types that accept promises from not-yet-migrated callers.** For example, change a loader parameter typed `() => JQuery.jqXHR` to `() => PromiseLike<T>`, and wrap its result with `Promise.resolve(loader())`. That way
  downstream repositories that still pass a jqXHR keep compiling and working.

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

A bound function can also receive the value *by accident*. In `promise.always(this._postProcessIcon.bind(this))`, where `_postProcessIcon(zoomFactor?: number)`, the old code passed `undefined` on success but the rejection *string* on
failure as `zoomFactor`. Translate this to what was actually intended, e.g. `.finally(() => this._postProcessIcon(null))`, not to a literal equivalent.

**c2) A detached `.always()`/`.fail()` branch becomes an unhandled rejection.** Pattern:

```js
promise.always(() => this.setBusy(false));   // separate statement, result discarded
return promise.then(...);
```

Under jQuery the discarded branch was harmless. Natively, `promise.finally(...)` returns a *new* promise that rejects whenever `promise` rejects. Nobody observes that new promise, so it surfaces as an unhandled rejection even though the
caller handles the main chain. Fold the cleanup into the chain that is actually returned, stored or awaited (`return promise.finally(() => this.setBusy(false)).then(...)`) instead of translating the statement one-to-one.

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
- **The dangerous `$.promiseAll` case is the one that still compiles:** a callback that only wanted the *first* value, e.g. `.then((setting: SettingDo) => setting)`. Under jQuery that received the first resolved value. Now it silently
  receives (and returns) the whole array. Write `.then(([setting]) => setting)`. Only calls **without** `asArray` changed; `$.promiseAll(promises, true)` always resolved with an array and needs no change.
- Ajax success handlers used to receive `(data, textStatus, jqXHR)` as separate arguments — the native-Promise-based `ajax.*` helpers only ever resolve with the response body. If you need the status/jqXHR/error details, check whether the framework wraps them in something like `AjaxError` on the rejection path — use that instead of expecting extra success-handler arguments.
  - Watch for indexed access that compensated for this. When a jqXHR went through `$.when`/`$.promiseAll`, its three resolve values were collapsed into an array, so code read `response[0].settings` (typed e.g. `[ResponseDo, any, any]`).
    That must now be `response.settings`.
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

In specs, `expect(promise.state()).toBe('pending')` can become a settled flag, e.g. `promise.then(() => settled = true, () => settled = true)`, while still awaiting the original promise for the real assertion.

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

Consequence: once `jasmine.clock().install()` is active, any code that still calls `.then()/.catch()` on a raw jQuery Deferred/jqXHR has that reaction turned into a **fake timer**, invisible to `flushMicrotasks()` (which only drains microtasks). A single ajax response's full processing can require alternating fake-timer hops (jQuery's internal `.then()`) and native-microtask hops (the rest of your Promise chain), sometimes several rounds deep if success also schedules a *new* timer (e.g. a poller re-arming itself). Manually guessing `await flushMicrotasks(N); jasmine.clock().tick(M);` combinations is fragile and was the source of most of the flakiness in the reference migration.

**Fix: use `jasmine.clock().autoTick()`** (jasmine-core ≥ 5.7) instead of manual `tick()`/`flushMicrotasks()` pairs. Call it once, right after `install()`:
```ts
beforeEach(() => {
  jasmine.clock().install();
  jasmine.clock().autoTick();
});
afterEach(() => {
  jasmine.clock().uninstall(); // also cleanly stops the auto-tick loop
});
```
It continuously fires the next due fake timer and yields to a real macrotask in between (letting any native-Promise chain — and anything it schedules — run to completion) automatically, without you needing to know the hop count. It still fast-forwards long fake delays (e.g. a 30s retry interval) without the test actually waiting 30 real seconds. Combine it with real `await sleep(N)` (a small real wait, e.g. 50ms) at points where you just need to let things settle with no specific event to await, and with precise signals (`widget.when('propertyChange:xyz')`, a returned promise, a request-count check) wherever the code exposes one — always prefer a precise signal over a guessed wait when one is available.

When wrapping a raw `$.ajax()`/jqXHR directly, prefer this project's/framework's own `ajax.getJson()`/`ajax.postJson()`/`AjaxCall` (native-Promise, already migrated) over hand-rolling a new wrapper. If you must wrap a raw jqXHR yourself, use the **single two-argument** `jqXHR.then(onFulfilled, onRejected)`, not `jqXHR.then(onFulfilled).catch(onRejected)`. The chained form costs the rejection path *two* jQuery-internal scheduling hops instead of one — a real asymmetry that can flip the relative order in which two competing async operations (e.g. an aborted call vs. a fresh one) settle, compared to what the pre-migration code guaranteed.

**Audit your own test helpers for the same disease.** Any shared test utility that "flushes" queued ajax calls or their responses using `jasmine.clock().tick()` alone (implicitly assuming synchronous jQuery-Deferred resolution — e.g. a `sendQueuedAjaxCalls()`-style helper) will likely stop working once the production code it drives switches from `.done()` to `.then()`. `tick()` cannot flush the resulting native-Promise chain. Write an `async` equivalent that awaits a real completion signal instead (e.g. the framework's `session.whenRequestsDone()`), convert every caller from the sync helper to the async one, and mark those tests `async`.

## 7. The other big class of bug: fire-and-forget promises and unhandled rejections

Native Promises trigger a global "unhandled rejection" error when nothing ever attaches a `.catch()`/`.then(_, onRejected)`/`.finally()`-with-rethrow to them — Karma/Jasmine treats this as a failing test. jQuery has no such mechanism at all, so every place a Promise-returning call's result was neither returned, stored, nor awaited was **silently swallowed under jQuery** and is now a genuine, loud failure. This is not a new bug you're introducing — it's a pre-existing gap the migration makes visible. Concrete places this shows up:

- **Expected errors need explicit handling.** E.g. aborting an in-flight lookup/search call is expected to reject — make sure the abort path has a real `.catch()`, not just an assumption that nobody's listening (see any `LookupBox`/search-abort style code as an example of the pattern to check).
- **Stray timers firing after a test (or the object) is gone.** A `setTimeout` scheduled by, say, a debounced "send" method can fire after the test ends and hit a partially torn-down mock, producing an unhandled rejection in an unrelated later test's `afterAll`. Any `destroy()`-style method should explicitly `clearTimeout` whatever it scheduled (check the base framework's own `Session.destroy()`/`HybridManager.destroy()`-equivalents for the pattern, and make sure your test teardown actually calls `destroy()`/cancels those timers between tests).
- **A leftover ajax call executing after the test ends.** Make sure `jasmine.Ajax.install()`/`uninstall()` bracket every test that can trigger a request, so a request that fires later doesn't escape as a real network call.
- **For genuinely expected rejections, write `expectAsync(promise).toBeRejected()`** rather than letting the rejection go unhandled or wrapping it in a try/catch that swallows the assertion value. But note the timing trap: **the call itself is what attaches the handler**; the `await` of its result can happen later. If you call `expectAsync(...)` only at the end of a test, after the promise already rejected earlier in the test body, Karma has already flagged it as unhandled by the time you get there:
  ```ts
  let promise = doSomethingThatWillReject();
  let rejected = expectAsync(promise).toBeRejected(); // attach now
  // ... rest of the test, including whatever triggers the rejection ...
  await rejected; // await later
  ```

**Don't reach for a bare `.catch(() => {})` / `.then(fn, () => {})` as the default fix for a fire-and-forget call site (typical for UI event handlers where nobody awaits the result).** That was jQuery's actual behavior — silently dropping the error — and reproducing it mechanically throws away exactly the diagnostic value native Promise gives you over jQuery. Before adding a no-op guard, decide which of these two cases you're in:

- **The error is already reported somewhere in the call chain** (e.g. an inner call already goes through the framework's `ErrorHandler`, or an event that already gets to a caller that handles it, before rethrowing/settling as rejected). Here an empty guard purely to prevent an unhandled-rejection failure is fine — add a one-line comment saying so, so the next reader doesn't mistake it for a silent swallow.
- **There is genuinely no other reporting path.** Don't paper over this with a local `.catch(error => someErrorHandler.handle(error))` at every call site either — that just trades one form of clutter (silent swallow) for another (per-call-site error-handling boilerplate), and this project may already be planning (or have) a global unhandled-rejection handler analogous to its synchronous one. If so, the correct migration is usually to **do nothing** — leave the rejection unhandled and let it surface; that's the notification, and it's exactly what a global handler is meant to catch. Ask the project owner before inventing a per-site reporting convention.

Two related patterns to get right while you're in this territory:

- **Cleanup that must run regardless of success/failure, without swallowing the error:** don't write `promise.then(cleanup, cleanup)` — if `cleanup` doesn't rethrow, that silently turns a rejected chain into a resolved one. Use `promise.finally(cleanup)` instead: it still runs unconditionally, but the rejection (if any) propagates to the promise `.finally()` returns, so it can still be caught further up or surface as unhandled if nobody's watching.
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

The same applies to **"clear the pending promise when done"** cleanup, e.g. `.always(() => this._pendingPromise = null)` right after an `abortAndReset()` of the previous request. Under jQuery the aborted request's cleanup ran synchronously
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
