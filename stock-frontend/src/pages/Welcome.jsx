import React from "react";
import { Card } from "antd";
import {
  ThunderboltOutlined,
  SafetyOutlined,
  DollarOutlined,
  LineChartOutlined,
  ClockCircleOutlined,
  WarningOutlined,
} from "@ant-design/icons";

const Welcome = () => {
  const rules = [
    {
      icon: <ThunderboltOutlined />,
      title: "系统概述",
      content: "终末地调度券交易模拟系统是一个模拟真实证券交易的虚拟交易平台。用户可以在多个交易所进行多种品种的买卖交易，体验真实的交易流程。",
    },
    {
      icon: <DollarOutlined />,
      title: "交易规则",
      items: [
        "初始资金：每个交易所赠送 100,000 模拟资金",
        "交易时间：7x24 小时开放交易",
        "交易单位：最小交易单位为 1 股/手",
        "价格精度：价格保留两位小数",
        "T+0 交易：当日买入可当日卖出",
        "无涨跌停限制：价格由市场供需决定",
      ],
    },
    {
      icon: <LineChartOutlined />,
      title: "交易品种",
      items: [
        "系统提供多种交易品种，包括股票、期货、指数等",
        "每个品种都有实时价格波动",
        "可以通过行情中心查看各品种的实时价格和 K 线图",
        "品种价格由买卖订单撮合成交决定",
      ],
    },
    {
      icon: <ClockCircleOutlined />,
      title: "撮合规则",
      items: [
        "价格优先：买单高价优先，卖单低价优先",
        "时间优先：同价格订单按时间先后顺序撮合",
        "买入价 ≥ 卖出价时立即成交",
        "成交价格为先挂单的价格",
        "部分成交后剩余数量继续挂单",
      ],
    },
    {
      icon: <SafetyOutlined />,
      title: "资金管理",
      items: [
        "买入前需确保账户有足够可用资金",
        "卖出前需确保账户有足够可用持仓",
        "委托下单后相应资金/持仓会被冻结",
        "撤单后冻结的资金/持仓会解冻",
        "成交后资金/持仓实时划转",
      ],
    },
    {
      icon: <WarningOutlined />,
      title: "风险提示",
      items: [
        "本系统为模拟交易，不构成真实投资建议",
        "交易数据仅供学习参考，无真实货币价值",
        "请理性交易，注意风险控制",
        "系统可能因技术原因出现数据延迟或异常",
      ],
    },
  ];

  return (
    <div className="welcome-page">
      {/* 页面头部 */}
      <div className="welcome-header">
        <div className="header-content">
          <ThunderboltOutlined className="header-icon" />
          <h1 className="header-title">欢迎使用终末地交易模拟系统</h1>
          <p className="header-subtitle">ENDFIELD TRADING SIMULATION SYSTEM</p>
        </div>
      </div>

      {/* 规则卡片 */}
      <div className="rules-container">
        {rules.map((rule, index) => (
          <Card
            key={index}
            className={`rule-card ${rule.icon.type === ThunderboltOutlined ? 'intro-card' : ''}`}
            title={
              <div className="card-title">
                <span className="card-icon">{rule.icon}</span>
                <span className="card-title-text">{rule.title}</span>
              </div>
            }
            size="small"
          >
            {rule.content && (
              <p className="card-content">{rule.content}</p>
            )}
            {rule.items && (
              <ul className="card-list">
                {rule.items.map((item, i) => (
                  <li key={i} className="card-list-item">
                    <span className="list-dot"></span>
                    {item}
                  </li>
                ))}
              </ul>
            )}
          </Card>
        ))}
      </div>

      {/* 底部提示 */}
      <div className="welcome-footer">
        <p>
          <SafetyOutlined />
          请仔细阅读以上交易规则，确认理解后开始交易
        </p>
        <p className="footer-note">
          系统版本：v2.0.26 | 最后更新：2026-02-27
        </p>
      </div>
    </div>
  );
};

export default Welcome;
