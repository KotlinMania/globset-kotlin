# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/6 (83.3%)
- **Function parity:** 92/115 matched (target 445) — 80.0%
- **Class/type parity:** 27/33 matched (target 56) — 81.8%
- **Combined symbol parity:** 119/148 matched (target 501) — 80.4%
- **Average inline-code cosine:** 0.49 (function body across 5 matched files)
- **Average documentation cosine:** 0.72 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. glob

- **Target:** `globset.Glob [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 1
- **Priority Score:** 1126503.6
- **Functions:** 44/53 matched (target 336)
- **Missing functions:** `compile_strategic_matcher`, `add_to_last_range`, `starts_with`, `ends_with`, `s`, `class`, `classn`, `rclass`, `rclassn`
- **Types:** 9/12 matched (target 25)
- **Missing types:** `Err`, `Target`, `Options`
- **Tests:** 0/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `glob.rs` vs expected `glob.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:glob.rs` vs expected `glob.rs`
- **Proposed provenance header:** `// port-lint: source glob.rs` (current: `// port-lint: source glob.rs`)
- **Proposed provenance header:** `// port-lint: tests glob.rs` (current: `// port-lint: tests glob.rs`)
- **Lint issues:** 2

### 2. serde_impl

- **Target:** `globset.SerdeImpl [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 61306.9
- **Functions:** 7/10 matched (target 9)
- **Missing functions:** `expecting`, `visit_str`, `visit_seq`
- **Types:** 0/3 matched
- **Missing types:** `GlobVisitor`, `Value`, `GlobSetVisitor`
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `serde_impl.rs` vs expected `serde_impl.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:serde_impl.rs` vs expected `serde_impl.rs`
- **Proposed provenance header:** `// port-lint: source serde_impl.rs` (current: `// port-lint: source serde_impl.rs`)
- **Proposed provenance header:** `// port-lint: tests serde_impl.rs` (current: `// port-lint: tests serde_impl.rs`)
- **Lint issues:** 2

### 3. lib

- **Target:** `globset.Lib [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 5104.5
- **Functions:** 35/35 matched (target 82)
- **Missing functions:** _none_
- **Types:** 16/16 matched (target 25)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Proposed provenance header:** `// port-lint: tests lib.rs` (current: `// port-lint: tests lib.rs`)
- **Lint issues:** 2

### 4. fnv

- **Target:** `globset.Fnv [PROVENANCE-FALLBACK]`
- **Similarity:** 0.38
- **Dependents:** 0
- **Priority Score:** 506.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `fnv.rs` vs expected `fnv.rs`
- **Proposed provenance header:** `// port-lint: source fnv.rs` (current: `// port-lint: source fnv.rs`)
- **Lint issues:** 1

### 5. pathutil

- **Target:** `globset.Pathutil [PROVENANCE-FALLBACK]`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 304.4
- **Functions:** 3/3 matched (target 15)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `pathutil.rs` vs expected `pathutil.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:pathutil.rs` vs expected `pathutil.rs`
- **Proposed provenance header:** `// port-lint: source pathutil.rs` (current: `// port-lint: source pathutil.rs`)
- **Proposed provenance header:** `// port-lint: tests pathutil.rs` (current: `// port-lint: tests pathutil.rs`)
- **Lint issues:** 2

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

