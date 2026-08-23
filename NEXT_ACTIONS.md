# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/5 (100.0%)
- **Function parity:** 78/104 matched (target 420) — 75.0%
- **Class/type parity:** 24/33 matched (target 55) — 72.7%
- **Combined symbol parity:** 102/137 matched (target 475) — 74.5%
- **Average inline-code cosine:** 0.46 (function body across 5 matched files)
- **Average documentation cosine:** 0.66 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 5 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. glob

- **Target:** `globset.Glob`
- **Similarity:** 0.59
- **Dependents:** 1
- **Priority Score:** 1196504.1
- **Functions:** 37/53 matched (target 326)
- **Missing functions:** `as_ref`, `eq`, `hash`, `fmt`, `from_str`, `deref`, `deref_mut`, `compile_strategic_matcher`, `add_to_last_range`, `starts_with`, `ends_with`, `s`, `class`, `classn`, `rclass`, `rclassn`
- **Types:** 9/12 matched (target 25)
- **Missing types:** `Err`, `Target`, `Options`
- **Tests:** 0/8 matched

### 2. lib

- **Target:** `globset.Lib`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 105105.3
- **Functions:** 28/35 matched (target 67)
- **Missing functions:** `fmt`, `new_regex`, `new_regex_set`, `from_cow`, `prefix`, `suffix`, `regex_set`
- **Types:** 13/16 matched (target 24)
- **Missing types:** `PatternSetPoolFn`, `MultiStrategyBuilder`, `RequiredExtensionStrategyBuilder`
- **Tests:** 5/5 matched

### 3. serde_impl

- **Target:** `globset.SerdeImpl`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 61306.9
- **Functions:** 7/10 matched (target 9)
- **Missing functions:** `expecting`, `visit_str`, `visit_seq`
- **Types:** 0/3 matched
- **Missing types:** `GlobVisitor`, `Value`, `GlobSetVisitor`
- **Tests:** 5/5 matched

### 4. fnv

- **Target:** `globset.Fnv`
- **Similarity:** 0.38
- **Dependents:** 0
- **Priority Score:** 506.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 5. pathutil

- **Target:** `globset.Pathutil`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 304.4
- **Functions:** 3/3 matched (target 15)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

