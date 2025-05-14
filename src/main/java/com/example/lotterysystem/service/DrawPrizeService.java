package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;

import java.util.List;

public interface DrawPrizeService {
    void drawPrize(DrawPrizeParam param);
    boolean checkDrawPrizeParam(DrawPrizeParam param);

    List<WinningRecordDO> saveWinnerRecords(DrawPrizeParam param);

    void deleteRecords(Long activityId, Long prizeId);

    List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param);
}
