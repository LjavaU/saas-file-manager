package com.supcon.tptrecommend.common.utils;


import com.supcon.tptrecommend.dto.FileParse.FileParseProgressResp;
import com.supcon.tptrecommend.integration.ws.WebsocketPush;

public class ProcessProgressSupport {


    public static void notifyParseComplete(Long fileId,Long userId) {
        FileParseProgressResp data = FileParseProgressResp.builder()
            .fileId(fileId)
            .parseProgress(100)
            .build();
        WebsocketPush.pushMessage(userId.toString(),data);
    }

    public static void notifyParseProcessing(Long fileId, Long userId,Integer progress) {
        FileParseProgressResp data = FileParseProgressResp.builder()
            .fileId(fileId)
            .parseProgress(progress)
            .build();
        WebsocketPush.pushMessage(userId.toString(),data);
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
