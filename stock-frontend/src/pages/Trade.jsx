import React, { useState, useEffect } from "react";
import { Table, Tag, Button, message } from "antd";
import { FileTextOutlined, ThunderboltOutlined } from "@ant-design/icons";
import axios from "axios";

const Trade = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  const getOrders = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/api/trade/orders");
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

  useEffect(() => {
    getOrders();
  }, []);

  const orderColumns = [
    {
      title: "订单号",
      dataIndex: "orderNo",
      key: "orderNo",
      width: 200,
      render: (val) => <span className="order-no">{val}</span>,
    },
    {
      title: "交易所",
      dataIndex: "exchangeName",
      key: "exchangeName",
      width: 100,
      render: (val) => <span className="exchange-name">{val}</span>,
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
    <div className="trade-page-simple">
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
          <FileTextOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">委托记录</h1>
            <span className="page-title-en">MY ORDERS</span>
          </div>
        </div>
      </div>

      {/* 操作栏 */}
      <div className="action-bar">
        <Button
          type="primary"
          icon={<ThunderboltOutlined />}
          onClick={getOrders}
          loading={loading}
        >
          刷新列表
        </Button>
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
          scroll={{ x: 1200 }}
          className="orders-table"
        />
      </div>
    </div>
  );
};

export default Trade;
