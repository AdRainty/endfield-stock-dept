package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.OrderDTO;
import com.adrainty.stock.dto.PlaceOrderRequest;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.Order;
import com.adrainty.stock.entity.UserPosition;
import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import com.adrainty.stock.exception.BusinessException;
import com.adrainty.stock.repository.InstrumentRepository;
import com.adrainty.stock.repository.OrderRepository;
import com.adrainty.stock.repository.UserPositionRepository;
import com.adrainty.stock.service.OrderBookService;
import com.adrainty.stock.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final InstrumentRepository instrumentRepository;
    private final UserPositionRepository userPositionRepository;
    private final OrderBookService orderBookService;
    private final CapitalServiceImpl capitalService;
    
    @Override
    @Transactional
    public OrderDTO placeOrder(Long userId, PlaceOrderRequest request) {
        Instrument instrument = instrumentRepository.findByInstrumentCode(request.getInstrumentCode())
            .orElseThrow(() -> BusinessException.of("品种不存在"));
        
        if (!instrument.getExchangeId().equals(request.getExchangeId())) {
            throw BusinessException.of("品种与交易所不匹配");
        }
        
        BigDecimal totalAmount = request.getPrice().multiply(request.getQuantity());
        
        if (request.getOrderType() == OrderType.BUY) {
            BigDecimal available = capitalService.getAvailableCapital(userId, request.getExchangeId());
            if (available.compareTo(totalAmount) < 0) {
                throw BusinessException.of("资金不足，可用：" + available + ", 需要：" + totalAmount);
            }
        } else {
            UserPosition position = userPositionRepository
                .findByUserIdAndExchangeIdAndInstrumentCode(userId, request.getExchangeId(), request.getInstrumentCode())
                .orElse(null);
            
            if (position == null || position.getAvailableQuantity().compareTo(request.getQuantity()) < 0) {
                throw BusinessException.of("持仓不足");
            }
        }
        
        Order order = new Order();
        order.setOrderNo("ORD_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setUserId(userId);
        order.setExchangeId(request.getExchangeId());
        order.setInstrumentCode(request.getInstrumentCode());
        order.setOrderType(request.getOrderType());
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setUnfilledQuantity(request.getQuantity());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());
        
        orderRepository.save(order);
        
        if (request.getOrderType() == OrderType.BUY) {
            capitalService.freezeCapital(userId, request.getExchangeId(), totalAmount, order.getOrderNo());
        } else {
            freezePosition(userId, request.getExchangeId(), request.getInstrumentCode(), request.getQuantity());
        }
        
        if (request.getOrderType() == OrderType.BUY) {
            orderBookService.addBidOrder(request.getExchangeId(), request.getInstrumentCode(), 
                request.getPrice(), request.getQuantity(), order.getId());
        } else {
            orderBookService.addAskOrder(request.getExchangeId(), request.getInstrumentCode(), 
                request.getPrice(), request.getQuantity(), order.getId());
        }
        
        log.info("下单成功：userId={}, orderNo={}, type={}, price={}, quantity={}", 
            userId, order.getOrderNo(), request.getOrderType(), request.getPrice(), request.getQuantity());
        
        return convertToDTO(order, instrument);
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> BusinessException.of("订单不存在"));
        
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.of("无权操作该订单");
        }
        
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELLED) {
            throw BusinessException.of("该订单无法撤单");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledTime(LocalDateTime.now());
        order.setCancelReason("用户撤单");
        
        if (order.getOrderType() == OrderType.BUY) {
            BigDecimal remainingAmount = order.getPrice().multiply(order.getUnfilledQuantity());
            capitalService.unfreezeCapital(userId, order.getExchangeId(), remainingAmount, order.getOrderNo());
        } else {
            unfreezePosition(userId, order.getExchangeId(), order.getInstrumentCode(), order.getUnfilledQuantity());
        }
        
        orderBookService.removeOrder(order.getExchangeId(), order.getInstrumentCode(), 
            order.getId(), order.getUnfilledQuantity());
        
        orderRepository.save(order);
        
        log.info("撤单成功：userId={}, orderNo={}", userId, orderNo);
        return true;
    }
    
    @Override
    public List<OrderDTO> getUserOrders(Long userId, String status) {
        List<Order> orders;
        
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            orders = orderRepository.findByUserIdAndStatusOrderByOrderTimeDesc(userId, orderStatus);
        } else {
            orders = orderRepository.findByUserIdOrderByOrderTimeDesc(userId);
        }
        
        return orders.stream()
            .map(o -> convertToDTO(o, instrumentRepository.findByInstrumentCode(o.getInstrumentCode()).orElse(null)))
            .collect(Collectors.toList());
    }
    
    @Override
    public OrderDTO getOrderDetail(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> BusinessException.of("订单不存在"));
        
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.of("无权查看该订单");
        }
        
        Instrument instrument = instrumentRepository.findByInstrumentCode(order.getInstrumentCode()).orElse(null);
        return convertToDTO(order, instrument);
    }
    
    private void freezePosition(Long userId, Long exchangeId, String instrumentCode, BigDecimal quantity) {
        UserPosition position = userPositionRepository
            .findByUserIdAndExchangeIdAndInstrumentCode(userId, exchangeId, instrumentCode)
            .orElse(null);
        
        if (position != null) {
            position.setAvailableQuantity(position.getAvailableQuantity().subtract(quantity));
            position.setFrozenQuantity(position.getFrozenQuantity().add(quantity));
            userPositionRepository.save(position);
        }
    }
    
    private void unfreezePosition(Long userId, Long exchangeId, String instrumentCode, BigDecimal quantity) {
        UserPosition position = userPositionRepository
            .findByUserIdAndExchangeIdAndInstrumentCode(userId, exchangeId, instrumentCode)
            .orElse(null);
        
        if (position != null) {
            position.setAvailableQuantity(position.getAvailableQuantity().add(quantity));
            position.setFrozenQuantity(position.getFrozenQuantity().subtract(quantity));
            userPositionRepository.save(position);
        }
    }
    
    private OrderDTO convertToDTO(Order order, Instrument instrument) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setExchangeId(order.getExchangeId());
        dto.setExchangeName(order.getExchangeId().toString());
        dto.setInstrumentCode(order.getInstrumentCode());
        dto.setInstrumentName(instrument != null ? instrument.getName() : order.getInstrumentCode());
        dto.setOrderType(order.getOrderType());
        dto.setPrice(order.getPrice());
        dto.setQuantity(order.getQuantity());
        dto.setFilledQuantity(order.getFilledQuantity());
        dto.setUnfilledQuantity(order.getUnfilledQuantity());
        dto.setFilledAmount(order.getFilledAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderTime(order.getOrderTime());
        dto.setFilledTime(order.getFilledTime());
        return dto;
    }
}
