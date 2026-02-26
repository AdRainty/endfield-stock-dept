import React, { useState, useEffect } from "react";
import { Table, Tag } from "antd";
import { WalletOutlined, CaretUpOutlined, CaretDownOutlined } from "@ant-design/icons";
import axios from "axios";

const Position = () => {
  const [positions, setPositions] = useState([]);
  const [loading, setLoading] = useState(false);

  const getPositions = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/api/trade/positions");
      if (res.data.code === 0) {
        setPositions(res.data.data);
      }
    } catch (error) {
      console.error("获取持仓失败", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getPositions();
  }, []);

  const columns = [
    {
      title: "交易所",
      dataIndex: "exchangeName",
      key: "exchangeName",
      width: 100,
      render: (val) => <span className="exchange-name">{val}</span>,
    },
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
      {/* 强制覆盖 Ant Design 表格样式的内联样式 */}
      <style>{`
        #root .ant-table-wrapper .ant-table,
        #root .ant-table-wrapper .ant-table table {
          background: transparent !important;
          color: var(--text-secondary) !important;
        }
        #root .ant-table-wrapper .ant-table-thead > tr > th,
        #root .ant-table-wrapper .ant-table-thead .ant-table-cell {
          background: var(--color-bg-tertiary) !important;
          color: var(--text-secondary) !important;
        }
        #root .ant-table-wrapper .ant-table-tbody > tr > td,
        #root .ant-table-wrapper .ant-table-tbody .ant-table-cell {
          background: var(--color-bg-secondary) !important;
          color: var(--text-secondary) !important;
        }
        #root .ant-table-wrapper .ant-table-tbody > tr:hover {
          background: var(--color-bg-tertiary) !important;
        }
        #root .ant-table-wrapper .ant-table-placeholder,
        #root .ant-table-wrapper .ant-table-placeholder .ant-table-cell {
          background: var(--color-bg-secondary) !important;
        }
        #root .ant-table-wrapper .ant-empty-description {
          color: var(--text-tertiary) !important;
        }
        #root .ant-table-wrapper .ant-table-measure-row {
          visibility: hidden !important;
        }
        #root .ant-table-wrapper .ant-table-measure-cell {
          background: var(--color-bg-tertiary) !important;
        }
      `}</style>
      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-title">
          <WalletOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">持仓查询</h1>
            <span className="page-title-en">POSITION QUERY</span>
          </div>
        </div>
      </div>

      {/* 持仓列表 */}
      <div className="position-panel">
        <div className="panel-header">
          <span className="panel-title">
            <span className="title-dot"></span>
            持仓列表 / POSITIONS
          </span>
          <span className="panel-subtitle">
            {positions.length} 个持仓 / ALL EXCHANGES
          </span>
        </div>
        <div className="position-table-container">
          <Table
            columns={columns}
            dataSource={positions}
            rowKey={(record) => `${record.exchangeId}-${record.instrumentCode}`}
            pagination={false}
            loading={loading}
            scroll={{ x: 1400 }}
            className="position-table"
          />
        </div>
      </div>
    </div>
  );
};

export default Position;
