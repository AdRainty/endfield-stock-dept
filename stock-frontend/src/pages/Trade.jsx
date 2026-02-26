import React, { useState, useEffect } from "react";
import { Card, Row, Col, Select, InputNumber, Button, Table, message, Tabs, Form } from "antd";
import { ArrowUpOutlined, ArrowDownOutlined } from "@ant-design/icons";
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

  // 获取交易所
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

  // 获取品种
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

  // 获取品种详情
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

  // 获取资金账户
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

  // 获取订单列表
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

  // 下单
  const handlePlaceOrder = async (type) => {
    try {
      const values = await orderForm.validateFields();
      const res = await axios.post("/api/trade/order", {
        exchangeId: selectedExchange,
        instrumentCode: selectedInstrument,
        orderType: type,
        price: values.price,
        quantity: values.quantity,
      });
      if (res.data.code === 0) {
        message.success(`${type === "BUY" ? "买入" : "卖出"}委托成功`);
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

  // 撤单
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
    },
    {
      title: "品种",
      dataIndex: "instrumentCode",
      key: "instrumentCode",
    },
    {
      title: "类型",
      dataIndex: "orderType",
      key: "orderType",
      render: (val) => (
        <span style={{ color: val === "BUY" ? "#cf1322" : "#3f8600" }}>
          {val === "BUY" ? "买入" : "卖出"}
        </span>
      ),
    },
    {
      title: "价格",
      dataIndex: "price",
      key: "price",
    },
    {
      title: "数量",
      dataIndex: "quantity",
      key: "quantity",
    },
    {
      title: "已成交",
      dataIndex: "filledQuantity",
      key: "filledQuantity",
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (val) => {
        const statusMap = {
          PENDING: "待成交",
          PARTIALLY_FILLED: "部分成交",
          FILLED: "已成交",
          CANCELLED: "已撤单",
        };
        return statusMap[val] || val;
      },
    },
    {
      title: "时间",
      dataIndex: "orderTime",
      key: "orderTime",
    },
    {
      title: "操作",
      key: "action",
      render: (_, record) =>
        record.status === "PENDING" || record.status === "PARTIALLY_FILLED" ? (
          <Button size="small" danger onClick={() => handleCancelOrder(record.orderNo)}>
            撤单
          </Button>
        ) : null,
    },
  ];

  return (
    <div>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="委托下单">
            <Form form={orderForm} layout="inline">
              <Form.Item
                label="交易所"
                style={{ marginBottom: 16 }}
              >
                <Select
                  value={selectedExchange}
                  onChange={setSelectedExchange}
                  style={{ width: 120 }}
                >
                  {exchanges.map((e) => (
                    <Option key={e.id} value={e.id}>
                      {e.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
              <Form.Item
                label="品种"
                style={{ marginBottom: 16 }}
              >
                <Select
                  value={selectedInstrument}
                  onChange={setSelectedInstrument}
                  style={{ width: 150 }}
                  loading={loading}
                >
                  {instruments.map((i) => (
                    <Option key={i.instrumentCode} value={i.instrumentCode}>
                      {i.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
              {instrumentData && (
                <div style={{ marginBottom: 16, padding: 12, background: "#f5f5f5", borderRadius: 4 }}>
                  <span style={{ fontSize: 18, fontWeight: "bold" }}>
                    当前价：{instrumentData.currentPrice?.toFixed(2)}
                  </span>
                  <span style={{
                    marginLeft: 12,
                    color: instrumentData.changePercent >= 0 ? "#cf1322" : "#3f8600",
                  }}>
                    {instrumentData.changePercent >= 0 ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
                    {instrumentData.changePercent?.toFixed(2)}%
                  </span>
                </div>
              )}
              <Form.Item
                name="price"
                label="委托价格"
                rules={[{ required: true, message: "请输入价格" }]}
                style={{ marginBottom: 16 }}
              >
                <InputNumber min={0.01} step={0.01} style={{ width: 150 }} />
              </Form.Item>
              <Form.Item
                name="quantity"
                label="委托数量"
                rules={[{ required: true, message: "请输入数量" }]}
                style={{ marginBottom: 16 }}
              >
                <InputNumber min={1} style={{ width: 150 }} />
              </Form.Item>
              <Form.Item style={{ marginBottom: 16 }}>
                <Button
                  type="primary"
                  danger
                  onClick={() => handlePlaceOrder("BUY")}
                  style={{ marginRight: 8 }}
                >
                  买入
                </Button>
                <Button
                  type="primary"
                  style={{ background: "#3f8600", borderColor: "#3f8600" }}
                  onClick={() => handlePlaceOrder("SELL")}
                >
                  卖出
                </Button>
              </Form.Item>
            </Form>
            {account && (
              <div style={{ marginTop: 16, padding: 12, background: "#f0f8ff", borderRadius: 4 }}>
                <div>可用资金：{account.available?.toFixed(2)}</div>
                <div>冻结资金：{account.frozen?.toFixed(2)}</div>
                <div>总资产：{account.totalAsset?.toFixed(2)}</div>
              </div>
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card title="委托记录">
            <Table
              columns={orderColumns}
              dataSource={orders}
              rowKey="orderNo"
              pagination={{ pageSize: 10 }}
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Trade;
