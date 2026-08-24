=== Deep Analysis: tmp/globset/src (rust) -> src/commonMain/kotlin (kotlin) ===

Scanning source codebase (rust)...
Codebase: tmp/globset/src (rust)
  Files: 5
  Total imports: 26
  Most depended: glob (1 dependents)

Scanning target codebase (kotlin)...
Codebase: src/commonMain/kotlin (kotlin)
  Files: 10
  Total imports: 34

Comparing codebases...
Computing AST similarities...

=== Codebase Comparison Report ===

Source: tmp/globset/src (5 files)
Target: src/commonMain/kotlin (10 files)
Scoring invariant: FnSim is required function body/parameter similarity. Class/type and symbol parity are reported beside it; whole-file shape is diagnostic only.

Matched:   5 files
Unmatched: 0 source, 1 target

=== Matched Files (by porting priority) ===

Source                        Target                        FnSim     Dependents FunctionParityTypeParity  Priority
 --------------------------------------------------------------------------------------------------------------------------
glob                          globset.Glob                  0.64      1          44/53         9/12        1126503.6 
serde_impl                    globset.SerdeImpl             0.31      0          7/10          0/3         61306.9   
lib                           globset.Lib                   0.55      0          35/35         16/16       5104.5    
fnv                           globset.Fnv                   0.38      0          3/3           2/2         506.2     
pathutil                      globset.Pathutil              0.56      0          3/3           0/0         304.4     

=== Function and Symbol Details ===

glob -> globset.Glob
  similarity: 0.64, priority: 1126503.6, dependents: 1
  functions: 44/53 matched (target total: 336, required body score: 0.64)
  missing functions: compile_strategic_matcher, add_to_last_range, starts_with, ends_with, s, class, classn, rclass, rclassn
  types: 9/12 matched (target total: 25)
  missing types: Err, Target, Options
  tests: 0/8 matched

serde_impl -> globset.SerdeImpl
  similarity: 0.31, priority: 61306.9, dependents: 0
  functions: 7/10 matched (target total: 9, required body score: 0.31)
  missing functions: expecting, visit_str, visit_seq
  types: 0/3 matched (target total: 3)
  missing types: GlobVisitor, Value, GlobSetVisitor
  tests: 5/5 matched

lib -> globset.Lib
  similarity: 0.55, priority: 5104.5, dependents: 0
  functions: 35/35 matched (target total: 82, required body score: 0.55)
  missing functions: none
  types: 16/16 matched (target total: 25)
  missing types: none
  tests: 5/5 matched

fnv -> globset.Fnv
  similarity: 0.38, priority: 506.2, dependents: 0
  functions: 3/3 matched (target total: 3, required body score: 0.38)
  missing functions: none
  types: 2/2 matched (target total: 2)
  missing types: none

pathutil -> globset.Pathutil
  similarity: 0.56, priority: 304.4, dependents: 0
  functions: 3/3 matched (target total: 15, required body score: 0.56)
  missing functions: none
  types: 0/0 matched (target total: 1)
  missing types: none


=== Porting Quality Summary ===

Matched by exact header:          5 / 5
Matched by provenance fallback:   0 / 5
Matched by name:                  0 / 5
Total TODOs in target: 0
Total lint errors:    0
Stub files:           0

=== Big Picture ===

- Missing files: 0
- Incomplete ports (similarity < 60%): 4
- Stub files: 0
- Files missing functions: 2 (total deficit: 12 functions)
- Type definitions missing: 6
- Files missing tests: 1 (total deficit: 8 unported `#[test]` functions)
- Documentation coverage: 229 / 684 lines (33%)

Primary focus: port missing functions/tests to reach per-file parity (12 functions, 8 tests)

=== Files with Issues ===

File                          Similarity LineRatio  FunctionParityTests     TODOs Lint  Status
----------------------------------------------------------------------------------------------------
globset.Glob                  0.64       0.00       44/53         0/8       0     0     MISSING_FUNCS
  missing functions: `compile_strategic_matcher`, `add_to_last_range`, `starts_with`, `ends_with`, `s`, `class`, `classn`, `rclass`, `rclassn`
  missing types: `Err`, `Target`, `Options`
globset.SerdeImpl             0.31       0.00       7/10          5/5       0     0     LOW_SIM
  missing functions: `expecting`, `visit_str`, `visit_seq`
  missing types: `GlobVisitor`, `Value`, `GlobSetVisitor`
globset.Lib                   0.55       0.00       35/35         5/5       0     0     
globset.Fnv                   0.38       0.00       3/3           -         0     0     LOW_SIM
globset.Pathutil              0.56       0.00       3/3           -         0     0     

=== Porting Recommendations ===

Incomplete ports (similarity < 60%): 4
Missing files: 0

Incomplete ports to complete:
  serde_impl                     similarity=0.31 function_parity=7/10 dependents=0
    missing functions: `expecting`, `visit_str`, `visit_seq`
    missing types: `GlobVisitor`, `Value`, `GlobSetVisitor`
  lib                            similarity=0.55 function_parity=35/35 dependents=0
  fnv                            similarity=0.38 function_parity=3/3 dependents=0
  pathutil                       similarity=0.56 function_parity=3/3 dependents=0

=== Documentation Gaps ===

There is missing documentation that is hurting overall scoring.
Documentation coverage: 229 / 684 lines (33%)
Files with >20% doc gap: 3

File                          Src Docs    Tgt Docs    Gap %     DocSim    DocAmt    DocEq     
----------------------------------------------------------------------------------------------
glob                          372         109         70%       0.89      0.29      0.59      
lib                           260         90          65%       0.80      0.35      0.57      
pathutil                      48          28          41%       0.92      0.58      0.75      

=== Generating Reports ===

✅ Generated: port_status_report.md
✅ Generated: high_priority_ports.md
✅ Generated: NEXT_ACTIONS.md
✅ Generated: port_lint_proposed_changes.md

📁 All reports generated successfully!
