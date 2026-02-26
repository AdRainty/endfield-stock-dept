package com.adrainty.stock.wechat.impl;

import com.adrainty.stock.wechat.IWechatMsgHandler;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Service;

/**
 * <p>TextWechatMsgHandler</p>
 *
 * @author AdRainty
 * @version V1.0.0
 * @description 微信消息处理
 * @since 2025/11/24 11:41:00
 */

@Slf4j
@Service
public class TextWechatMsgHandler implements IWechatMsgHandler {

    @Override
    public WxMpXmlOutMessage doHandler(WxMpXmlMessage wxMessage) {
        log.info("接收到微信文本消息: {}", wxMessage);
        return WxMpXmlOutMessage.TEXT()
                .content("你好: " + wxMessage.getContent())
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .build();
    }

    @Override
    public String supportMsgType() {
        return WxConsts.XmlMsgType.TEXT;
    }
}
