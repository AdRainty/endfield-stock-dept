package com.adrainty.stock.wechat.impl;

import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.enums.UserRole;
import com.adrainty.stock.mapper.ExchangeMapper;
import com.adrainty.stock.mapper.UserMapper;
import com.adrainty.stock.service.CapitalService;
import com.adrainty.stock.service.PositionService;
import com.adrainty.stock.util.WechatUtil;
import com.adrainty.stock.wechat.IWechatMsgHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>EventWechatMsgHandler</p>
 *
 * @author AdRainty
 * @version V1.0.0
 * @description 微信事件消息处理器（处理扫码登录）
 * @since 2025/11/24 11:37
 */

@Slf4j
@Service
public class EventWechatMsgHandler implements IWechatMsgHandler {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CapitalService capitalService;

    @Resource
    private PositionService positionService;

    @Resource
    private ExchangeMapper exchangeMapper;

    @Value("${app.initial-capital:100000}")
    private BigDecimal initialCapital;

    private static final String REDIS_QR_PREFIX = "wx:qrcode:";
    private static final String WX_NICKNAME_PREFIX = "用户_";

    @Override
    @Transactional
    public WxMpXmlOutMessage doHandler(WxMpXmlMessage wxMessage) {
        String event = wxMessage.getEvent();
        if (WxConsts.EventType.SCAN.equals(event)) {
            String eventKey = wxMessage.getEventKey();
            String openId = wxMessage.getFromUser();

            log.info("用户扫码登录，eventKey: {}, openId: {}", eventKey, openId);
            handleQrCodeLogin(eventKey, openId);
        }

        return WxMpXmlOutMessage.TEXT()
                .content("登录成功，请在电脑上继续操作")
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .build();
    }

    /**
     * 处理二维码扫码登录
     */
    public void handleQrCodeLogin(String scene, String openId) {
        // 通过 sceneId 查找对应的 scene 字符串
        String redisKey = REDIS_QR_PREFIX + scene;
        WechatUtil.WxQrCodeScene qrScene = (WechatUtil.WxQrCodeScene) redisTemplate.opsForValue().get(redisKey);
        if (qrScene == null) {
            log.warn("二维码场景不存在或已过期：scene={}", scene);
            return;
        }

        // 查询用户是否存在
        User user = userMapper.findByWechatOpenid(openId);
        boolean newUser = false;

        if (user == null) {
            // 新用户注册
            newUser = true;

            // 检查是否为首位注册用户
            boolean isFirstUser = !userMapper.existsByWechatOpenid(openId) &&
                                   userMapper.selectCount(null) == 0;

            user = new User();
            user.setWechatOpenid(openId);
            user.setNickname(WX_NICKNAME_PREFIX + RandomStringUtils.randomAlphanumeric(8));
            user.setRole(isFirstUser ? UserRole.ADMIN : UserRole.USER);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("新用户注册：openid={}, nickname={}, role={}, isFirstUser={}",
                    openId, user.getNickname(), user.getRole(), isFirstUser);

            // 初始化资金和持仓
            initUserResources(user.getId());
        } else {
            log.info("用户登录：openid={}, nickname={}", openId, user.getNickname());
        }

        // 更新二维码状态为 SUCCESS
        qrScene.setStatus("SUCCESS");
        qrScene.setOpenid(openId);
        qrScene.setNickname(user.getNickname());
        qrScene.setAvatar(user.getAvatar());

        // 延长过期时间，给用户登录的时间
        redisTemplate.opsForValue().set(redisKey, qrScene, 10, TimeUnit.MINUTES);

        log.info("微信扫码成功，scene={}, openid={}, nickname={}, newUser={}",
                scene, openId, user.getNickname(), newUser);
    }

    /**
     * 初始化用户资源（资金和持仓）
     */
    private void initUserResources(Long userId) {
        // 获取所有交易所
        List<Exchange> exchanges = exchangeMapper.findByStatus(1);

        for (Exchange exchange : exchanges) {
            // 初始化资金（每个交易所 10W）
            capitalService.initCapital(userId, exchange.getId(), initialCapital);

            // 初始化持仓记录
            positionService.initPosition(userId, exchange.getId());
        }

        log.info("初始化用户资源完成：userId={}", userId);
    }

    @Override
    public String supportMsgType() {
        return WxConsts.XmlMsgType.EVENT;
    }

}
