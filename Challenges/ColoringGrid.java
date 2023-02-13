import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

public class Solution {
    static InputStream is;
    static PrintWriter out;
    static String INPUT = "";
    
    static long h(int[] a)
    {
        long h = 0;
        for(int v : a)h = h * 1000000009 + v;
        return h;
    }
    
    static int mod = 1000000007;
    static void solve()
    {
        long[][][] co = new long[9][][];
        for(int nc = 1;nc <= 8;nc++){
            List<int[]> e = enumerate(nc);
            
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            for(int j = 0;j < e.size();j++){
                map.put(h(e.get(j)), j);
            }
            
            int n = e.size();
            int[] maxbit = new int[n];
            int[][] g = new int[n][];
            for(int j = 0;j < n;j++){
                int bit = 0;
                for(int k = 0;k < nc;k++){
                    bit |= 1<<e.get(j)[k];
                }
                maxbit[j] = Integer.numberOfTrailingZeros(Integer.highestOneBit(bit));
                g[j] = new int[maxbit[j]+2];
                for(int k = 0;k <= maxbit[j]+1;k++){
                    int[] sh = shift(e.get(j));
                    sh[sh.length-1] = k;
                    sh = relocate(sh);
                    g[j][k] = map.get(h(sh));
                }
            }
            
            List<List<Long>> ll = new ArrayList<List<Long>>();
            int inf = 10, sup = 10+nc*nc+2;
            for(int i = inf;i < sup;i++){
                ll.add(enumGo(i, maxbit, nc, e, g));
            }
            co[nc] = new long[nc+1][];
            for(int j = 1;j <= nc;j++){
                long[][] lco = new long[sup-inf][];
                for(int i = inf;i < sup;i++){
                    lco[i-inf] = new long[]{i, ll.get(i-inf).get(j-1)};
                }
                co[nc][j] = rstrip(guessR(lco));
            }
        }
        for(int T = ni();T >= 1;T--){
            int n = ni(), m = ni(), K = ni();
            if(n < m){
                int d = n; n = m; m = d;
            }
            // n >= m
            long ret = 0;
            long[] f = co[n][m];
            for(int i = f.length-1;i >= 0;i--){
                ret = (ret * K + f[i]) % mod;
            }
            out.println(ret);
        }
    }
    
    public static long[] rstrip(long[] a)
    {
        int i;
        for(i = a.length-1;i >= 0 && a[i] == 0;i--);
        return i == a.length-1 ? a : Arrays.copyOf(a, i+1);
    }
    
    public static long[] guessR(long[][] co)
    {
        int n = co.length;
        long[] dp = new long[n+1];
        dp[0] = 1;
        // (x-x0)(x-x1)...(x-x{n-1})
        for(int i = 0;i < n;i++){
            for(int j = i;j >= 0;j--){
                dp[j+1] += dp[j];
                if(dp[j+1] >= mod)dp[j+1] -= mod;
                dp[j] *= mod-co[i][0];
                dp[j] %= mod;
            }
        }
        
        long[] ret = new long[n];
        for(int i = 0;i < n;i++){
            long den = 1;
            for(int j = 0;j < n;j++){
                if(i != j){
                    den *= co[i][0]-co[j][0]+mod;
                    den %= mod;
                }
            }
            
            long iden = invl(den, mod);
            long minus = 0;
            for(int j = n-1;j >= 0;j--){
                minus = (dp[j+1] + minus * co[i][0]) % mod;
                ret[j] += minus*iden%mod*co[i][1]%mod;
            }
        }
        for(int i = 0;i < n;i++)ret[i] %= mod;
        return ret;
    }
    
    public static long invl(long a, long mod)
    {
        long b = mod;
        long p = 1, q = 0;
        while(b > 0){
            long c = a / b;
            long d;
            d = a; a = b; b = d % b;
            d = p; p = q; q = d - c * q;
        }
        return p < 0 ? p + mod : p;
    }
    
    static class Info
    {
        public List<int[]> e;
        public int[][] g;
        public int[] maxbit;
    }
    
    static List<Long> enumGo(long K, int[] maxbit, int nc, List<int[]> e, int[][] g)
    {
        int n = e.size();
        long[] dp = new long[n];
        outer:
        for(int i = 0;i < n;i++){
            int[] src = e.get(i);
            for(int j = 0;j < nc-1;j++){
                if(src[j] == src[j+1])continue outer;
            }
            dp[i] = 1;
            for(int j = 0;j <= maxbit[i];j++){
                dp[i] = dp[i] * (K-j) % mod;
            }
        }
        List<Long> vs = new ArrayList<Long>();
        for(int j = nc;j < nc*nc;j++){
            if(j % nc == 0){
                long ret = 0;
                for(int i = 0;i < n;i++){
                    ret += dp[i];
                }
                vs.add(ret%mod);
            }
            long[] ndp = new long[n];
            boolean lf = j%nc == 0;
            for(int k = 0;k < n;k++){
                if(dp[k] == 0)continue;
                int[] src = e.get(k);
                for(int L = 0;L <= maxbit[k]+1;L++){
                    if(src[0] == L)continue;
                    if(!lf && src[nc-1] == L)continue;
                    if(L <= maxbit[k]){
                        ndp[g[k][L]] += dp[k];
                        if(ndp[g[k][L]] >= mod)ndp[g[k][L]] -= mod;
                    }else if(K-(maxbit[k]+1) >= 1){
                        ndp[g[k][L]] += dp[k] * (K-(maxbit[k]+1));
                        ndp[g[k][L]] %= mod;
                    }
                }
            }
            dp = ndp;
//            tr(j, dp);
        }
        long ret = 0;
        for(int i = 0;i < n;i++){
            ret += dp[i];
        }
        vs.add(ret%mod);
        return vs;
    }
    
    static int[] shift(final int[] a)
    {
        int[] ret = new int[a.length];
        for(int l = 1;l < a.length;l++){
            ret[l-1] = a[l];
        }
        return ret;
    }
    
    static int[] relocate(int[] a)
    {
        int[] map = new int[9];
        Arrays.fill(map, -1);
        int p = 0;
        for(int i = 0;i < a.length;i++){
            if(a[i] == -1)continue;
            if(map[a[i]] == -1){
                map[a[i]] = p++;
            }
            a[i] = map[a[i]];
        }
        return a;
    }
    
    public static List<int[]> enumerate(int n)
    {
        List<int[]> packs = new ArrayList<int[]>();
        dfs(0, 0, new int[n], packs);
        return packs;
    }
    
    public static void dfs(int cur, int sup, int[] a, List<int[]> packs)
    {
        if(cur == a.length){
            packs.add(Arrays.copyOf(a, a.length));
            return;
        }
        
        for(int i = 0;i < sup;i++){
            a[cur] = i;
            dfs(cur+1, sup, a, packs);
        }
        a[cur] = sup;
        dfs(cur+1, sup+1, a, packs);
    }
    
    public static void main(String[] args) throws Exception
    {
        long S = System.currentTimeMillis();
        is = INPUT.isEmpty() ? System.in : new ByteArrayInputStream(INPUT.getBytes());
        out = new PrintWriter(System.out);
        
        solve();
        out.flush();
        long G = System.currentTimeMillis();
        tr(G-S+"ms");
    }
    
    private static boolean eof()
    {
        if(lenbuf == -1)return true;
        int lptr = ptrbuf;
        while(lptr < lenbuf)if(!isSpaceChar(inbuf[lptr++]))return false;
        
        try {
            is.mark(1000);
            while(true){
                int b = is.read();
                if(b == -1){
                    is.reset();
                    return true;
                }else if(!isSpaceChar(b)){
                    is.reset();
                    return false;
                }
            }
        } catch (IOException e) {
            return true;
        }
    }
    
    private static byte[] inbuf = new byte[1024];
    static int lenbuf = 0, ptrbuf = 0;
    
    private static int readByte()
    {
        if(lenbuf == -1)throw new InputMismatchException();
        if(ptrbuf >= lenbuf){
            ptrbuf = 0;
            try { lenbuf = is.read(inbuf); } catch (IOException e) { throw new InputMismatchException(); }
            if(lenbuf <= 0)return -1;
        }
        return inbuf[ptrbuf++];
    }
    
    private static boolean isSpaceChar(int c) { return !(c >= 33 && c <= 126); }
    private static int skip() { int b; while((b = readByte()) != -1 && isSpaceChar(b)); return b; }
    
    private static double nd() { return Double.parseDouble(ns()); }
    private static char nc() { return (char)skip(); }
    
    private static String ns()
    {
        int b = skip();
        StringBuilder sb = new StringBuilder();
        while(!(isSpaceChar(b))){ // when nextLine, (isSpaceChar(b) && b != ' ')
            sb.appendCodePoint(b);
            b = readByte();
        }
        return sb.toString();
    }
    
    private static char[] ns(int n)
    {
        char[] buf = new char[n];
        int b = skip(), p = 0;
        while(p < n && !(isSpaceChar(b))){
            buf[p++] = (char)b;
            b = readByte();
        }
        return n == p ? buf : Arrays.copyOf(buf, p);
    }
    
    private static char[][] nm(int n, int m)
    {
        char[][] map = new char[n][];
        for(int i = 0;i < n;i++)map[i] = ns(m);
        return map;
    }
    
    private static int[] na(int n)
    {
        int[] a = new int[n];
        for(int i = 0;i < n;i++)a[i] = ni();
        return a;
    }
    
    private static int ni()
    {
        int num = 0, b;
        boolean minus = false;
        while((b = readByte()) != -1 && !((b >= '0' && b <= '9') || b == '-'));
        if(b == '-'){
            minus = true;
            b = readByte();
        }
        
        while(true){
            if(b >= '0' && b <= '9'){
                num = num * 10 + (b - '0');
            }else{
                return minus ? -num : num;
            }
            b = readByte();
        }
    }
    
    private static long nl()
    {
        long num = 0;
        int b;
        boolean minus = false;
        while((b = readByte()) != -1 && !((b >= '0' && b <= '9') || b == '-'));
        if(b == '-'){
            minus = true;
            b = readByte();
        }
        
        while(true){
            if(b >= '0' && b <= '9'){
                num = num * 10 + (b - '0');
            }else{
                return minus ? -num : num;
            }
            b = readByte();
        }
    }
    
    private static void tr(Object... o) { if(INPUT.length() != 0)System.out.println(Arrays.deepToString(o)); }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
Calculate the number of ways to color an N * M grid using K colors. Adjacent squares in the grid should have different colors. 
Squares are considered adjacent if they share an edge.

Input Format
The first line contains an integer T denoting the number of test-cases. The next T lines contains integers N, M and K separated by a single space.

Output Format
Output T lines, one for each test case containing the number of ways modulo 109+7.

Constraints
1 <= T <= 105
1 <= N,M <= 8
1 <= K <= 109

Sample Input

3
3 3 2
3 4 3
1 1 1
Sample Output

2
1122
1
Explanation
For the first case, there are two ways to color the grid. The colorings are in a chessboard pattern with either color at the top right square.

*/
