import React, { useState, useEffect } from "react";
import { Table, Button, Modal, Form, Input, Switch, message, Tag } from "antd";
import { BankOutlined, ShopOutlined, PlusOutlined, EditOutlined } from "@ant-design/icons";
import axios from "axios";

const AdminExchange = () => {
  const [exchanges, setExchanges] = useState([]);
  const [instruments, setInstruments] = useState([]);
  const [exchangeModalVisible, setExchangeModalVisible] = useState(false);
  const [instrumentModalVisible, setInstrumentModalVisible] = useState(false);
  const [editingExchange, setEditingExchange] = useState(null);
  const [editingInstrument, setEditingInstrument] = useState(null);
  const [exchangeForm] = Form.useForm();
  const [instrumentForm] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const getExchanges = async () => {
    try {
      const res = await axios.get("/api/exchange/list");
      if (res.data.code === 0) {
        setExchanges(res.data.data);
      }
    } catch (error) {
      console.error("获取交易所列表失败", error);
    }
  };

  const getInstruments = async () => {
    try {
      const res = await axios.get("/api/admin/instruments");
      if (res.data.code === 0) {
        setInstruments(res.data.data);
      }
    } catch (error) {
      console.error("获取品种列表失败", error);
    }
  };

  useEffect(() => {
    getExchanges();
    getInstruments();
  }, []);

  // ==================== 交易所管理 ====================
  const showExchangeModal = (exchange) => {
    setEditingExchange(exchange || null);
    setExchangeModalVisible(true);
    if (exchange) {
      exchangeForm.setFieldsValue(exchange);
    } else {
      exchangeForm.resetFields();
    }
  };

  const handleExchangeSubmit = async () => {
    try {
      const values = await exchangeForm.validateFields();
      const res = editingExchange
        ? await axios.post(`/api/admin/exchange/${editingExchange.id}`, values)
        : await axios.post("/api/admin/exchange", values);

      if (res.data.code === 0) {
        message.success(editingExchange ? "更新成功" : "创建成功");
        setExchangeModalVisible(false);
        getExchanges();
      }
    } catch (error) {
      message.error(error.response?.data?.message || "操作失败");
    }
  };

  const toggleExchangeStatus = async (exchangeId, status) => {
    try {
      const res = await axios.post(`/api/admin/exchange/${exchangeId}/status?status=${status}`);
      if (res.data.code === 0) {
        message.success("更新成功");
        getExchanges();
      }
    } catch (error) {
      message.error("更新失败");
    }
  };

  // ==================== 品种管理 ====================
  const showInstrumentModal = (instrument) => {
    setEditingInstrument(instrument || null);
    setInstrumentModalVisible(true);
    if (instrument) {
      instrumentForm.setFieldsValue(instrument);
    } else {
      instrumentForm.resetFields();
    }
  };

  const handleInstrumentSubmit = async () => {
    try {
      const values = await instrumentForm.validateFields();
      const res = editingInstrument
        ? await axios.post(`/api/admin/instrument/${editingInstrument.id}`, values)
        : await axios.post("/api/admin/instrument", values);

      if (res.data.code === 0) {
        message.success(editingInstrument ? "更新成功" : "创建成功");
        setInstrumentModalVisible(false);
        getInstruments();
      }
    } catch (error) {
      message.error(error.response?.data?.message || "操作失败");
    }
  };

  const toggleInstrumentStatus = async (instrumentId, status) => {
    try {
      const res = await axios.post(`/api/admin/instrument/${instrumentId}/status?status=${status}`);
      if (res.data.code === 0) {
        message.success("更新成功");
        getInstruments();
      }
    } catch (error) {
      message.error("更新失败");
    }
  };

  // ==================== 表格列定义 ====================
  const exchangeColumns = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
      width: 70,
      render: (val) => <span className="exchange-id">#{val}</span>,
    },
    {
      title: "交易所名称",
      dataIndex: "name",
      key: "name",
      width: 200,
      render: (val) => (
        <span className="exchange-name">
          <ShopOutlined /> {val}
        </span>
      ),
    },
    {
      title: "交易所代码",
      dataIndex: "code",
      key: "code",
      width: 150,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (val) => (
        <Tag className={`status-tag ${val === 1 ? 'active' : 'inactive'}`}>
          {val === 1 ? "正常" : "停用"}
        </Tag>
      ),
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <div className="action-buttons">
          <Button
            size="small"
            className="edit-btn"
            onClick={() => showExchangeModal(record)}
          >
            <EditOutlined /> 编辑
          </Button>
          <Switch
            size="small"
            checked={record.status === 1}
            onChange={(checked) => toggleExchangeStatus(record.id, checked ? 1 : 0)}
          />
        </div>
      ),
    },
  ];

  const instrumentColumns = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
      width: 70,
    },
    {
      title: "交易所",
      dataIndex: "exchangeName",
      key: "exchangeName",
      width: 150,
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
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "类型",
      dataIndex: "type",
      key: "type",
      width: 80,
      render: (val) => {
        const typeMap = {
          STOCK: { text: "股票", class: "stock" },
          FUND: { text: "基金", class: "fund" },
          BOND: { text: "债券", class: "bond" },
        };
        const type = typeMap[val] || { text: val, class: "" };
        return <Tag className={`type-tag ${type.class}`}>{type.text}</Tag>;
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      render: (val) => (
        <Tag className={`status-tag ${val === 1 ? 'active' : 'inactive'}`}>
          {val === 1 ? "正常" : "停用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <div className="action-buttons">
          <Button
            size="small"
            className="edit-btn"
            onClick={() => showInstrumentModal(record)}
          >
            <EditOutlined /> 编辑
          </Button>
          <Switch
            size="small"
            checked={record.status === 1}
            onChange={(checked) => toggleInstrumentStatus(record.id, checked ? 1 : 0)}
          />
        </div>
      ),
    },
  ];

  return (
    <div className="admin-page">
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
          <BankOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">交易所管理</h1>
            <span className="page-title-en">EXCHANGE MANAGEMENT</span>
          </div>
        </div>
      </div>

      {/* 交易所管理 */}
      <div className="admin-panel">
        <div className="panel-header">
          <span className="panel-title">
            <span className="title-dot"></span>
            交易所管理 / EXCHANGE
          </span>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => showExchangeModal()}
            className="add-btn"
          >
            添加交易所
          </Button>
        </div>
        <div className="table-container">
          <Table
            columns={exchangeColumns}
            dataSource={exchanges}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            scroll={{ x: 1000 }}
            className="admin-table"
          />
        </div>
      </div>

      {/* 交易品种管理 */}
      <div className="admin-panel">
        <div className="panel-header">
          <span className="panel-title">
            <span className="title-dot"></span>
            交易品种管理 / INSTRUMENTS
          </span>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => showInstrumentModal()}
            className="add-btn"
          >
            添加品种
          </Button>
        </div>
        <div className="table-container">
          <Table
            columns={instrumentColumns}
            dataSource={instruments}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            scroll={{ x: 1200 }}
            className="admin-table"
          />
        </div>
      </div>

      {/* 交易所编辑弹窗 */}
      <Modal
        title={
          <div className="modal-title">
            <ExchangeOutlined className="modal-icon" />
            <span>{editingExchange ? "编辑交易所" : "添加交易所"}</span>
          </div>
        }
        open={exchangeModalVisible}
        onOk={handleExchangeSubmit}
        onCancel={() => setExchangeModalVisible(false)}
        width={480}
        className="allocate-modal"
        okText="确认"
        cancelText="取消"
      >
        <Form form={exchangeForm} layout="vertical">
          <Form.Item
            name="name"
            label="交易所名称"
            rules={[{ required: true, message: "请输入名称" }]}
          >
            <Input placeholder="请输入交易所名称" />
          </Form.Item>
          <Form.Item
            name="code"
            label="交易所代码"
            rules={[{ required: true, message: "请输入代码" }]}
          >
            <Input placeholder="请输入交易所代码" />
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 品种编辑弹窗 */}
      <Modal
        title={
          <div className="modal-title">
            <ShopOutlined className="modal-icon" />
            <span>{editingInstrument ? "编辑品种" : "添加品种"}</span>
          </div>
        }
        open={instrumentModalVisible}
        onOk={handleInstrumentSubmit}
        onCancel={() => setInstrumentModalVisible(false)}
        width={480}
        className="allocate-modal"
        okText="确认"
        cancelText="取消"
      >
        <Form form={instrumentForm} layout="vertical">
          <Form.Item
            name="exchangeId"
            label="所属交易所"
            rules={[{ required: true, message: "请选择交易所" }]}
          >
            <Select>
              {exchanges.map((e) => (
                <Option key={e.id} value={e.id}>{e.name}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="instrumentCode"
            label="品种代码"
            rules={[{ required: true, message: "请输入代码" }]}
          >
            <Input placeholder="请输入品种代码" />
          </Form.Item>
          <Form.Item
            name="name"
            label="品种名称"
            rules={[{ required: true, message: "请输入名称" }]}
          >
            <Input placeholder="请输入品种名称" />
          </Form.Item>
          <Form.Item
            name="type"
            label="品种类型"
            rules={[{ required: true, message: "请选择类型" }]}
          >
            <Select>
              <Option value="STOCK">股票</Option>
              <Option value="FUND">基金</Option>
              <Option value="BOND">债券</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminExchange;
