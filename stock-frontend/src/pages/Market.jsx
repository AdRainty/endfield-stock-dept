import React, { useState, useEffect, useRef } from "react";
import { Select, Tag, Spin, Input, Button, message } from "antd";
import { LineChartOutlined, CaretUpOutlined, CaretDownOutlined, DashboardOutlined, ShoppingOutlined } from "@ant-design/icons";
import axios from "axios";
import KlineChart from "../components/KlineChart";

const { Option } = Select;

const Market = () => {
  const [exchanges, setExchanges] = useState([]);
  const [selectedExchange, setSelectedExchange] = useState(null);
  const [instruments, setInstruments] = useState([]);
  const [selectedInstrument, setSelectedInstrument] = useState(null);
  const [orderBook, setOrderBook] = useState(null);
  const [loading, setLoading] = useState(false);
  const [flashState, setFlashState] = useState({});

  // 交易相关状态
  const [orderType, setOrderType] = useState("BUY"); // BUY / SELL
  const [orderPrice, setOrderPrice] = useState("");
  const [orderQuantity, setOrderQuantity] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const prevPricesRef = useRef({});
  const selectedInstrumentRef = useRef(null);
  const orderBookRef = useRef(null);

  // 更新引用
  useEffect(() => {
    selectedInstrumentRef.current = selectedInstrument;
    orderBookRef.current = orderBook;
  }, [selectedInstrument, orderBook]);

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

  // 处理价格更新和闪烁效果
  const updateInstruments = (newData) => {
    // 检测价格变化
    newData.forEach(inst => {
      const code = inst.instrumentCode;
      const prevPrice = prevPricesRef.current[code];

      if (prevPrice !== undefined && prevPrice !== inst.currentPrice) {
        const direction = inst.currentPrice > prevPrice ? 'rise' : 'fall';
        setFlashState(prev => ({ ...prev, [code]: direction }));

        // 1.5 秒后清除闪烁
        setTimeout(() => {
          setFlashState(prev => ({ ...prev, [code]: null }));
        }, 1500);

        // 如果是当前选中的品种，同时更新档口显示的最新价和涨跌幅
        if (code === selectedInstrumentRef.current && orderBookRef.current) {
          setOrderBook(prev => ({
            ...prev,
            latestPrice: inst.currentPrice,
            changePercent: inst.changePercent,
            changeAmount: inst.changeAmount
          }));
        }
      }
    });

    // 更新引用
    newData.forEach(inst => {
      prevPricesRef.current[inst.instrumentCode] = inst.currentPrice;
    });

    setInstruments(newData);
  };

  // 获取品种列表
  const getInstruments = async () => {
    if (!selectedExchange) return;
    setLoading(true);
    try {
      const res = await axios.get(`/api/exchange/${selectedExchange}/instruments`);
      if (res.data.code === 0) {
        const newData = res.data.data;
        updateInstruments(newData);

        if (newData.length > 0 && !selectedInstrument) {
          setSelectedInstrument(newData[0].instrumentCode);
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

  // 提交订单
  const handleSubmitOrder = async () => {
    if (!selectedExchange || !selectedInstrument) {
      message.warning("请先选择交易品种");
      return;
    }

    if (!orderPrice || !orderQuantity) {
      message.warning("请输入价格和数量");
      return;
    }

    const price = parseFloat(orderPrice);
    const quantity = parseFloat(orderQuantity);

    if (price <= 0 || quantity <= 0) {
      message.warning("价格和数量必须大于 0");
      return;
    }

    setSubmitting(true);
    try {
      const res = await axios.post("/api/trade/order", {
        exchangeId: selectedExchange,
        instrumentCode: selectedInstrument,
        orderType: orderType,
        price: price,
        quantity: quantity,
      });

      if (res.data.code === 0) {
        message.success(`${orderType === "BUY" ? "买入" : "卖出"}委托成功`);
        setOrderPrice("");
        setOrderQuantity("");
      }
    } catch (error) {
      console.error("下单失败", error);
    } finally {
      setSubmitting(false);
    }
  };

  // 设置价格为买一/卖一
  const setPriceFromOrderBook = (type) => {
    if (!orderBook) return;
    if (type === "buy" && orderBook.bids?.[0]?.price) {
      setOrderPrice(orderBook.bids[0].price.toFixed(2));
    } else if (type === "sell" && orderBook.asks?.[0]?.price) {
      setOrderPrice(orderBook.asks[0].price.toFixed(2));
    }
  };

  // 初始化加载交易所
  useEffect(() => {
    getExchanges();
  }, []);

  // 交易所变化时加载品种
  useEffect(() => {
    if (selectedExchange) {
      getInstruments();
      prevPricesRef.current = {}; // 重置价格引用
    }
  }, [selectedExchange]);

  // 档口数据轮询
  useEffect(() => {
    if (selectedExchange && selectedInstrument) {
      getOrderBook();
      const timer = setInterval(getOrderBook, 3000);
      return () => clearInterval(timer);
    }
  }, [selectedExchange, selectedInstrument]);

  // 品种价格轮询（左侧列表）
  useEffect(() => {
    if (selectedExchange) {
      const timer = setInterval(() => {
        axios.get(`/api/exchange/${selectedExchange}/instruments`)
          .then(res => {
            if (res.data.code === 0) {
              updateInstruments(res.data.data);
            }
          })
          .catch(err => console.error("刷新品种失败", err));
      }, 3000);
      return () => clearInterval(timer);
    }
  }, [selectedExchange]);

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

      {/* 主要内容 - 三列布局：左侧品种列表 / 中间 K 线图表 / 右侧委托档口 */}
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
            {instruments.map((inst) => {
              const flash = flashState[inst.instrumentCode];
              return (
                <div
                  key={inst.instrumentCode}
                  className={`instrument-card ${selectedInstrument === inst.instrumentCode ? 'active' : ''} ${flash ? 'flash-' + flash : ''}`}
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
                      <span className={`price-value ${flash || ''}`}>{inst.currentPrice?.toFixed(2)}</span>
                    </div>
                    <div className={`inst-change ${inst.changePercent >= 0 ? 'rise' : 'fall'}`}>
                      {inst.changePercent >= 0 ? <CaretUpOutlined /> : <CaretDownOutlined />}
                      <span>{inst.changePercent >= 0 ? '+' : ''}{inst.changePercent?.toFixed(2)}%</span>
                    </div>
                  </div>
                  <div className="inst-footer">
                    <span className="inst-vol">量：{inst.volume?.toLocaleString()}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 中间：K 线图表 */}
        <div className="chart-panel">
          {selectedExchange && selectedInstrument ? (
            <KlineChart exchangeId={selectedExchange} instrumentCode={selectedInstrument} />
          ) : (
            <div className="empty-state">
              <div className="empty-icon">
                <LineChartOutlined />
              </div>
              <p className="empty-text">请选择交易品种查看图表</p>
              <p className="empty-subtext">SELECT AN INSTRUMENT TO VIEW CHART</p>
            </div>
          )}
        </div>

        {/* 右侧：委托档口 + 交易面板 */}
        <div className="orderbook-panel">
          <div className="panel-header">
            <span className="panel-title">
              <span className="title-dot"></span>
              五档行情 / ORDER BOOK
            </span>
            <span className="panel-subtitle">
              {selectedInstrument || '-'}
            </span>
          </div>

          {orderBook ? (
            <div className="orderbook-content">
              {/* 卖盘档口 - 倒序显示 卖 5 到卖 1 */}
              <div className="orderbook-sells">
                {Array.from({ length: 5 }).map((_, i) => {
                  const ask = orderBook.asks?.[4 - i] || {};
                  const level = 5 - i;
                  return (
                    <div key={`ask-${level}`} className="orderbook-row-simple">
                      <span className="level-label">卖{level}</span>
                      <span className={`price ask-price ${ask.price ? '' : 'empty'}`}>
                        {ask.price?.toFixed(2) || '-'}
                      </span>
                      <span className={`qty bid-qty ${ask.quantity ? '' : 'empty'}`}>
                        {ask.quantity?.toFixed(0) || '-'}
                      </span>
                    </div>
                  );
                })}
              </div>

              {/* 分隔线 */}
              <div className="orderbook-divider"></div>

              {/* 买盘档口 - 买 1 到买 5 */}
              <div className="orderbook-buys">
                {Array.from({ length: 5 }).map((_, i) => {
                  const bid = orderBook.bids?.[i] || {};
                  const level = i + 1;
                  return (
                    <div key={`bid-${level}`} className="orderbook-row-simple">
                      <span className="level-label">买{level}</span>
                      <span className={`price bid-price ${bid.price ? '' : 'empty'}`}>
                        {bid.price?.toFixed(2) || '-'}
                      </span>
                      <span className={`qty bid-qty ${bid.quantity ? '' : 'empty'}`}>
                        {bid.quantity?.toFixed(0) || '-'}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : (
            <div className="empty-state">
              <div className="empty-icon">
                <LineChartOutlined />
              </div>
              <p className="empty-text">请选择交易品种</p>
              <p className="empty-subtext">SELECT AN INSTRUMENT</p>
            </div>
          )}

          {/* 交易面板 */}
          <div className="trade-panel">
            <div className="trade-tabs">
              <button
                className={`trade-tab ${orderType === 'BUY' ? 'active' : ''}`}
                onClick={() => setOrderType('BUY')}
              >
                买入 / BUY
              </button>
              <button
                className={`trade-tab ${orderType === 'SELL' ? 'active' : ''}`}
                onClick={() => setOrderType('SELL')}
              >
                卖出 / SELL
              </button>
            </div>

            <div className="trade-form">
              <div className="trade-row">
                <span className="trade-label">价格</span>
                <div className="trade-input-group">
                  <Input
                    type="number"
                    className="trade-input"
                    value={orderPrice}
                    onChange={(e) => setOrderPrice(e.target.value)}
                    placeholder="0.00"
                    step="0.01"
                  />
                  <div className="price-quick-btns">
                    <button onClick={() => setPriceFromOrderBook('sell')} className="price-btn">卖一</button>
                    <button onClick={() => setPriceFromOrderBook('buy')} className="price-btn">买一</button>
                  </div>
                </div>
              </div>

              <div className="trade-row">
                <span className="trade-label">数量</span>
                <Input
                  type="number"
                  className="trade-input"
                  value={orderQuantity}
                  onChange={(e) => setOrderQuantity(e.target.value)}
                  placeholder="0"
                  step="1"
                />
              </div>

              <div className="trade-row">
                <span className="trade-label">总额</span>
                <span className="trade-total">
                  {(parseFloat(orderPrice || 0) * parseFloat(orderQuantity || 0)).toFixed(2)}
                  <span className="trade-unit">CNY</span>
                </span>
              </div>

              <Button
                type="primary"
                className={`trade-submit ${orderType === 'BUY' ? 'btn-buy' : 'btn-sell'}`}
                onClick={handleSubmitOrder}
                loading={submitting}
                icon={<ShoppingOutlined />}
              >
                {orderType === 'BUY' ? '买入委托' : '卖出委托'}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Market;
