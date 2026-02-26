import React, { useState, useEffect } from "react";
import { Table, Tag, Select, Button, message } from "antd";
import { FileTextOutlined, ThunderboltOutlined } from "@ant-design/icons";
import axios from "axios";

const { Option } = Select;

const OrdersHistory = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState("");

  const getOrders = async () => {
    setLoading(true);
    try {
      let url = "/api/trade/orders";
      const params = {};
      if (selectedStatus) {
        params.status = selectedStatus;
      }

      const res = await axios.get(url, { params });
      if (res.data.code === 0) {
        setOrders(res.data.data);
      }
    } catch (error) {
      console.error("获取订单列表失败", error);
      message.error("获取委托记录失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getOrders();
  }, []);

  const handleStatusChange = (value) => {
    setSelectedStatus(value);
    setTimeout(() => {
      getOrders();
    }, 100);
  };

  const orderColumns = [
    {
      title: "订单号",
      dataIndex: "orderNo",
      key: "orderNo",
      width: 200,
      render: (val) => <span className="order-no">{val}</span>,
    },
    {
      title: "品种",
      dataIndex: "instrumentCode",
      key: "instrumentCode",
      width: 100,
      render: (val, record) => (
        <span className="instrument-code">
          {record.instrumentName || val}
        </span>
      ),
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
      width: 90,
      render: (val) => <span className="price">{val?.toFixed(2)}</span>,
    },
    {
      title: "数量",
      dataIndex: "quantity",
      key: "quantity",
      width: 90,
      render: (val) => <span className="quantity">{val?.toFixed(0)}</span>,
    },
    {
      title: "已成交",
      dataIndex: "filledQuantity",
      key: "filledQuantity",
      width: 90,
      render: (val) => <span className="filled">{val?.toFixed(0)}</span>,
    },
    {
      title: "成交金额",
      dataIndex: "filledAmount",
      key: "filledAmount",
      width: 100,
      render: (val) => <span className="amount">{val?.toFixed(2)}</span>,
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
      title: "委托时间",
      dataIndex: "orderTime",
      key: "orderTime",
      width: 170,
      render: (val) => <span className="order-time">{val?.replace('T', ' ')}</span>,
    },
    {
      title: "成交时间",
      dataIndex: "filledTime",
      key: "filledTime",
      width: 170,
      render: (val) => (
        <span className="filled-time">
          {val ? val.replace('T', ' ') : '-'}
        </span>
      ),
    },
  ];

  const statusOptions = [
    { value: "", label: "全部" },
    { value: "PENDING", label: "待成交" },
    { value: "PARTIALLY_FILLED", label: "部分成交" },
    { value: "FILLED", label: "已成交" },
    { value: "CANCELLED", label: "已撤单" },
  ];

  return (
    <div className="orders-history-page">
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
        #root .ant-table-wrapper .ant-table-pagination {
          background: var(--color-bg-secondary) !important;
        }
        #root .ant-table-wrapper .ant-empty-description {
          color: var(--text-tertiary) !important;
        }
      `}</style>

      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-title">
          <FileTextOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">历史委托</h1>
            <span className="page-title-en">ORDER HISTORY</span>
          </div>
        </div>
      </div>

      {/* 筛选区域 */}
      <div className="filter-bar">
        <div className="filter-item">
          <label className="filter-label">状态筛选</label>
          <Select
            value={selectedStatus}
            onChange={handleStatusChange}
            className="filter-select"
            dropdownClassName="custom-dropdown"
            style={{ width: 150 }}
          >
            {statusOptions.map((opt) => (
              <Option key={opt.value} value={opt.value}>
                {opt.label}
              </Option>
            ))}
          </Select>
        </div>
        <div className="filter-item" style={{ marginLeft: 'auto' }}>
          <Button
            type="primary"
            icon={<ThunderboltOutlined />}
            onClick={getOrders}
            loading={loading}
          >
            刷新列表
          </Button>
        </div>
      </div>

      {/* 数据表格 */}
      <div className="data-table-container">
        <Table
          columns={orderColumns}
          dataSource={orders}
          rowKey="orderNo"
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
          loading={loading}
          scroll={{ x: 1400 }}
          className="orders-table"
        />
      </div>

      {/* 统计信息 */}
      {orders.length > 0 && (
        <div className="summary-bar">
          <div className="summary-item">
            <span className="summary-label">总委托数</span>
            <span className="summary-value">{orders.length}</span>
          </div>
          <div className="summary-item">
            <span className="summary-label">已成交</span>
            <span className="summary-value rise">
              {orders.filter(o => o.status === "FILLED" || o.status === "PARTIALLY_FILLED").length}
            </span>
          </div>
          <div className="summary-item">
            <span className="summary-label">待成交</span>
            <span className="summary-value pending">
              {orders.filter(o => o.status === "PENDING").length}
            </span>
          </div>
          <div className="summary-item">
            <span className="summary-label">已撤单</span>
            <span className="summary-value cancelled">
              {orders.filter(o => o.status === "CANCELLED").length}
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrdersHistory;
