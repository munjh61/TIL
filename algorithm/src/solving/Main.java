package solving;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int N = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());
        int[] weight = new int[N];
        int[] cost = new int[N];
        for(int i = 0 ; i < N ; i ++){
            st = new StringTokenizer(br.readLine());
            weight[i] = Integer.parseInt(st.nextToken());
            cost[i] = Integer.parseInt(st.nextToken());
        }
        br.close();
        // --- 입력 종료 ---
        int[] best = new int [K+1];
        for(int curW = 1 ; curW <= K ; curW++){
            best[curW] = best[curW-1];
            for(int j = 0 ; j < N ; j ++){
                if(curW - weight[j] < 0) continue;
                int takeOutAndPutNew = best[curW - weight[j]] + cost[j];
                if(takeOutAndPutNew >= best[curW]){
                    best[curW] = takeOutAndPutNew;
                }
            }
        }
        System.out.println(best[K]);
    }
}