import React, { useState, useEffect } from "react";
import { Card, Table, Button, Modal, Form, InputNumber, Select, message, Input, Tag } from "antd";
import { SettingOutlined, PlusOutlined } from "@ant-design/icons";
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

  // 获取用户列表
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

  // 获取分配记录
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

  // 获取统计数据
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

  // 打开分配弹窗
  const showAllocateModal = (user) => {
    setSelectedUser(user);
    setAllocateModalVisible(true);
    allocateForm.resetFields();
  };

  // 提交分配
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

  // 更新用户状态
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
      width: 60,
    },
    {
      title: "昵称",
      dataIndex: "nickname",
      key: "nickname",
    },
    {
      title: "微信 OpenID",
      dataIndex: "wechatOpenid",
      key: "wechatOpenid",
      ellipsis: true,
    },
    {
      title: "角色",
      dataIndex: "role",
      key: "role",
      render: (val) => (
        <Tag color={val === "ADMIN" ? "red" : "blue"}>
          {val === "ADMIN" ? "管理员" : "普通用户"}
        </Tag>
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (val) => (
        <Tag color={val === 1 ? "green" : "red"}>
          {val === 1 ? "正常" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "注册时间",
      dataIndex: "createdAt",
      key: "createdAt",
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_, record) => (
        <>
          <Button
            size="small"
            type="link"
            onClick={() => showAllocateModal(record)}
          >
            分配原能
          </Button>
          <Button
            size="small"
            type="link"
            danger={record.status === 1}
            onClick={() => updateUserStatus(record.id, record.status === 1 ? 0 : 1)}
          >
            {record.status === 1 ? "禁用" : "启用"}
          </Button>
        </>
      ),
    },
  ];

  const allocationColumns = [
    {
      title: "分配单号",
      dataIndex: "allocationNo",
      key: "allocationNo",
      ellipsis: true,
    },
    {
      title: "用户 ID",
      dataIndex: "userId",
      key: "userId",
      width: 80,
    },
    {
      title: "交易所 ID",
      dataIndex: "exchangeId",
      key: "exchangeId",
      width: 80,
    },
    {
      title: "分配金额",
      dataIndex: "amount",
      key: "amount",
      render: (val) => <span style={{ color: "#cf1322" }}>+{val?.toFixed(2)}</span>,
    },
    {
      title: "分配后余额",
      dataIndex: "balanceAfter",
      key: "balanceAfter",
      render: (val) => val?.toFixed(2),
    },
    {
      title: "原因",
      dataIndex: "reason",
      key: "reason",
    },
    {
      title: "操作时间",
      dataIndex: "operateTime",
      key: "operateTime",
    },
  ];

  return (
    <div>
      {statistics && (
        <Card style={{ marginBottom: 16 }}>
          <Card.Grid style={{ width: "25%", textAlign: "center" }}>
            <div style={{ fontSize: 14, color: "#666" }}>总用户数</div>
            <div style={{ fontSize: 24, fontWeight: "bold" }}>{statistics.totalUsers}</div>
          </Card.Grid>
          <Card.Grid style={{ width: "25%", textAlign: "center" }}>
            <div style={{ fontSize: 14, color: "#666" }}>正常用户</div>
            <div style={{ fontSize: 24, fontWeight: "bold", color: "#52c41a" }}>{statistics.normalUsers}</div>
          </Card.Grid>
          <Card.Grid style={{ width: "25%", textAlign: "center" }}>
            <div style={{ fontSize: 14, color: "#666" }}>管理员数</div>
            <div style={{ fontSize: 24, fontWeight: "bold", color: "#1890ff" }}>{statistics.adminUsers}</div>
          </Card.Grid>
          <Card.Grid style={{ width: "25%", textAlign: "center" }}>
            <div style={{ fontSize: 14, color: "#666" }}>累计分配</div>
            <div style={{ fontSize: 24, fontWeight: "bold", color: "#fa8c16" }}>{statistics.totalAllocated?.toFixed(2)}</div>
          </Card.Grid>
        </Card>
      )}

      <Card
        title={<span><SettingOutlined /> 用户管理</span>}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={getUsers}>
            刷新
          </Button>
        }
        style={{ marginBottom: 16 }}
      >
        <Table
          columns={userColumns}
          dataSource={users}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1000 }}
        />
      </Card>

      <Card title="分配记录">
        <Table
          columns={allocationColumns}
          dataSource={allocations}
          rowKey="allocationNo"
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1000 }}
        />
      </Card>

      <Modal
        title={`分配原能 - ${selectedUser?.nickname}`}
        open={allocateModalVisible}
        onOk={handleAllocate}
        onCancel={() => setAllocateModalVisible(false)}
        width={500}
      >
        <Form form={allocateForm} layout="vertical">
          <Form.Item
            name="exchangeId"
            label="交易所"
            rules={[{ required: true, message: "请选择交易所" }]}
          >
            <Select>
              <Option value={1}>四号谷底</Option>
              <Option value={2}>武陵</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="amount"
            label="分配金额"
            rules={[{ required: true, message: "请输入金额" }]}
          >
            <InputNumber min={1} style={{ width: "100%" }} placeholder="请输入分配金额" />
          </Form.Item>
          <Form.Item
            name="reason"
            label="分配原因"
            rules={[{ required: true, message: "请输入原因" }]}
          >
            <TextArea rows={3} placeholder="请输入分配原因" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Admin;
