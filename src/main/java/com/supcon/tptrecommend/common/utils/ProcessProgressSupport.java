package com.supcon.tptrecommend.common.utils;

import com.supcon.systemcomponent.websocket.WebSocketSender;
import com.supcon.systemcomponent.websocket.message.JsonMessageDO;
import com.supcon.tptrecommend.dto.FileParse.FileParseProgressResp;
import com.supcon.tptrecommend.feign.entity.FileParseResp;
import com.supcon.tptrecommend.manager.impl.FileManagerImpl;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessProgressSupport {

    public static void notifyProcessProgress(Long fileId) {
        int start = 0;
        int endCap = 99;  // 最大不能达到 100
        int durationMs = 6000;
        int intervalMs = 600;
        int steps = durationMs / intervalMs;

        int totalMaxIncrement = endCap - start;  // 99 - 10 = 89
        int totalIncrement = totalMaxIncrement - new Random().nextInt(2) - 1; // 例如最多用到 88

        List<Integer> increments = generateRandomSteps(totalIncrement, steps);

        AtomicInteger stepIndex = new AtomicInteger(0);
        AtomicInteger currentProgress = new AtomicInteger(start);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Map<Long, FileParseResp> cache = FileManagerImpl.CACHE;
            if(cache.containsKey(fileId)){
                cache.remove(fileId);
                scheduler.shutdown();
                return;
            }
            int i = stepIndex.getAndIncrement();
            if (i < increments.size()) {
                int progress = currentProgress.addAndGet(increments.get(i));
                if (progress >= 100) {
                    progress = 99; // 强制不超过
                }

                // 推送之前在校验一次
                if(cache.containsKey(fileId)){
                    cache.remove(fileId);
                    scheduler.shutdown();
                    return;
                }
                FileParseProgressResp data = FileParseProgressResp.builder()
                    .parseProgress(progress)
                    .build();
                WebSocketSender.sendByKey(fileId.toString(), JsonMessageDO.data(null, data));
            }else {
                scheduler.shutdown();
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }
    /**
     * 将 totalSum 拆分成 n 个正整数，和为 totalSum。
     */
    private static List<Integer> generateRandomSteps(int totalSum, int n) {
        Random rand = new Random();
        int[] cuts = new int[n - 1];

        // 生成 n-1 个随机分割点
        for (int i = 0; i < n - 1; i++) {
            cuts[i] = rand.nextInt(totalSum - 1) + 1;
        }
        Arrays.sort(cuts);

        List<Integer> steps = new ArrayList<>();
        int prev = 0;
        for (int cut : cuts) {
            steps.add(cut - prev);
            prev = cut;
        }
        steps.add(totalSum - prev);  // 最后一段

        return steps;
    }

    public static void notifyParseComplete(Long fileId) {
        FileParseProgressResp data = FileParseProgressResp.builder()
            .parseProgress(100)
            .build();
        WebSocketSender.sendByKey(fileId.toString(), JsonMessageDO.data(null, data));
    }



}
