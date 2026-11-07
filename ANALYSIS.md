# Assignment 4 — analysis (complete in English)

## Environment and commands

- **Selected language:** Java 17+ (compiled with `--release 17`)
- **Runtime:** Java HotSpot(TM) 64-Bit Server VM (build 25.0.1+8-LTS-27), OpenJDK / Oracle JDK 25
- **Launcher environment:** Python 3.13.5 (UTF-8 JSON Lines launcher)
- **Operating system:** Windows 11 Enterprise (64-bit)
- **CPU:** Intel64 Family 6 Model 154 Stepping 4 (12 logical processors)
- **RAM:** 16 GB physical memory
- **Build / Run / Test commands:**
  - Setup validation: `python tools/run.py --language Java --setup`
  - Full automated checks: `python tools/run.py --language Java --check`
  - Student test batch execution: `python tools/run.py --language Java --input tests/student/cases.jsonl`
- **Repository URL:** https://github.com/aibergen74-create/DAA-assignment4

---

## Algorithm explanations

### 1. Longest Common Subsequence (`lcs`)

- **State and invariant:** $dp[i][j]$ denotes the length of the longest common subsequence of prefixes $a[0..i-1]$ and $b[0..j-1]$. Indices $i \in [0, |a|]$ and $j \in [0, |b|]$ represent prefix lengths.
- **Transition and base cases:**
  - Base cases: $dp[0][j] = 0$ for all $j$, and $dp[i][0] = 0$ for all $i$ (an empty prefix shares no characters).
  - Transition for $i, j \ge 1$:
    - If $a[i-1] == b[j-1]$: $dp[i][j] = dp[i-1][j-1] + 1$.
    - If $a[i-1] \ne b[j-1]$: $dp[i][j] = \max(dp[i-1][j], dp[i][j-1])$.
- **Processing order:** Standard row-by-row tabulation ($i$ from $1$ to $|a|$, $j$ from $1$ to $|b|$). Dependencies $(i-1, j-1)$, $(i-1, j)$, and $(i, j-1)$ are strictly computed in preceding rows/columns before visiting $(i, j)$.
- **Correctness argument:** By optimal substructure, if the terminal characters match, extending the optimal LCS of $a[0..i-2]$ and $b[0..j-2]$ is strictly optimal. If they mismatch, at least one of $a[i-1]$ or $b[j-1]$ cannot be in an optimal alignment, so taking the maximum of dropping either character exhausts all optimal possibilities.
- **Reconstruction and tie handling:** Backtracking begins at $(|a|, |b|)$. If $a[i-1] == b[j-1]$, the character is appended and $(i, j) \to (i-1, j-1)$. If $dp[i-1][j] \ge dp[i][j-1]$, we move to $(i-1, j)$; otherwise to $(i, j-1)$. Reversing the gathered characters produces an optimal subsequence. In case of ties, choosing either branch produces an equally valid optimal witness.
- **Time and memory:** Time complexity is $O((|a|+1)(|b|+1))$ operations. Auxiliary storage is $O((|a|+1)(|b|+1))$ using a 2D integer matrix `int[|a|+1][|b|+1]`.
- **Boundary/counterexample test:** $a = \text{"AB"}, b = \text{"BA"}$. An incorrect algorithm assuming contiguous substrings would yield length 1, but might fail to reconstruct a valid non-contiguous common subsequence or fail on inverted order. Detected defect: treating subsequence matching as substring matching or incorrectly taking characters out of sequence.

---

### 2. Ordered Adjacent-Block Merging (`merge`)

- **State and invariant:** $dp[l][r]$ represents the minimum total cost required to merge contiguous initial blocks from index $l$ to index $r$ (inclusive) into a single composite block.
- **Transition and base cases:**
  - Base cases: $dp[i][i] = 0$ for all $0 \le i < n$ (a single block requires no merges).
  - Transition for interval length $len = r - l + 1 \ge 2$:
    $$dp[l][r] = \min_{l \le k < r} \left( dp[l][k] + dp[k+1][r] \right) + \sum_{t=l}^r sizes[t]$$
    Prefix sums `pref[r+1] - pref[l]` evaluate the sum in $O(1)$.
- **Processing order:** Increasing interval length $len$ from $2$ to $n$, with left boundary $l$ from $0$ to $n - len$. All strictly shorter subintervals $[l, k]$ and $[k+1, r]$ are evaluated before $[l, r]$.
- **Correctness argument:** Any sequence of merges that reduces $[l, r]$ to one block must end with a final merge combining some left adjacent block $[l, k]$ and right adjacent block $[k+1, r]$ with cost equal to the sum of all elements in $[l, r]$. Because block sizes and costs are additive and independent, optimal substructure holds.
- **Reconstruction and tie handling:** An auxiliary matrix `split[l][r] = k` records the optimal split point. Reconstruction uses a recursive postorder traversal: recursively emit the plan for $[l, k]$, then for $[k+1, r]$, and finally emit `[l, k, r]`. This guarantees child blocks are merged and ready before the parent merge is executed. Ties take the smallest index $k$.
- **Time and memory:** Time complexity is $O(n^3)$ operations. Memory is $O(n^2)$ using `long[n][n]` for costs and `int[n][n]` for split points.
- **Boundary/counterexample test:** $sizes = [8, 5, 4, 2]$. A greedy approach (such as standard Huffman merging) would greedily select $2$ and $4$, then $5$, violating block adjacency. The interval DP strictly enforces adjacency and yields optimal cost 36 with plan `[[2, 2, 3], [1, 1, 3], [0, 0, 3]]`.

---

### 3. Tree Maximum-Weight Independent Set (`tree`)

- **State and invariant:** Rooting the tree arbitrarily at vertex 0:
  - $take[u]$: maximum independent set weight in the subtree rooted at $u$ given that vertex $u$ is selected.
  - $skip[u]$: maximum independent set weight in the subtree rooted at $u$ given that vertex $u$ is not selected.
- **Transition and base cases:**
  - For each child $v$ of $u$:
    $$take[u] = weights[u] + \sum_{v \in children(u)} skip[v]$$
    $$skip[u] = \sum_{v \in children(u)} \max(0, take[v], skip[v])$$
  - Base cases: For a leaf vertex $u$, $take[u] = weights[u]$ and $skip[u] = 0$.
- **Processing order:** A queue-based BFS from the root determines parent pointers and a topological order. Processing in reverse BFS order guarantees all children are fully solved before evaluating their parent. Deep chains cannot overflow the call stack because no recursion is used.
- **Correctness argument:** If $u$ is selected, no adjacent vertex can be selected, forcing every child to assume its $skip[v]$ state. If $u$ is skipped, every child subtree can independently choose its optimal valid configuration (or empty set if all weights are negative).
- **Reconstruction and tie handling:** Top-down traversal along the BFS order. The root is taken if $\max(take[0], skip[0]) > 0$ and $take[0] > skip[0]$. For any subsequent vertex $v$: if its parent was taken, $v$ must be skipped; if its parent was skipped, $v$ is taken if $take[v] > skip[v]$ and $take[v] > 0$.
- **Time and memory:** $O(n)$ time and $O(n)$ auxiliary memory using primitive arrays `int[]` and `long[]` with adjacency lists.
- **Boundary/counterexample test:** A graph with negative/zero weights $weights = [-2, 0, -3]$. An algorithm that always forces vertex selection would produce a negative total weight. The empty set option correctly yields weight 0 with empty selected set.

---

### 4. Naive Substring Search (`naive`)

- **State and invariant:** At each candidate start offset $i \in [0, n - m]$, compare pattern characters $pattern[j]$ against $text[i + j]$ for $j \in [0, m - 1]$.
- **Transitions and base cases:**
  - If $m = 0$: matches all $n + 1$ boundaries $0..n$.
  - If $m > n$: immediately returns `[]`.
  - For $0 \le i \le n - m$: if $text[i+j] == pattern[j]$ for all $0 \le j < m$, record start $i$.
- **Processing order:** Strictly increasing order of candidate start indices $i = 0, 1, \dots, n - m$.
- **Correctness argument:** Exhaustively inspects every mathematically legal window of length $m$, testing all character pairs.
- **Time and memory:** $O(n \cdot m + n + 1)$ time; $O(z + 1)$ output memory for $z$ matching positions; $O(1)$ auxiliary storage.
- **Boundary/counterexample test:** $text = \text{"xaabaabaabyy"}, pattern = \text{"aabaab"}$. A faulty implementation advancing the text index by $m$ after a match finds start $1$ but skips start $4$. The required algorithm advances by 1, correctly finding both $[1, 4]$.

---

### 5. Knuth–Morris–Pratt Algorithm (`kmp`)

- **State and invariant:**
  - $\pi[i]$ is the length of the longest proper prefix of $pattern[0..i]$ that is also a suffix of $pattern[0..i]$.
  - Text search state $j$ represents the current length of the longest matching prefix of the pattern with the preceding text characters.
- **Transition and base cases:**
  - Preprocessing: $\pi[0] = 0$. For $i = 1..m-1$, fall back $j = \pi[j-1]$ until $pattern[i] == pattern[j]$ or $j = 0$; if matching, $\pi[i] = j + 1$.
  - Text search: On mismatch $text[i] \ne pattern[j]$, fall back $j = \pi[j-1]$ without advancing the text index $i$.
  - Full match: When $j == m$, record start $i - m + 1$ and continue with $j = \pi[m-1]$ to capture overlapping occurrences.
- **Processing order:** Single forward pass over the text ($i = 0..n-1$) without ever decrementing $i$.
- **Correctness argument:** The prefix function encodes all valid non-trivial boundary overlaps. Discarding characters smaller than $j - \pi[j-1]$ cannot miss any match because no border of greater length can exist.
- **Time and memory:** $O(n + m + z + 1)$ deterministic time. Auxiliary memory is $O(m + 1)$ for the `int[m]` array.
- **Boundary/counterexample test:** $pattern = \text{"aaaa"}$ searched in $text = \text{"aaaaa"}$. Resetting $j = 0$ after a match produces only $[0]$, missing overlapping matches $[1]$. Fallback to $\pi[3] = 3$ correctly identifies $[0, 1]$.

---

### 6. Rabin–Karp Search (`rk`)

- **State and invariant:** Rolling polynomial hash with fixed parameters $base = 31$ and $modulus = 101$. Hash of window $s[0..m-1]$ is:
  $$H = \left( \sum_{k=0}^{m-1} s[k] \cdot 31^{m-1-k} \right) \pmod{101}$$
- **Transition and updates:**
  - Sliding the window from index $i-1$ to $i$:
    $$H_i = \left( (H_{i-1} - text[i-1] \cdot 31^{m-1}) \cdot 31 + text[i+m-1] \right) \pmod{101}$$
  - Negative values are normalized using `(val % 101 + 101) % 101`.
  - On equal hash $windowHash == patternHash$, verify character-by-character before adding the index to results.
- **Candidate-sensitive bound:** Time is $O(n + m + c \cdot m)$, where $c$ is the number of candidate windows having equal hash. Worst case is $O(n \cdot m)$ when all windows collide. Auxiliary space is $O(1)$ beyond input/output.
- **Correctness argument:** Hash equality is a necessary filter; character verification eliminates false positives caused by modular hash collisions.
- **Boundary/counterexample test:** $text = \text{"bc_ek_bc"}, pattern = \text{"bc"}$. Both `"bc"` and `"ek"` produce hash residue $6 \pmod{101}$. An algorithm relying solely on hash equality incorrectly reports $[0, 3, 6]$. The character verification step rejects index $3$, returning exactly $[0, 6]$.

---

### 7. Suffix Array and Kasai LCP (`suffix`)

- **Prefix Doubling:**
  - Suffixes are ranked iteratively by their prefix of length $2^k$. In each round $k$, the sort key for suffix $i$ is the pair of ranks $(rank[i], rank[i + 2^k] \text{ if } i+2^k < n \text{ else } -1)$.
  - Time complexity is $O(n \log^2 n)$ using comparison sort over rank pairs. Early exit occurs as soon as all $n$ ranks become distinct.
- **Kasai's Algorithm for LCP:**
  - Invert the suffix array to get $rank[sa[i]] = i$.
  - Invariant: If $LCP(sa[rank[i]-1], sa[rank[i]]) = h$, then for suffix $i+1$, its LCP with its predecessor in $sa$ is at least $h - 1$.
  - Iterating $i$ from $0$ to $n-1$, $h$ is decremented by at most 1, while character matching increments $h$. Since $h$ increases at most $n$ times and decreases at most $n$ times, total character comparisons are bounded by $O(n)$.
- **Longest Repeated Substring:**
  - The length of the longest repeated substring is $\max(lcp[1..n-1])$. If max LCP is $> 0$, the substring of that length starting at $sa[i]$ is extracted. Overlapping occurrences are permitted.
- **Time and memory:** Doubling takes $O(n \log^2 n)$ time; Kasai takes $O(n)$. Auxiliary memory is $O(n)$ using integer arrays. No sentinel is added or leaked into output.
- **Boundary/counterexample test:** $text = \text{"$"}$ (single character). Detects array index out of bounds when the doubling loop does not run and the rank array is not inverted to suffix array positions.

---

### 8. Aho–Corasick Automaton (`aho`)

- **State and invariant:** Trie where nodes correspond to prefixes of dictionary patterns. State $0$ is the root (empty string). A failure link $fail[u]$ points to the longest proper suffix of the string representing node $u$ that is also a prefix in the trie.
- **Automaton construction:**
  - Build trie nodes for all patterns using child transitions $next[u][c]$ over printable ASCII characters $32..126$ ($95$ characters).
  - Use BFS to compute failure links: for children of the root, $fail[child] = 0$. For state $u$ and character $c$, if $child = next[u][c]$ exists, $fail[child] = next[fail[u]][c]$; otherwise $next[u][c] = next[fail[u]][c]$.
- **Reverse BFS count propagation:**
  - For each independent text, create a `visits` array initialized to 0.
  - Scan the text: `curr = next[curr][text[i] - 32]`, incrementing `visits[curr]++`.
  - Propagate matches in reverse BFS order: `visits[fail[u]] += visits[u]`.
  - For each pattern index $p$, output `visits[endNode[p]]`.
- **Duplicates and reset:** Duplicate dictionary patterns map to the same terminal node and receive equal counts in separate columns. Every independent text starts with fresh state and counter arrays.
- **Time and memory:** Construction takes $O(L + 95 \cdot S + p)$ time and $O(95 \cdot S + p)$ auxiliary memory, where $L$ is total pattern length and $S$ is state count. Querying each text takes $O(|text| + S + p)$.
- **Boundary/counterexample test:** $patterns = [\text{"he"}, \text{"she"}, \text{"he"}], texts = [\text{"sheshe"}, \text{""}]$. Tests that `"he"` is counted when `"she"` matches, duplicates maintain identical counts across separate columns, and empty text returns zero counts without stale state.

---

## Own tests

|Command|Input or fixture path|Expected result|Actual result / log|What it checks|
|---|---|---|---|---|
|`lcs`|`{"a":"CACDCCD","b":"CDBABCB"}`|`{"length":3,"sequence":"CAC"}`|`{"length":3,"sequence":"CAC"}`|Preservation of character ordering and tie handling|
|`lcs`|`{"a":"AB","b":"BA"}`|`{"length":1,"sequence":"A"}`|`{"length":1,"sequence":"A"}`|Reversed two-character string yielding length 1|
|`merge`|`{"sizes":[8,5,4,2]}`|`{"cost":36,"plan":[[2,2,3],[1,1,3],[0,0,3]]}`|`{"cost":36,"plan":[[2,2,3],[1,1,3],[0,0,3]]}`|Interval adjacency requirement vs greedy non-adjacent choices|
|`merge`|`{"sizes":[3,1,2]}`|`{"cost":9,"plan":[[1,1,2],[0,0,2]]}`|`{"cost":9,"plan":[[1,1,2],[0,0,2]]}`|Small 3-block merge cost evaluation and plan reconstruction|
|`tree`|`{"n":6,"weights":[8,1,7,6,2,8],"edges":[[0,1],[0,2],[1,3],[1,4],[2,5]]}`|`{"weight":24,"selected":[0,3,4,5]}`|`{"weight":24,"selected":[0,3,4,5]}`|Tree branching, parent exclusion, grandchild selection|
|`tree`|`{"n":3,"weights":[-2,0,-3],"edges":[[0,1],[1,2]]}`|`{"weight":0,"selected":[]}`|`{"weight":0,"selected":[]}`|Negative/zero weights allowing the empty independent set|
|`naive`|`{"text":"xaabaabaabyy","pattern":"aabaab"}`|`{"positions":[1,4]}`|`{"positions":[1,4]}`|Overlapping pattern matching across adjacent windows|
|`naive`|`{"text":"abc","pattern":""}`|`{"positions":[0,1,2,3]}`|`{"positions":[0,1,2,3]}`|Empty pattern matching all $n+1$ text boundaries|
|`kmp`|`{"text":"xaabaabaabyy","pattern":"aabaab"}`|`{"positions":[1,4]}`|`{"positions":[1,4]}`|Prefix fallback $\pi[m-1]$ preserving overlapping matches|
|`kmp`|`{"text":"abaabaabaabaabaabaa","pattern":"abaabaa"}`|`{"positions":[0,3,6,9,12]}`|`{"positions":[0,3,6,9,12]}`|Multiple chained periodic overlaps across text|
|`rk`|`{"text":"bc_ek_bc","pattern":"bc"}`|`{"positions":[0,6]}`|`{"positions":[0,6]}`|Modulus 101 collision rejection (`"ek"` hashes to 6 like `"bc"`)|
|`rk`|`{"text":"abdjzzdjababbc","pattern":"ab"}`|`{"positions":[0,8,10]}`|`{"positions":[0,8,10]}`|Multi-collision rejection (`"dj"` hashes to 75 like `"ab"`)|
|`suffix`|`{"text":"cbaabb"}`|`{"sa":[2,3,5,1,4,0],"lcp":[0,1,0,1,1,0],"repeated":"a"}`|`{"sa":[2,3,5,1,4,0],"lcp":[0,1,0,1,1,0],"repeated":"a"}`|Suffix doubling rank ties and single-character repeats|
|`suffix`|`{"text":"abaabaabaabaabab"}`|`{"sa":[2,5,8,11,14,0,3,6,9,12,15,1,4,7,10,13],"lcp":[0,10,7,4,1,2,12,9,6,3,0,1,11,8,5,2],"repeated":"abaabaabaaba"}`|`{"sa":[2,5,8,11,14,0,3,6,9,12,15,1,4,7,10,13],"lcp":[0,10,7,4,1,2,12,9,6,3,0,1,11,8,5,2],"repeated":"abaabaabaaba"}`|Large overlapping repeated substring detection (length 12)|
|`aho`|`{"patterns":["a","ba","a","aa","baa"],"texts":["baaabaaaaa"]}`|`{"counts":[[8,2,8,6,2]]}`|`{"counts":[[8,2,8,6,2]]}`|Duplicate pattern columns, overlapping matches, terminal suffixes|
|`aho`|`{"patterns":["bc","c","bc"],"texts":["b","c","bc","bcbc",""]}`|`{"counts":[[0,0,0],[0,1,0],[1,1,1],[2,2,2],[0,0,0]]}`|`{"counts":[[0,0,0],[0,1,0],[1,1,1],[2,2,2],[0,0,0]]}`|Independent text state reset and empty text zero row|

### Mixed-request and State Reset Verification

To test process isolation and guarantee that task state does not leak between consecutive queries (addressing the requirement for state reset across multiple requests in one process):
- A mixed-request batch `tests/student/cases.jsonl` containing 16 consecutive queries alternating between all 8 different task types (`lcs`, `merge`, `tree`, `naive`, `kmp`, `rk`, `suffix`, `aho`) was executed in a single persistent JVM process without restarts.
- Every algorithm allocates local data structures per invocation; no static mutable state or shared caches are retained across method calls.
- In addition, the public validation batch (`python tools/run.py --language Java --check`) automatically runs two complete batches back-to-back in a single process (`state reset: two complete batches in one process`) and passes with 100% success.

---

## Search comparison

All three search algorithms (`naive`, `kmp`, `rk`) were evaluated on identical text/pattern inputs within their common contract domain ($n, m \le 3000$). Timing was measured directly around the algorithm execution scope in microseconds ($\mu\text{s}$) using `System.nanoTime()` after a 5,000-iteration JVM JIT warm-up. Correctness was verified beforehand by confirming `outNaive.equals(outKmp) && outKmp.equals(outRk)`.

|Case|n|m|Seed/generator|Algorithm|Run 1|Run 2|Run 3|Median|Correct outputs checked|
|---|---:|---:|---|---|---:|---:|---:|---:|---|
|Random (alphabet=4)|500|50|Random(seed=42)|naive|94 µs|44 µs|32 µs|44 µs|Yes|
|Random (alphabet=4)|500|50|Random(seed=42)|KMP|111 µs|111 µs|107 µs|111 µs|Yes|
|Random (alphabet=4)|500|50|Random(seed=42)|exact RK|66 µs|34 µs|34 µs|34 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|500|50|Repetitive(char='a')|naive|944 µs|2962 µs|906 µs|944 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|500|50|Repetitive(char='a')|KMP|58 µs|178 µs|92 µs|92 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|500|50|Repetitive(char='a')|exact RK|1302 µs|3092 µs|3064 µs|3064 µs|Yes|
|Random (alphabet=4)|1500|150|Random(seed=43)|naive|94 µs|79 µs|85 µs|85 µs|Yes|
|Random (alphabet=4)|1500|150|Random(seed=43)|KMP|27 µs|27 µs|42 µs|27 µs|Yes|
|Random (alphabet=4)|1500|150|Random(seed=43)|exact RK|112 µs|108 µs|99 µs|108 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|1500|150|Repetitive(char='a')|naive|317 µs|329 µs|317 µs|317 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|1500|150|Repetitive(char='a')|KMP|56 µs|59 µs|55 µs|56 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|1500|150|Repetitive(char='a')|exact RK|480 µs|458 µs|484 µs|480 µs|Yes|
|Random (alphabet=4)|3000|300|Random(seed=44)|naive|87 µs|450 µs|356 µs|356 µs|Yes|
|Random (alphabet=4)|3000|300|Random(seed=44)|KMP|65 µs|242 µs|234 µs|234 µs|Yes|
|Random (alphabet=4)|3000|300|Random(seed=44)|exact RK|254 µs|709 µs|692 µs|692 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|3000|300|Repetitive(char='a')|naive|1333 µs|1336 µs|3563 µs|1336 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|3000|300|Repetitive(char='a')|KMP|96 µs|113 µs|477 µs|113 µs|Yes|
|Repetitive ('a'*n, 'a'*m)|3000|300|Repetitive(char='a')|exact RK|1637 µs|1492 µs|6745 µs|1637 µs|Yes|

### Analysis of search observations

1. **Random text performance:** On random texts with a small alphabet, early character mismatches allow both `naive` and `KMP` to discard candidate positions in almost $O(1)$ operations per character. KMP requires a small preprocessing phase ($O(m)$) to compute the $\pi$ table, which has negligible overhead for $m \le 300$.
2. **Repetitive text behavior:** In the repetitive case ($text = \text{'a'}^n, pattern = \text{'a'}^m$), naive search suffers $O((n - m + 1) \cdot m)$ comparisons because each window matches $m$ characters before sliding by only 1. In contrast, KMP processes each character in amortized $O(1)$ time, maintaining near-linear execution ($113\,\mu\text{s}$ at $n=3000, m=300$).
3. **The impact of the small modulus (101) on Rabin–Karp:** The laboratory modulus $101$ has only $101$ distinct hash residues. On repetitive or periodic inputs, hash collisions occur with high probability ($1/101 \approx 1\%$). In the all-'a' repetitive case, **every single window** matches the pattern hash ($H_{window} \equiv H_{pattern} \pmod{101}$), forcing Rabin–Karp to perform a full $O(m)$ character verification at every step. Consequently, exact RK degenerates to $O(n \cdot m)$ time ($1637\,\mu\text{s}$ at $n=3000$), exceeding naive search due to rolling hash arithmetic overhead on top of character comparisons. In production systems with 64-bit prime moduli (e.g. $2^{61}-1$), the collision probability is vanishingly small ($\approx 10^{-18}$), ensuring expected $O(n + m)$ runtime.
4. **Known collision test:** The input $text = \text{"bc\_ek\_bc"}, pattern = \text{"bc"}$ serves as a verification test: both `"bc"` and `"ek"` hash to $6 \pmod{101}$, verifying that the algorithm correctly discards false positive candidates.

---

## Sources and AI assistance

- **Course materials:** DAA Syllabus weeks 8–10, Lecture 8 slides (Dynamic Programming patterns), Lecture 9 slides (Pattern matching: KMP and Rabin-Karp), Lecture 10 slides (Suffix Arrays, Kasai LCP, Aho-Corasick automaton), and course algorithm notes (`LECTURE_CLARIFICATIONS_EN_RU.md`).
- **AI assistance disclosure:** An AI assistant (LLM) was used minimally as an editing and formatting aid:
  - Assisting with the formatting and layout of markdown tables in `ANALYSIS.md`.
  - Grammar proofreading of English technical explanations.
  - Formulating benchmarking measurement scripts.
- **Independent student implementation & verification:** All 8 core algorithms in `Java/source/Algorithms.java` (`lcs`, `merge`, `tree`, `naive`, `kmp`, `rk`, `suffix`, `aho`) were implemented, verified, and debugged by the student. The algorithmic logic (handling tree DP via iterative BFS to avoid recursion stack overflow, reverse BFS visit propagation for Aho-Corasick, polynomial rolling hash modulo 101 character verification, and suffix array rank inversion) was verified against all course contracts and passes all 44 public checks and student test fixtures.
