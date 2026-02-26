import React, { useState, useEffect } from "react";
import { Card, Table, Select, Row, Col, Statistic, Tag } from "antd";
import { WalletOutlined } from "@ant-design/icons";
import axios from "axios";

const { Option } = Select;

const Position = () => {
  const [exchanges, setExchanges] = useState([]);
  const [selectedExchange, setSelectedExchange] = useState(null);
  const [positions, setPositions] = useState([]);
  const [account, setAccount] = useState(null);
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

  // 获取持仓
  const getPositions = async () => {
    if (!selectedExchange) return;
    setLoading(true);
    try {
      const res = await axios.get(`/api/trade/position/${selectedExchange}`);
      if (res.data.code === 0) {
        setPositions(res.data.data);
      }
    } catch (error) {
      console.error("获取持仓失败", error);
    } finally {
      setLoading(false);
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

  useEffect(() => {
    getExchanges();
  }, []);

  useEffect(() => {
    if (selectedExchange) {
      getPositions();
      getAccount();
    }
  }, [selectedExchange]);

  const columns = [
    {
      title: "品种代码",
      dataIndex: "instrumentCode",
      key: "instrumentCode",
    },
    {
      title: "品种名称",
      dataIndex: "instrumentName",
      key: "instrumentName",
    },
    {
      title: "持仓数量",
      dataIndex: "quantity",
      key: "quantity",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "可用数量",
      dataIndex: "availableQuantity",
      key: "availableQuantity",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "冻结数量",
      dataIndex: "frozenQuantity",
      key: "frozenQuantity",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "成本价",
      dataIndex: "costPrice",
      key: "costPrice",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "最新价",
      dataIndex: "latestPrice",
      key: "latestPrice",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "持仓市值",
      dataIndex: "marketValue",
      key: "marketValue",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "持仓盈亏",
      dataIndex: "profitLoss",
      key: "profitLoss",
      render: (val) => (
        <span style={{ color: val >= 0 ? "#cf1322" : "#3f8600" }}>
          {val >= 0 ? "+" : ""}{val?.toFixed(2)}
        </span>
      ),
    },
    {
      title: "盈亏比例",
      dataIndex: "profitLossRate",
      key: "profitLossRate",
      render: (val) => (
        <Tag color={val >= 0 ? "red" : "green"}>
          {val >= 0 ? "+" : ""}{val?.toFixed(2)}%
        </Tag>
      ),
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
        </Row>
      </Card>

      {account && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="可用资金"
                value={account.available}
                precision={2}
                prefix="¥"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="冻结资金"
                value={account.frozen}
                precision={2}
                prefix="¥"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="持仓市值"
                value={account.positionValue}
                precision={2}
                prefix="¥"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="总资产"
                value={account.totalAsset}
                precision={2}
                prefix="¥"
                valueStyle={{ color: account.totalProfitLoss >= 0 ? "#cf1322" : "#3f8600" }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card title={<span><WalletOutlined /> 持仓列表</span>}>
        <Table
          columns={columns}
          dataSource={positions}
          rowKey="instrumentCode"
          pagination={false}
          loading={loading}
          scroll={{ x: 1200 }}
        />
      </Card>
    </div>
  );
};

export default Position;
