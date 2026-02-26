package com.adrainty.stock.wechat;

import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * <p>IWechatMsgHandler</p>
 *
 * @author AdRainty
 * @version V1.0.0
 * @description 微信消息处理
 * @since 2025/11/24 11:36:13
 */
public interface IWechatMsgHandler {

    WxMpXmlOutMessage doHandler(WxMpXmlMessage wxMpXmlMessage);

    String supportMsgType();

}
