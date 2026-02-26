import React, { useState, useEffect } from "react";
import { Select, Tag, Spin } from "antd";
import { LineChartOutlined, CaretUpOutlined, CaretDownOutlined, DashboardOutlined } from "@ant-design/icons";
import axios from "axios";

const { Option } = Select;

const Market = () => {
  const [exchanges, setExchanges] = useState([]);
  const [selectedExchange, setSelectedExchange] = useState(null);
  const [instruments, setInstruments] = useState([]);
  const [selectedInstrument, setSelectedInstrument] = useState(null);
  const [orderBook, setOrderBook] = useState(null);
  const [loading, setLoading] = useState(false);

  // 获取交易所列表
  const getExchanges = async () => {
    try {
      const res = await axios.get("/api/exchange/list");
      if (res.data.code === 0) {
        setExchanges(res.data.data);
        if (res.data.data.length > 0) {
          setSelectedExchange(res.data.data[0].id);
        }
      }
    } catch (error) {
      console.error("获取交易所失败", error);
    }
  };

  // 获取品种列表
  const getInstruments = async () => {
    if (!selectedExchange) return;
    setLoading(true);
    try {
      const res = await axios.get(`/api/exchange/${selectedExchange}/instruments`);
      if (res.data.code === 0) {
        setInstruments(res.data.data);
        if (res.data.data.length > 0) {
          setSelectedInstrument(res.data.data[0].instrumentCode);
        }
      }
    } catch (error) {
      console.error("获取品种失败", error);
    } finally {
      setLoading(false);
    }
  };

  // 获取档口数据
  const getOrderBook = async () => {
    if (!selectedExchange || !selectedInstrument) return;
    try {
      const res = await axios.get(`/api/market/orderbook/${selectedExchange}/${selectedInstrument}`);
      if (res.data.code === 0) {
        setOrderBook(res.data.data);
      }
    } catch (error) {
      console.error("获取档口失败", error);
    }
  };

  useEffect(() => {
    getExchanges();
  }, []);

  useEffect(() => {
    if (selectedExchange) {
      getInstruments();
    }
  }, [selectedExchange]);

  useEffect(() => {
    if (selectedExchange && selectedInstrument) {
      getOrderBook();
      const timer = setInterval(getOrderBook, 3000);
      return () => clearInterval(timer);
    }
  }, [selectedExchange, selectedInstrument]);

  return (
    <div className="market-page">
      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-title">
          <LineChartOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">行情中心</h1>
            <span className="page-title-en">MARKET CENTER</span>
          </div>
        </div>
        <div className="header-controls">
          <Select
            className="exchange-select"
            value={selectedExchange}
            onChange={setSelectedExchange}
            dropdownClassName="custom-dropdown"
          >
            {exchanges.map((e) => (
              <Option key={e.id} value={e.id}>
                <span className="exchange-option">
                  <DashboardOutlined /> {e.name}
                </span>
              </Option>
            ))}
          </Select>
        </div>
      </div>

      {/* 主要内容 */}
      <div className="market-content">
        {/* 左侧：品种列表 */}
        <div className="instruments-panel">
          <div className="panel-header">
            <span className="panel-title">
              <span className="title-dot"></span>
              交易品种 / INSTRUMENTS
            </span>
            {loading && <Spin size="small" className="panel-loader" />}
          </div>
          <div className="instruments-list">
            {instruments.map((inst) => (
              <div
                key={inst.instrumentCode}
                className={`instrument-card ${selectedInstrument === inst.instrumentCode ? 'active' : ''}`}
                onClick={() => setSelectedInstrument(inst.instrumentCode)}
              >
                <div className="inst-header">
                  <span className="inst-code">{inst.instrumentCode}</span>
                  <Tag className={`inst-tag type-${inst.instrumentCode?.charAt(0)}`}>
                    {inst.name}
                  </Tag>
                </div>
                <div className="inst-body">
                  <div className="inst-price">
                    <span className="price-label">现价</span>
                    <span className="price-value">{inst.currentPrice?.toFixed(2)}</span>
                  </div>
                  <div className={`inst-change ${inst.changePercent >= 0 ? 'rise' : 'fall'}`}>
                    {inst.changePercent >= 0 ? <CaretUpOutlined /> : <CaretDownOutlined />}
                    <span>{inst.changePercent >= 0 ? '+' : ''}{inst.changePercent?.toFixed(2)}%</span>
                  </div>
                </div>
                <div className="inst-footer">
                  <span className="inst-vol">成交量：{inst.volume?.toLocaleString()}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 右侧：档口详情 */}
        <div className="orderbook-panel">
          <div className="panel-header">
            <span className="panel-title">
              <span className="title-dot"></span>
              委托档口 / ORDER BOOK
            </span>
            <span className="panel-subtitle">
              {selectedInstrument || 'SELECT INSTRUMENT'}
            </span>
          </div>

          {orderBook ? (
            <div className="orderbook-content">
              {/* 当前价格 */}
              <div className="current-price-display">
                <div className="price-main">
                  <span className={`price-big ${orderBook.changePercent >= 0 ? 'rise' : 'fall'}`}>
                    {orderBook.latestPrice?.toFixed(2)}
                  </span>
                  <span className="price-currency">CNY</span>
                </div>
                <div className={`price-change ${orderBook.changePercent >= 0 ? 'rise' : 'fall'}`}>
                  <span className="change-value">
                    {orderBook.changeAmount >= 0 ? '+' : ''}{orderBook.changeAmount?.toFixed(2)}
                  </span>
                  <span className="change-percent">
                    {orderBook.changePercent >= 0 ? '+' : ''}{orderBook.changePercent?.toFixed(2)}%
                  </span>
                </div>
              </div>

              {/* 买卖档口表格 */}
              <div className="orderbook-table">
                <div className="orderbook-header">
                  <span className="col-label">买价 / BID</span>
                  <span className="col-label">数量</span>
                  <span className="col-label">档位</span>
                  <span className="col-label">数量</span>
                  <span className="col-label">卖价 / ASK</span>
                </div>
                <div className="orderbook-rows">
                  {Array.from({ length: 5 }).map((_, i) => {
                    const bid = orderBook.bids?.[i] || {};
                    const ask = orderBook.asks?.[i] || {};
                    const level = 5 - i;
                    return (
                      <div key={i} className="orderbook-row" style={{ '--level': level }}>
                        <span className={`cell bid-price ${bid.price ? '' : 'empty'}`}>
                          {bid.price?.toFixed(2) || '-'}
                        </span>
                        <span className={`cell bid-qty ${bid.quantity ? '' : 'empty'}`}>
                          {bid.quantity?.toFixed(0) || '-'}
                        </span>
                        <span className="cell level">{level}</span>
                        <span className={`cell ask-qty ${ask.quantity ? '' : 'empty'}`}>
                          {ask.quantity?.toFixed(0) || '-'}
                        </span>
                        <span className={`cell ask-price ${ask.price ? '' : 'empty'}`}>
                          {ask.price?.toFixed(2) || '-'}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* 深度条装饰 */}
              <div className="depth-indicator">
                <div className="depth-bid" style={{ width: `${Math.random() * 60 + 20}%` }}></div>
                <div className="depth-center"></div>
                <div className="depth-ask" style={{ width: `${Math.random() * 60 + 20}%` }}></div>
              </div>
            </div>
          ) : (
            <div className="empty-state">
              <div className="empty-icon">
                <LineChartOutlined />
              </div>
              <p className="empty-text">请选择交易品种查看档口数据</p>
              <p className="empty-subtext">SELECT AN INSTRUMENT TO VIEW ORDER BOOK</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Market;
