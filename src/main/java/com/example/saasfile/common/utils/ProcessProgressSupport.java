package com.example.saasfile.common.utils;


import com.example.saasfile.dto.FileParse.FileParseProgressResp;
import com.example.saasfile.integration.ws.WebsocketPush;

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

    
    public static int calculateFromStartProgress(long processedRows, long totalRows, int startProgress) {
        if (totalRows == 0) {
            return 99;
        }
        if (processedRows >= totalRows) {
            return 99;
        }
        double taskProgress = (double) processedRows / totalRows;
        double progressRange = 99 - startProgress;
        return (int) (startProgress + taskProgress * progressRange);
    }

   


}
