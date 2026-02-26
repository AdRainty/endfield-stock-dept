import React, { useState, useEffect } from "react";
import { Table, Button, Modal, Form, InputNumber, Select, message, Input, Tag } from "antd";
import { SettingOutlined, PlusOutlined, UserOutlined, DollarOutlined } from "@ant-design/icons";
import axios from "axios";

const { Option } = Select;
const { TextArea } = Input;

const Admin = () => {
  const [users, setUsers] = useState([]);
  const [allocations, setAllocations] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [allocateModalVisible, setAllocateModalVisible] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [allocateForm] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const getUsers = async () => {
    try {
      const res = await axios.get("/api/admin/users");
      if (res.data.code === 0) {
        setUsers(res.data.data);
      }
    } catch (error) {
      console.error("获取用户列表失败", error);
    }
  };

  const getAllocations = async () => {
    try {
      const res = await axios.get("/api/admin/allocations");
      if (res.data.code === 0) {
        setAllocations(res.data.data);
      }
    } catch (error) {
      console.error("获取分配记录失败", error);
    }
  };

  const getStatistics = async () => {
    try {
      const res = await axios.get("/api/admin/statistics");
      if (res.data.code === 0) {
        setStatistics(res.data.data);
      }
    } catch (error) {
      console.error("获取统计数据失败", error);
    }
  };

  useEffect(() => {
    getUsers();
    getAllocations();
    getStatistics();
  }, []);

  const showAllocateModal = (user) => {
    setSelectedUser(user);
    setAllocateModalVisible(true);
    allocateForm.resetFields();
  };

  const handleAllocate = async () => {
    try {
      const values = await allocateForm.validateFields();
      const res = await axios.post("/api/admin/allocate", {
        targetUserId: selectedUser.id,
        exchangeId: values.exchangeId,
        amount: values.amount,
        reason: values.reason,
      });
      if (res.data.code === 0) {
        message.success("分配成功");
        setAllocateModalVisible(false);
        getAllocations();
        getStatistics();
      }
    } catch (error) {
      message.error(error.response?.data?.message || "分配失败");
    }
  };

  const updateUserStatus = async (userId, status) => {
    try {
      const res = await axios.post(`/api/admin/user/${userId}/status?status=${status}`);
      if (res.data.code === 0) {
        message.success("更新成功");
        getUsers();
      }
    } catch (error) {
      message.error("更新失败");
    }
  };

  const userColumns = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
      width: 70,
      render: (val) => <span className="user-id">#{val}</span>,
    },
    {
      title: "昵称",
      dataIndex: "nickname",
      key: "nickname",
      width: 150,
      render: (val) => (
        <span className="user-nickname">
          <UserOutlined /> {val}
        </span>
      ),
    },
    {
      title: "微信 OpenID",
      dataIndex: "wechatOpenid",
      key: "wechatOpenid",
      ellipsis: true,
      render: (val) => <span className="user-openid">{val}</span>,
    },
    {
      title: "角色",
      dataIndex: "role",
      key: "role",
      width: 100,
      render: (val) => (
        <Tag className={`role-tag ${val === "ADMIN" ? 'admin' : 'user'}`}>
          {val === "ADMIN" ? "管理员" : "普通用户"}
        </Tag>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (val) => (
        <Tag className={`status-tag ${val === 1 ? 'active' : 'inactive'}`}>
          {val === 1 ? "正常" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "注册时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <div className="action-buttons">
          <Button
            size="small"
            className="allocate-btn"
            onClick={() => showAllocateModal(record)}
          >
            <DollarOutlined /> 分配
          </Button>
          <Button
            size="small"
            className={`status-btn ${record.status === 1 ? 'danger' : 'success'}`}
            onClick={() => updateUserStatus(record.id, record.status === 1 ? 0 : 1)}
          >
            {record.status === 1 ? "禁用" : "启用"}
          </Button>
        </div>
      ),
    },
  ];

  const allocationColumns = [
    {
      title: "分配单号",
      dataIndex: "allocationNo",
      key: "allocationNo",
      width: 200,
      render: (val) => <span className="allocation-no">{val}</span>,
    },
    {
      title: "用户 ID",
      dataIndex: "userId",
      key: "userId",
      width: 80,
      render: (val) => <span className="user-id">#{val}</span>,
    },
    {
      title: "交易所 ID",
      dataIndex: "exchangeId",
      key: "exchangeId",
      width: 90,
    },
    {
      title: "分配金额",
      dataIndex: "amount",
      key: "amount",
      width: 110,
      render: (val) => <span className="allocation-amount">+{val?.toFixed(2)}</span>,
    },
    {
      title: "分配后余额",
      dataIndex: "balanceAfter",
      key: "balanceAfter",
      width: 120,
      render: (val) => <span className="balance">{val?.toFixed(2)}</span>,
    },
    {
      title: "原因",
      dataIndex: "reason",
      key: "reason",
      ellipsis: true,
    },
    {
      title: "操作时间",
      dataIndex: "operateTime",
      key: "operateTime",
      width: 180,
    },
  ];

  return (
    <div className="admin-page">
      {/* 页面头部 */}
      <div className="page-header">
        <div className="header-title">
          <SettingOutlined className="title-icon" />
          <div className="title-content">
            <h1 className="page-title-cn">管理后台</h1>
            <span className="page-title-en">ADMINISTRATION</span>
          </div>
        </div>
      </div>

      {/* 统计卡片 */}
      {statistics && (
        <div className="stats-cards">
          <div className="stat-card">
            <div className="stat-icon total">
              <UserOutlined />
            </div>
            <div className="stat-content">
              <span className="stat-label">总用户数</span>
              <span className="stat-value">{statistics.totalUsers}</span>
            </div>
          </div>
          <div className="stat-card normal">
            <div className="stat-icon normal">
              <UserOutlined />
            </div>
            <div className="stat-content">
              <span className="stat-label">正常用户</span>
              <span className="stat-value normal">{statistics.normalUsers}</span>
            </div>
          </div>
          <div className="stat-card admin">
            <div className="stat-icon admin">
              <SettingOutlined />
            </div>
            <div className="stat-content">
              <span className="stat-label">管理员数</span>
              <span className="stat-value admin">{statistics.adminUsers}</span>
            </div>
          </div>
          <div className="stat-card allocated">
            <div className="stat-icon allocated">
              <DollarOutlined />
            </div>
            <div className="stat-content">
              <span className="stat-label">累计分配</span>
              <span className="stat-value allocated">{statistics.totalAllocated?.toFixed(2)}</span>
              <span className="stat-unit">CNY</span>
            </div>
          </div>
        </div>
      )}

      {/* 用户管理表格 */}
      <div className="admin-panel">
        <div className="panel-header">
          <span className="panel-title">
            <span className="title-dot"></span>
            用户管理 / USER MANAGEMENT
          </span>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={getUsers}
            className="refresh-btn"
          >
            刷新
          </Button>
        </div>
        <div className="table-container">
          <Table
            columns={userColumns}
            dataSource={users}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            scroll={{ x: 1200 }}
            className="admin-table"
          />
        </div>
      </div>

      {/* 分配记录表格 */}
      <div className="admin-panel">
        <div className="panel-header">
          <span className="panel-title">
            <span className="title-dot"></span>
            分配记录 / ALLOCATION RECORDS
          </span>
        </div>
        <div className="table-container">
          <Table
            columns={allocationColumns}
            dataSource={allocations}
            rowKey="allocationNo"
            pagination={{ pageSize: 10 }}
            scroll={{ x: 1000 }}
            className="admin-table"
          />
        </div>
      </div>

      {/* 分配弹窗 */}
      <Modal
        title={
          <div className="modal-title">
            <DollarOutlined className="modal-icon" />
            <span>分配原能 - {selectedUser?.nickname}</span>
          </div>
        }
        open={allocateModalVisible}
        onOk={handleAllocate}
        onCancel={() => setAllocateModalVisible(false)}
        width={480}
        className="allocate-modal"
        okText="确认分配"
        cancelText="取消"
      >
        <Form form={allocateForm} layout="vertical">
          <Form.Item
            name="exchangeId"
            label="交易所"
            rules={[{ required: true, message: "请选择交易所" }]}
          >
            <Select>
              <Option value={1}>四号谷底交易所</Option>
              <Option value={2}>武陵交易所</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="amount"
            label="分配金额"
            rules={[{ required: true, message: "请输入金额" }]}
          >
            <InputNumber min={1} placeholder="请输入分配金额" style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="reason"
            label="分配原因"
            rules={[{ required: true, message: "请输入原因" }]}
          >
            <TextArea rows={4} placeholder="请输入分配原因" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Admin;
