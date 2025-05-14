package com.example.lotterysystem;

import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailTest {
    @Autowired
    MailUtil mailUtil;

    @Test
    void sendMail(){
        String context ="";

            context = "Hi，" + "小尾尾" + "。恭喜你在"
                    + "谁是世界第一可爱" + "活动中获得"
                    + "一等奖"
                    + "。获奖时间为"
                    + "2025-04-30" + "，请尽快领 取您的奖励！";
            mailUtil.sendSampleMail("740150989@qq.com",
                    "中奖通知", context);

    }

}
