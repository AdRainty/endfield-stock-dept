import React, { useState, useEffect } from "react";
import { Card, Table, Select, Row, Col, Statistic, Tag } from "antd";
import { LineChartOutlined } from "@ant-design/icons";
import axios from "axios";
import { Line } from "recharts";
import { LineChart, ResponsiveContainer, XAxis, YAxis, Tooltip, Line as RechartsLine } from "recharts";

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
      // 每 3 秒刷新一次档口
      const timer = setInterval(getOrderBook, 3000);
      return () => clearInterval(timer);
    }
  }, [selectedExchange, selectedInstrument]);

  // 品种表格列
  const columns = [
    {
      title: "品种代码",
      dataIndex: "instrumentCode",
      key: "instrumentCode",
    },
    {
      title: "品种名称",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "当前价",
      dataIndex: "currentPrice",
      key: "currentPrice",
      render: (val) => <span style={{ fontWeight: "bold" }}>{val?.toFixed(2)}</span>,
    },
    {
      title: "涨跌幅",
      dataIndex: "changePercent",
      key: "changePercent",
      render: (val) => (
        <span style={{ color: val >= 0 ? "#cf1322" : "#3f8600" }}>
          {val >= 0 ? "+" : ""}{val?.toFixed(2)}%
        </span>
      ),
    },
    {
      title: "涨跌额",
      dataIndex: "changeAmount",
      key: "changeAmount",
      render: (val) => (
        <span style={{ color: val >= 0 ? "#cf1322" : "#3f8600" }}>
          {val >= 0 ? "+" : ""}{val?.toFixed(2)}
        </span>
      ),
    },
    {
      title: "最高价",
      dataIndex: "highPrice",
      key: "highPrice",
    },
    {
      title: "最低价",
      dataIndex: "lowPrice",
      key: "lowPrice",
    },
    {
      title: "成交量",
      dataIndex: "volume",
      key: "volume",
    },
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col>
            <span style={{ fontSize: 16, fontWeight: "bold" }}>选择交易所：</span>
          </Col>
          <Col>
            <Select
              value={selectedExchange}
              onChange={setSelectedExchange}
              style={{ width: 150 }}
            >
              {exchanges.map((e) => (
                <Option key={e.id} value={e.id}>
                  {e.name}
                </Option>
              ))}
            </Select>
          </Col>
          <Col style={{ marginLeft: 24 }}>
            <span style={{ fontSize: 16, fontWeight: "bold" }}>选择品种：</span>
          </Col>
          <Col>
            <Select
              value={selectedInstrument}
              onChange={setSelectedInstrument}
              style={{ width: 200 }}
              loading={loading}
            >
              {instruments.map((i) => (
                <Option key={i.instrumentCode} value={i.instrumentCode}>
                  {i.name} ({i.instrumentCode})
                </Option>
              ))}
            </Select>
          </Col>
        </Row>
      </Card>

      <Row gutter={16}>
        <Col span={16}>
          <Card title={<span><LineChartOutlined /> 品种列表</span>}>
            <Table
              columns={columns}
              dataSource={instruments}
              rowKey="instrumentCode"
              pagination={false}
              size="small"
              onRow={(record) => ({
                onClick: () => setSelectedInstrument(record.instrumentCode),
                style: {
                  cursor: "pointer",
                  background:
                    selectedInstrument === record.instrumentCode
                      ? "#e6f7ff"
                      : "",
                },
              })}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card title="买卖档口">
            {orderBook ? (
              <div>
                <div style={{ textAlign: "center", marginBottom: 16 }}>
                  <Statistic
                    title="最新价"
                    value={orderBook.latestPrice}
                    precision={2}
                    valueStyle={{
                      color: orderBook.changePercent >= 0 ? "#cf1322" : "#3f8600",
                    }}
                  />
                  <Tag color={orderBook.changePercent >= 0 ? "red" : "green"}>
                    {orderBook.changePercent >= 0 ? "+" : ""}
                    {orderBook.changePercent?.toFixed(2)}%
                  </Tag>
                </div>
                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                  <thead>
                    <tr style={{ background: "#fafafa" }}>
                      <th style={{ padding: 8, color: "#cf1322" }}>买价</th>
                      <th style={{ padding: 8 }}>数量</th>
                      <th style={{ padding: 8 }} />
                      <th style={{ padding: 8 }}>数量</th>
                      <th style={{ padding: 8, color: "#3f8600" }}>卖价</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Array.from({ length: 5 }).map((_, i) => {
                      const bid = orderBook.bids?.[i] || {};
                      const ask = orderBook.asks?.[i] || {};
                      return (
                        <tr key={i} style={{ borderBottom: "1px solid #f0f0f0" }}>
                          <td style={{ padding: 8, textAlign: "right", color: bid.price ? "#cf1322" : "#999" }}>
                            {bid.price?.toFixed(2) || "-"}
                          </td>
                          <td style={{ padding: 8, textAlign: "right" }}>
                            {bid.quantity?.toFixed(0) || "-"}
                          </td>
                          <td style={{ padding: 8, textAlign: "center" }}>{5 - i}</td>
                          <td style={{ padding: 8, textAlign: "right" }}>
                            {ask.quantity?.toFixed(0) || "-"}
                          </td>
                          <td style={{ padding: 8, textAlign: "left", color: ask.price ? "#3f8600" : "#999" }}>
                            {ask.price?.toFixed(2) || "-"}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            ) : (
              <div style={{ textAlign: "center", padding: 40 }}>请选择品种查看档口</div>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Market;
