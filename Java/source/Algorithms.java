import java.util.*;

public final class Algorithms {

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("length", 0);
            res.put("sequence", "");
            return res;
        }

        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i][j + 1], dp[i + 1][j]);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        int i = n;
        int j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        String sequence = sb.reverse().toString();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("length", dp[n][m]);
        res.put("sequence", sequence);
        return res;
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> rawSizes = (List<?>) input.get("sizes");
        if (rawSizes == null || rawSizes.isEmpty()) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("cost", 0L);
            res.put("plan", new ArrayList<>());
            return res;
        }

        int n = rawSizes.size();
        long[] sizes = new long[n];
        for (int i = 0; i < n; i++) {
            sizes[i] = ((Number) rawSizes.get(i)).longValue();
        }

        if (n == 1) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("cost", 0L);
            res.put("plan", new ArrayList<>());
            return res;
        }

        long[] pref = new long[n + 1];
        for (int i = 0; i < n; i++) {
            pref[i + 1] = pref[i] + sizes[i];
        }

        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];

        for (int len = 2; len <= n; len++) {
            for (int l = 0; l <= n - len; l++) {
                int r = l + len - 1;
                dp[l][r] = Long.MAX_VALUE;
                long sum = pref[r + 1] - pref[l];
                for (int k = l; k < r; k++) {
                    long cost = dp[l][k] + dp[k + 1][r] + sum;
                    if (cost < dp[l][r]) {
                        dp[l][r] = cost;
                        split[l][r] = k;
                    }
                }
            }
        }

        List<List<Integer>> plan = new ArrayList<>();
        buildMergePlan(0, n - 1, split, plan);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("cost", dp[0][n - 1]);
        res.put("plan", plan);
        return res;
    }

    private static void buildMergePlan(int l, int r, int[][] split, List<List<Integer>> plan) {
        if (l >= r) {
            return;
        }
        int k = split[l][r];
        buildMergePlan(l, k, split, plan);
        buildMergePlan(k + 1, r, split, plan);
        plan.add(Arrays.asList(l, k, r));
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        if (n <= 0) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("weight", 0L);
            res.put("selected", new ArrayList<Integer>());
            return res;
        }

        List<?> rawWeights = (List<?>) input.get("weights");
        long[] weights = new long[n];
        for (int i = 0; i < n; i++) {
            weights[i] = ((Number) rawWeights.get(i)).longValue();
        }

        List<?> rawEdges = (List<?>) input.get("edges");
        List<Integer>[] adj = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new ArrayList<>();
        }
        if (rawEdges != null) {
            for (Object eo : rawEdges) {
                List<?> e = (List<?>) eo;
                int u = ((Number) e.get(0)).intValue();
                int v = ((Number) e.get(1)).intValue();
                adj[u].add(v);
                adj[v].add(u);
            }
        }

        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        int[] order = new int[n];
        boolean[] visited = new boolean[n];
        int head = 0;
        int tail = 0;

        order[tail++] = 0;
        visited[0] = true;

        while (head < tail) {
            int u = order[head++];
            for (int v : adj[u]) {
                if (!visited[v]) {
                    visited[v] = true;
                    parent[v] = u;
                    order[tail++] = v;
                }
            }
        }

        long[] take = new long[n];
        long[] skip = new long[n];

        for (int idx = n - 1; idx >= 0; idx--) {
            int u = order[idx];
            take[u] = weights[u];
            skip[u] = 0;
            for (int v : adj[u]) {
                if (v != parent[u]) {
                    take[u] += skip[v];
                    skip[u] += Math.max(0L, Math.max(take[v], skip[v]));
                }
            }
        }

        boolean[] chooseTake = new boolean[n];
        long rootOpt = Math.max(0L, Math.max(take[0], skip[0]));
        if (rootOpt > 0 && take[0] > skip[0]) {
            chooseTake[0] = true;
        }

        for (int idx = 0; idx < n; idx++) {
            int u = order[idx];
            for (int v : adj[u]) {
                if (v != parent[u]) {
                    if (chooseTake[u]) {
                        chooseTake[v] = false;
                    } else {
                        if (take[v] > skip[v] && take[v] > 0) {
                            chooseTake[v] = true;
                        } else {
                            chooseTake[v] = false;
                        }
                    }
                }
            }
        }

        List<Integer> selected = new ArrayList<>();
        long totalWeight = 0;
        for (int i = 0; i < n; i++) {
            if (chooseTake[i]) {
                selected.add(i);
                totalWeight += weights[i];
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("weight", totalWeight);
        res.put("selected", selected);
        return res;
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        if (text == null) text = "";
        if (pattern == null) pattern = "";

        int n = text.length();
        int m = pattern.length();
        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("positions", positions);
            return res;
        }

        if (m <= n) {
            for (int i = 0; i <= n - m; i++) {
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    positions.add(i);
                }
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("positions", positions);
        return res;
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        if (text == null) text = "";
        if (pattern == null) pattern = "";

        int n = text.length();
        int m = pattern.length();
        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("positions", positions);
            return res;
        }

        if (m > n) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("positions", positions);
            return res;
        }

        int[] pi = new int[m];
        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            pi[i] = j;
        }

        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                positions.add(i - m + 1);
                j = pi[m - 1];
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("positions", positions);
        return res;
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        if (text == null) text = "";
        if (pattern == null) pattern = "";

        int n = text.length();
        int m = pattern.length();
        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("positions", positions);
            return res;
        }

        if (m > n) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("positions", positions);
            return res;
        }

        long base = 31;
        long mod = 101;

        long patternHash = 0;
        long windowHash = 0;
        long power = 1;

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            windowHash = (windowHash * base + text.charAt(i)) % mod;
            if (i < m - 1) {
                power = (power * base) % mod;
            }
        }

        for (int i = 0; i <= n - m; i++) {
            if (windowHash == patternHash) {
                boolean match = true;
                for (int k = 0; k < m; k++) {
                    if (text.charAt(i + k) != pattern.charAt(k)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    positions.add(i);
                }
            }

            if (i < n - m) {
                long removeVal = (text.charAt(i) * power) % mod;
                windowHash = (windowHash - removeVal + mod) % mod;
                windowHash = (windowHash * base + text.charAt(i + m)) % mod;
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("positions", positions);
        return res;
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        if (text == null || text.isEmpty()) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("sa", new ArrayList<Integer>());
            res.put("lcp", new ArrayList<Integer>());
            res.put("repeated", "");
            return res;
        }

        int n = text.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        int[] nextRank = new int[n];

        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = (int) text.charAt(i);
        }

        for (int k = 1; k < n; k <<= 1) {
            final int step = k;
            final int[] currentRank = rank;

            Arrays.sort(sa, (a, b) -> {
                if (currentRank[a] != currentRank[b]) {
                    return Integer.compare(currentRank[a], currentRank[b]);
                }
                int rankA = (a + step < n) ? currentRank[a + step] : -1;
                int rankB = (b + step < n) ? currentRank[b + step] : -1;
                return Integer.compare(rankA, rankB);
            });

            nextRank[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int prev = sa[i - 1];
                int curr = sa[i];
                int prevSecond = (prev + step < n) ? currentRank[prev + step] : -1;
                int currSecond = (curr + step < n) ? currentRank[curr + step] : -1;

                if (currentRank[curr] == currentRank[prev] && currSecond == prevSecond) {
                    nextRank[curr] = nextRank[prev];
                } else {
                    nextRank[curr] = nextRank[prev] + 1;
                }
            }

            System.arraycopy(nextRank, 0, rank, 0, n);
            if (rank[sa[n - 1]] == n - 1) {
                break;
            }
        }

        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int[] lcp = new int[n];
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int prev = sa[rank[i] - 1];
                while (i + h < n && prev + h < n && text.charAt(i + h) == text.charAt(prev + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            }
        }

        int maxLcp = 0;
        int bestStart = -1;
        for (int i = 1; i < n; i++) {
            if (lcp[i] > maxLcp) {
                maxLcp = lcp[i];
                bestStart = sa[i];
            }
        }

        String repeated = "";
        if (maxLcp > 0 && bestStart != -1) {
            repeated = text.substring(bestStart, bestStart + maxLcp);
        }

        List<Integer> saList = new ArrayList<>(n);
        List<Integer> lcpList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            saList.add(sa[i]);
            lcpList.add(lcp[i]);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("sa", saList);
        res.put("lcp", lcpList);
        res.put("repeated", repeated);
        return res;
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> rawTexts = (List<?>) input.get("texts");
        List<?> rawPatterns = (List<?>) input.get("patterns");

        if (rawTexts == null || rawTexts.isEmpty()) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("counts", new ArrayList<>());
            return res;
        }

        int numTexts = rawTexts.size();
        if (rawPatterns == null || rawPatterns.isEmpty()) {
            List<List<Long>> counts = new ArrayList<>();
            for (int i = 0; i < numTexts; i++) {
                counts.add(new ArrayList<>());
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("counts", counts);
            return res;
        }

        int p = rawPatterns.size();
        String[] patterns = new String[p];
        int totalLen = 0;
        for (int i = 0; i < p; i++) {
            patterns[i] = (String) rawPatterns.get(i);
            totalLen += patterns[i].length();
        }

        int maxStates = totalLen + 2;
        int[][] next = new int[maxStates][95];
        int[] fail = new int[maxStates];
        int[] endNode = new int[p];
        int stateCount = 1;

        for (int i = 0; i < p; i++) {
            String pat = patterns[i];
            int curr = 0;
            for (int k = 0; k < pat.length(); k++) {
                int ch = pat.charAt(k) - 32;
                if (ch >= 0 && ch < 95) {
                    if (next[curr][ch] == 0) {
                        next[curr][ch] = stateCount++;
                    }
                    curr = next[curr][ch];
                }
            }
            endNode[i] = curr;
        }

        int[] bfsOrder = new int[stateCount];
        int head = 0;
        int tail = 0;

        for (int c = 0; c < 95; c++) {
            int child = next[0][c];
            if (child != 0) {
                fail[child] = 0;
                bfsOrder[tail++] = child;
            }
        }

        while (head < tail) {
            int u = bfsOrder[head++];
            for (int c = 0; c < 95; c++) {
                int child = next[u][c];
                if (child != 0) {
                    fail[child] = next[fail[u]][c];
                    bfsOrder[tail++] = child;
                } else {
                    next[u][c] = next[fail[u]][c];
                }
            }
        }

        List<List<Long>> counts = new ArrayList<>();

        for (Object to : rawTexts) {
            String text = (String) to;
            long[] visits = new long[stateCount];
            if (text != null && !text.isEmpty()) {
                int curr = 0;
                for (int i = 0; i < text.length(); i++) {
                    int ch = text.charAt(i) - 32;
                    if (ch >= 0 && ch < 95) {
                        curr = next[curr][ch];
                    } else {
                        curr = 0;
                    }
                    visits[curr]++;
                }
            }

            for (int i = tail - 1; i >= 0; i--) {
                int u = bfsOrder[i];
                visits[fail[u]] += visits[u];
            }

            List<Long> row = new ArrayList<>(p);
            for (int i = 0; i < p; i++) {
                row.add(visits[endNode[i]]);
            }
            counts.add(row);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("counts", counts);
        return res;
    }
}
