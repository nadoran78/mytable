package com.zerobase.mytable.service;

import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SendMessageServiceTest {

    @Autowired
    private SendMessageService sendMessageService;

    @Value(value = "${caller.number}")
    private String callerNumber;

    // 문자 발송 테스트
    @Test
    @Disabled
    void successSendMessage() {
        //given

        //when
        SingleMessageSentResponse response =
                sendMessageService.sendOneMessage(callerNumber, "test message");
        //then
        assertEquals("정상 접수(이통사로 접수 예정) ".trim(), response.getStatusMessage().trim());
    }
}