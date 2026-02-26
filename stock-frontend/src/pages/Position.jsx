import React, { useState, useEffect } from "react";
import { Select, Table, Tag, Card } from "antd";
import { WalletOutlined, CaretUpOutlined, CaretDownOutlined, DashboardOutlined } from "@ant-design/icons";
import request from "../services/request";

const { Option } = Select;

const Position = () => {
  const [exchanges, setExchanges] = useState([]);
  const [selectedExchange, setSelectedExchange] = useState(null);
  const [positions, setPositions] = useState([]);
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(false);

  const getExchanges = async () => {
    try {
      const res = await request.get("/exchange/list");
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

  const getPositions = async () => {
    if (!selectedExchange) return;
    setLoading(true);
    try {
      const res = await request.get(`/trade/position/${selectedExchange}`);
      if (res.data.code === 0) {
        setPositions(res.data.data);
      }
    } catch (error) {
      console.error("获取持仓失败", error);
    } finally {
      setLoading(false);
    }
  };

  const getAccount = async () => {
    if (!selectedExchange) return;
    try {
      const res = await request.get(`/trade/account/${selectedExchange}`);
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
      width: 120,
      render: (val) => <span className="instrument-code">{val}</span>,
    },
    {
      title: "品种名称",
      dataIndex: "instrumentName",
      key: "instrumentName",
      width: 100,
    },
    {
      title: "持仓数量",
      dataIndex: "quantity",
      key: "quantity",
      width: 100,
      render: (val) => <span className="quantity">{val?.toFixed(2)}</span>,
    },
    {
      title: "可用数量",
      dataIndex: "availableQuantity",
      key: "availableQuantity",
      width: 100,
      render: (val) => <span className="available">{val?.toFixed(2)}</span>,
    },
    {
      title: "冻结数量",
      dataIndex: "frozenQuantity",
      key: "frozenQuantity",
      width: 100,
      render: (val) => <span className="frozen">{val?.toFixed(2)}</span>,
    },
    {
      title: "成本价",
      dataIndex: "costPrice",
      key: "costPrice",
      width: 90,
      render: (val) => <span className="cost">{val?.toFixed(2)}</span>,
    },
    {
      title: "最新价",
      dataIndex: "latestPrice",
      key: "latestPrice",
      width: 90,
      render: (val) => <span className="price">{val?.toFixed(2)}</span>,
    },
    {
      title: "持仓市值",
      dataIndex: "marketValue",
      key: "marketValue",
      width: 110,
      render: (val) => <span className="value">{val?.toFixed(2)}</span>,
    },
    {
      title: "持仓盈亏",
      dataIndex: "profitLoss",
      key: "profitLoss",
      width: 110,
      render: (val) => (
        <span className={`profit-loss ${val >= 0 ? 'rise' : 'fall'}`}>
          {val >= 0 ? <CaretUpOutlined /> : <CaretDownOutlined />}
          {val >= 0 ? '+' : ''}{val?.toFixed(2)}
        </span>
      ),
    },
    {
      title: "盈亏比例",
      dataIndex: "profitLossRate",
      key: "profitLossRate",
      width: 100,
      render: (val) => (
        <Tag className={`profit-rate-tag ${val >= 0 ? 'rise' : 'fall'}`}>
          {val >= 0 ? '+' : ''}{val?.toFixed(2)}%
        </Tag>
      ),
    },
  ];

  return (
    <div className="position-page">
      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-title">
          <WalletOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">持仓查询</h1>
            <span className="page-title-en">POSITION QUERY</span>
          </div>
        </div>
        <div className="header-controls">
          <Select
            value={selectedExchange}
            onChange={setSelectedExchange}
            dropdownClassName="custom-dropdown"
          >
            {exchanges.map((e) => (
              <Option key={e.id} value={e.id}>
                <DashboardOutlined /> {e.name}
              </Option>
            ))}
          </Select>
        </div>
      </div>

      {/* 持仓统计 - 表格上方 */}
      {account && (
        <div className="position-summary">
          <div className="summary-item">
            <span className="summary-label">总资产</span>
            <span className={`summary-value ${account.totalProfitLoss >= 0 ? 'rise' : 'fall'}`}>
              {account.totalAsset?.toFixed(2)}
            </span>
            <span className="summary-unit">CNY</span>
          </div>
          <div className="summary-divider"></div>
          <div className="summary-item">
            <span className="summary-label">可用资金</span>
            <span className="summary-value">{account.available?.toFixed(2)}</span>
            <span className="summary-unit">CNY</span>
          </div>
          <div className="summary-divider"></div>
          <div className="summary-item">
            <span className="summary-label">持仓市值</span>
            <span className="summary-value">{account.positionValue?.toFixed(2)}</span>
            <span className="summary-unit">CNY</span>
          </div>
          <div className="summary-divider"></div>
          <div className="summary-item">
            <span className="summary-label">当日盈亏</span>
            <span className={`summary-value ${account.todayProfitLoss >= 0 ? 'rise' : 'fall'}`}>
              {account.todayProfitLoss >= 0 ? '+' : ''}{account.todayProfitLoss?.toFixed(2)}
            </span>
          </div>
          <div className="summary-divider"></div>
          <div className="summary-item">
            <span className="summary-label">持仓盈亏</span>
            <span className={`summary-value ${account.positionProfitLoss >= 0 ? 'rise' : 'fall'}`}>
              {account.positionProfitLoss >= 0 ? '+' : ''}{account.positionProfitLoss?.toFixed(2)}
            </span>
          </div>
          <div className="summary-divider"></div>
          <div className="summary-item">
            <span className="summary-label">总盈亏</span>
            <span className={`summary-value ${account.totalProfitLoss >= 0 ? 'rise' : 'fall'}`}>
              {account.totalProfitLoss >= 0 ? '+' : ''}{account.totalProfitLoss?.toFixed(2)}
            </span>
          </div>
        </div>
      )}

      {/* 持仓列表 */}
      <div className="position-panel">
        <div className="panel-header">
          <span className="panel-title">
            <span className="title-dot"></span>
            持仓列表 / POSITIONS
          </span>
          <span className="panel-subtitle">
            {positions.length} 个持仓 / {selectedExchange ? 'EXCHANGE ' + selectedExchange : 'SELECT EXCHANGE'}
          </span>
        </div>
        <div className="position-table-container">
          <Table
            columns={columns}
            dataSource={positions}
            rowKey="instrumentCode"
            pagination={false}
            loading={loading}
            scroll={{ x: 1200 }}
            className="position-table"
          />
        </div>
      </div>
    </div>
  );
};

export default Position;
