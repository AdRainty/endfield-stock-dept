import React, { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { Layout, Menu, Avatar, Dropdown, message } from "antd";
import {
  LineChartOutlined,
  TradeOutlined,
  WalletOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from "@ant-design/icons";
import axios from "axios";

const { Header, Sider, Content } = Layout;

const BasicLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const user = JSON.parse(localStorage.getItem("user") || "{}");

  const menuItems = [
    {
      key: "/market",
      icon: <LineChartOutlined />,
      label: "行情中心",
    },
    {
      key: "/trade",
      icon: <TradeOutlined />,
      label: "交易委托",
    },
    {
      key: "/position",
      icon: <WalletOutlined />,
      label: "持仓查询",
    },
    ...(user.role === "ADMIN"
      ? [
          {
            key: "/admin",
            icon: <SettingOutlined />,
            label: "管理后台",
          },
        ]
      : []),
  ];

  const handleLogout = async () => {
    try {
      await axios.post("/api/auth/logout");
    } catch (error) {
      console.error("登出失败", error);
    }
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    message.success("已退出登录");
    navigate("/login");
  };

  const dropdownMenu = {
    items: [
      {
        key: "profile",
        icon: <UserOutlined />,
        label: user.nickname || "用户",
      },
      {
        type: "divider",
      },
      {
        key: "logout",
        icon: <LogoutOutlined />,
        label: "退出登录",
        onClick: handleLogout,
      },
    ],
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark">
        <div
          style={{
            height: 64,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "white",
            fontSize: collapsed ? 14 : 16,
            fontWeight: "bold",
            background: "#001529",
          }}
        >
          {collapsed ? "终末地" : "终末地调度卷交易"}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: "0 16px",
            background: "#fff",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <button
            onClick={() => setCollapsed(!collapsed)}
            style={{
              background: "none",
              border: "none",
              fontSize: 18,
              cursor: "pointer",
            }}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </button>
          <Dropdown menu={dropdownMenu} placement="bottomRight">
            <div style={{ cursor: "pointer", display: "flex", alignItems: "center", gap: 8 }}>
              <Avatar src={user.avatar} icon={<UserOutlined />} />
              <span>{user.nickname || "用户"}</span>
            </div>
          </Dropdown>
        </Header>
        <Content
          style={{
            margin: 16,
            padding: 16,
            background: "#fff",
            borderRadius: 4,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default BasicLayout;
