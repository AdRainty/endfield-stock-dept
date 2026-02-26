import React, { useState, useEffect } from "react";
import { Select, InputNumber, Button, Table, message, Form, Tag } from "antd";
import { ArrowUpOutlined, ArrowDownOutlined, ThunderboltOutlined, FileTextOutlined } from "@ant-design/icons";
import axios from "axios";

const { Option } = Select;

const Trade = () => {
  const [exchanges, setExchanges] = useState([]);
  const [selectedExchange, setSelectedExchange] = useState(null);
  const [instruments, setInstruments] = useState([]);
  const [selectedInstrument, setSelectedInstrument] = useState(null);
  const [instrumentData, setInstrumentData] = useState(null);
  const [account, setAccount] = useState(null);
  const [orderForm] = Form.useForm();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [orderType, setOrderType] = useState("BUY");

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

  const getInstrument = async () => {
    if (!selectedInstrument) return;
    try {
      const res = await axios.get(`/api/market/instrument/${selectedInstrument}`);
      if (res.data.code === 0) {
        setInstrumentData(res.data.data);
      }
    } catch (error) {
      console.error("获取品种详情失败", error);
    }
  };

  const getAccount = async () => {
    if (!selectedExchange) return;
    try {
      const res = await axios.get(`/api/trade/account/${selectedExchange}`);
      if (res.data.code === 0) {
        setAccount(res.data.data);
      }
    } catch (error) {
      console.error("获取资金账户失败", error);
    }
  };

  const getOrders = async () => {
    try {
      const res = await axios.get("/api/trade/orders");
      if (res.data.code === 0) {
        setOrders(res.data.data);
      }
    } catch (error) {
      console.error("获取订单列表失败", error);
    }
  };

  useEffect(() => {
    getExchanges();
  }, []);

  useEffect(() => {
    if (selectedExchange) {
      getInstruments();
      getAccount();
    }
  }, [selectedExchange]);

  useEffect(() => {
    if (selectedInstrument) {
      getInstrument();
    }
  }, [selectedInstrument]);

  const handlePlaceOrder = async () => {
    try {
      const values = await orderForm.validateFields();
      const res = await axios.post("/api/trade/order", {
        exchangeId: selectedExchange,
        instrumentCode: selectedInstrument,
        orderType: orderType,
        price: values.price,
        quantity: values.quantity,
      });
      if (res.data.code === 0) {
        message.success(`${orderType === "BUY" ? "买入" : "卖出"}委托成功`);
        orderForm.resetFields();
        getOrders();
        getAccount();
      }
    } catch (error) {
      if (error.response?.data?.message) {
        message.error(error.response.data.message);
      }
    }
  };

  const handleCancelOrder = async (orderNo) => {
    try {
      const res = await axios.post(`/api/trade/order/${orderNo}/cancel`);
      if (res.data.code === 0) {
        message.success("撤单成功");
        getOrders();
      }
    } catch (error) {
      message.error(error.response?.data?.message || "撤单失败");
    }
  };

  const orderColumns = [
    {
      title: "订单号",
      dataIndex: "orderNo",
      key: "orderNo",
      width: 180,
      render: (val) => <span className="order-no">{val}</span>,
    },
    {
      title: "品种",
      dataIndex: "instrumentCode",
      key: "instrumentCode",
      width: 100,
      render: (val) => <span className="instrument-code">{val}</span>,
    },
    {
      title: "类型",
      dataIndex: "orderType",
      key: "orderType",
      width: 70,
      render: (val) => (
        <Tag className={`order-type-tag ${val === "BUY" ? 'buy' : 'sell'}`}>
          {val === "BUY" ? "买入" : "卖出"}
        </Tag>
      ),
    },
    {
      title: "价格",
      dataIndex: "price",
      key: "price",
      width: 80,
      render: (val) => <span className="price">{val?.toFixed(2)}</span>,
    },
    {
      title: "数量",
      dataIndex: "quantity",
      key: "quantity",
      width: 80,
      render: (val) => <span className="quantity">{val?.toFixed(0)}</span>,
    },
    {
      title: "已成交",
      dataIndex: "filledQuantity",
      key: "filledQuantity",
      width: 80,
      render: (val) => <span className="filled">{val?.toFixed(0)}</span>,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (val) => {
        const statusMap = {
          PENDING: { text: "待成交", class: "pending" },
          PARTIALLY_FILLED: { text: "部分成交", class: "partial" },
          FILLED: { text: "已成交", class: "filled" },
          CANCELLED: { text: "已撤单", class: "cancelled" },
        };
        const status = statusMap[val] || { text: val, class: "" };
        return <span className={`order-status ${status.class}`}>{status.text}</span>;
      },
    },
    {
      title: "时间",
      dataIndex: "orderTime",
      key: "orderTime",
      width: 160,
    },
    {
      title: "操作",
      key: "action",
      width: 80,
      render: (_, record) =>
        (record.status === "PENDING" || record.status === "PARTIALLY_FILLED") ? (
          <Button
            size="small"
            className="cancel-btn"
            onClick={() => handleCancelOrder(record.orderNo)}
          >
            撤单
          </Button>
        ) : null,
    },
  ];

  return (
    <div className="trade-page">
      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-title">
          <ThunderboltOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">交易委托</h1>
            <span className="page-title-en">TRADE ORDER</span>
          </div>
        </div>
      </div>

      <div className="trade-content">
        {/* 左侧：委托面板 */}
        <div className="trade-panel">
          <div className="panel-header">
            <span className="panel-title">
              <span className="title-dot"></span>
              委托下单 / PLACE ORDER
            </span>
          </div>

          <div className="order-form-container">
            {/* 交易所和品种选择 */}
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">交易所</label>
                <Select
                  value={selectedExchange}
                  onChange={setSelectedExchange}
                  className="full-width"
                  dropdownClassName="custom-dropdown"
                >
                  {exchanges.map((e) => (
                    <Option key={e.id} value={e.id}>{e.name}</Option>
                  ))}
                </Select>
              </div>
              <div className="form-group">
                <label className="form-label">品种</label>
                <Select
                  value={selectedInstrument}
                  onChange={setSelectedInstrument}
                  className="full-width"
                  loading={loading}
                  dropdownClassName="custom-dropdown"
                >
                  {instruments.map((i) => (
                    <Option key={i.instrumentCode} value={i.instrumentCode}>
                      {i.name}
                    </Option>
                  ))}
                </Select>
              </div>
            </div>

            {/* 当前价格显示 */}
            {instrumentData && (
              <div className="current-price-row">
                <span className="price-label">当前价</span>
                <span className={`price-value ${instrumentData.changePercent >= 0 ? 'rise' : 'fall'}`}>
                  {instrumentData.currentPrice?.toFixed(2)}
                </span>
                <span className={`price-change ${instrumentData.changePercent >= 0 ? 'rise' : 'fall'}`}>
                  {instrumentData.changePercent >= 0 ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
                  {instrumentData.changePercent?.toFixed(2)}%
                </span>
              </div>
            )}

            {/* 买卖类型切换 */}
            <div className="order-type-switch">
              <button
                className={`type-btn buy ${orderType === "BUY" ? 'active' : ''}`}
                onClick={() => setOrderType("BUY")}
              >
                <ArrowUpOutlined /> 买入 / BUY
              </button>
              <button
                className={`type-btn sell ${orderType === "SELL" ? 'active' : ''}`}
                onClick={() => setOrderType("SELL")}
              >
                <ArrowDownOutlined /> 卖出 / SELL
              </button>
            </div>

            {/* 价格和数量输入 */}
            <Form form={orderForm} layout="vertical">
              <div className="form-group">
                <label className="form-label">委托价格</label>
                <Form.Item
                  name="price"
                  rules={[{ required: true, message: "请输入价格" }]}
                >
                  <InputNumber
                    className="full-width-input"
                    min={0.01}
                    step={0.01}
                    placeholder="0.00"
                    controls={false}
                  />
                </Form.Item>
              </div>
              <div className="form-group">
                <label className="form-label">委托数量</label>
                <Form.Item
                  name="quantity"
                  rules={[{ required: true, message: "请输入数量" }]}
                >
                  <InputNumber
                    className="full-width-input"
                    min={1}
                    placeholder="0"
                    controls={false}
                  />
                </Form.Item>
              </div>
            </Form>

            {/* 资金信息 */}
            {account && (
              <div className="account-info">
                <div className="info-row">
                  <span className="info-label">可用资金</span>
                  <span className="info-value">{account.available?.toFixed(2)}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">冻结资金</span>
                  <span className="info-value">{account.frozen?.toFixed(2)}</span>
                </div>
                <div className="info-row highlight">
                  <span className="info-label">总资产</span>
                  <span className={`info-value ${account.totalProfitLoss >= 0 ? 'rise' : 'fall'}`}>
                    {account.totalAsset?.toFixed(2)}
                  </span>
                </div>
              </div>
            )}

            {/* 提交按钮 */}
            <button
              className={`submit-btn ${orderType === "BUY" ? 'buy' : 'sell'}`}
              onClick={handlePlaceOrder}
            >
              <ThunderboltOutlined />
              {orderType === "BUY" ? '确认买入' : '确认卖出'}
            </button>
          </div>
        </div>

        {/* 右侧：委托记录 */}
        <div className="orders-panel">
          <div className="panel-header">
            <span className="panel-title">
              <span className="title-dot"></span>
              委托记录 / ORDER HISTORY
            </span>
            <Button
              size="small"
              className="refresh-btn"
              icon={<FileTextOutlined />}
              onClick={getOrders}
            >
              刷新
            </Button>
          </div>

          <div className="orders-table-container">
            <Table
              columns={orderColumns}
              dataSource={orders}
              rowKey="orderNo"
              pagination={false}
              size="small"
              scroll={{ y: '100%' }}
              className="orders-table"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Trade;
