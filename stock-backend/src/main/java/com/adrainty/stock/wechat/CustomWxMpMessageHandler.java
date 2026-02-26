package com.adrainty.stock.wechat;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author AdRainty
 * @version V1.0.0
 * @since 2025/11/23 下午2:02
 */

@Slf4j
@Service
public class CustomWxMpMessageHandler implements WxMpMessageHandler {

    private final Map<String, IWechatMsgHandler> wechatMsgHandlerMap;

    public CustomWxMpMessageHandler(List<IWechatMsgHandler> wechatMsgHandlerList) {
        wechatMsgHandlerMap = wechatMsgHandlerList.stream()
                .collect(Collectors.toMap(IWechatMsgHandler::supportMsgType, handler -> handler));
    }

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService wxMpService, WxSessionManager sessionManager) {
        log.info("接收到微信加密消息: {}", wxMessage);
        String msgType = wxMessage.getMsgType();
        IWechatMsgHandler handler = wechatMsgHandlerMap.get(msgType);
        if (handler != null) {
            return handler.doHandler(wxMessage);
        }
        return null;
    }

}
