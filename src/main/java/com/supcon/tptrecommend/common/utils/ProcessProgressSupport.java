package com.supcon.tptrecommend.common.utils;


import com.supcon.tptrecommend.dto.FileParse.FileParseProgressResp;
import com.supcon.tptrecommend.integration.ws.WebsocketPush;
import com.supcon.tptrecommend.manager.strategy.impl.ExcelFileAnalysishandle;

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

        int totalMaxIncrement = endCap - start;
        int totalIncrement = totalMaxIncrement - new Random().nextInt(2) - 1;

        List<Integer> increments = generateRandomSteps(totalIncrement, steps);

        AtomicInteger stepIndex = new AtomicInteger(0);
        AtomicInteger currentProgress = new AtomicInteger(start);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Set<Long> cache = ExcelFileAnalysishandle.STOP_SIGNAL_CACHE;
        scheduler.scheduleAtFixedRate(() -> {
            if (cache.contains(fileId)) {
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
                if (cache.contains(fileId)) {
                    cache.remove(fileId);
                    scheduler.shutdown();
                    return;
                }
                FileParseProgressResp data = FileParseProgressResp.builder()
                    .fileId(fileId)
                    .parseProgress(progress)
                    .build();
                WebsocketPush.pushMessage(data);
            } else {
                cache.remove(fileId);
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
            .fileId(fileId)
            .parseProgress(100)
            .build();
        WebsocketPush.pushMessage(data);
    }

    public static void notifyParseProcessing(Long fileId, Integer progress) {
        FileParseProgressResp data = FileParseProgressResp.builder()
            .fileId(fileId)
            .parseProgress(progress)
            .build();
        WebsocketPush.pushMessage(data);
    }

    /**
     * 用于统计excel或者csv文件处理进度
     * 注意：根据起始值计算当前进度
     *
     * @param processedRows 已处理的行数
     * @param totalRows     总行数
     * @param startProgress 起始进度百分比 (例如 20)
     * @return 当前的整体进度百分比 (例如 20-100之间)
     */
    public static int calculateFromStartProgress(long processedRows, long totalRows, int startProgress) {
        if (totalRows == 0) {
            // 如果总行数为0，直接返回最终进度100
            return 99;
        }
        if (processedRows >= totalRows) {
            // 不返回100 ，只有当最后一批数据处理完成时才返回100
            return 99;
        }

        // 计算在 (startProgress -> 100) 区间内的进度
        double taskProgress = (double) processedRows / totalRows;
        double progressRange = 99 - startProgress;

        //当前总进度 = 起始进度 + (已处理行数 / 总行数) * (100 - 起始进度)
        return (int) (startProgress + taskProgress * progressRange);
    }

   


}
