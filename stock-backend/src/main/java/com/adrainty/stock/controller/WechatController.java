package com.adrainty.stock.controller;

import com.adrainty.stock.util.WechatUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信消息接收控制器
 * <p>
 * 用于接收微信服务器推送的消息和事件
 * </p>
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Tag(name = "微信消息接收", description = "接收微信服务器推送的消息和事件")
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WechatController {

    private final WechatUtil wechatUtil;

    /**
     * 微信回调接口
     * 微信开放平台会携带 code 和 state 参数回调此接口
     */
    @Operation(hidden = true)
    @RequestMapping("/callback")
    public String wxCallback(HttpServletRequest request) {
        String signature = request.getParameter(WechatUtil.SIGNATURE);
        String timestamp = request.getParameter(WechatUtil.TIMESTAMP);
        String nonce = request.getParameter(WechatUtil.NONCE);

        wechatUtil.checkSignature(timestamp, nonce, signature);
        String echostr = request.getParameter(WechatUtil.ECHO_STR);
        if (StringUtils.isNotBlank(echostr)) {
            // 说明是一个仅仅用来验证的请求，回显echostr
            log.info("Echo str: {}", echostr);
            return echostr;
        }

        return wechatUtil.handleWxCallback(request);
    }

}
