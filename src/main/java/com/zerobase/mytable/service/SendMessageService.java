package com.zerobase.mytable.service;

import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendMessageService {
    // 메세지 전송 서비스를 위한 api_key, api_secrte 지정
    @Value(value = "${api.key}")
    private String apiKey;
    @Value(value = "${api.secret}")
    private String apiSecret;

    // 발신번호 따로 관리
    @Value(value = "${caller.number}")
    private String callerNumber;


    public SingleMessageSentResponse sendOneMessage(String phone, String text) {

        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey,
                apiSecret, "https://api.coolsms.co.kr");

        Message message = new Message();

        message.setFrom(callerNumber);
        message.setTo(phone.replace("-", ""));
        message.setText(text);

        return messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
