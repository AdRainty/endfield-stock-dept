import React, { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { Layout, Menu, Avatar, Dropdown, message } from "antd";
import {
  LineChartOutlined,
  WalletOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ThunderboltOutlined,
  DashboardOutlined,
  BankOutlined,
  FileTextOutlined,
  HomeOutlined,
  TrophyOutlined,
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
      key: "/welcome",
      icon: <HomeOutlined />,
      label: <span>首页 <span className="menu-sub">HOME</span></span>,
    },
    {
      key: "/market",
      icon: <LineChartOutlined />,
      label: <span>行情中心 <span className="menu-sub">MARKET</span></span>,
    },
    {
      key: "/orders",
      icon: <FileTextOutlined />,
      label: <span>历史委托 <span className="menu-sub">ORDERS</span></span>,
    },
    {
      key: "/position",
      icon: <WalletOutlined />,
      label: <span>持仓查询 <span className="menu-sub">POSITION</span></span>,
    },
    {
      key: "/leaderboard",
      icon: <TrophyOutlined />,
      label: <span>排行榜 <span className="menu-sub">LEADERBOARD</span></span>,
    },
    {
      key: "/profile",
      icon: <UserOutlined />,
      label: <span>个人中心 <span className="menu-sub">PROFILE</span></span>,
    },
    ...(user.role === "ADMIN"
      ? [
          {
            key: "/admin",
            icon: <SettingOutlined />,
            label: <span>管理后台 <span className="menu-sub">ADMIN</span></span>,
            children: [
              {
                key: "/admin/users",
                icon: <UserOutlined />,
                label: <span>用户管理 <span className="menu-sub">USER MANAGEMENT</span></span>,
              },
              {
                key: "/admin/exchange",
                icon: <BankOutlined />,
                label: <span>交易所管理 <span className="menu-sub">EXCHANGE MANAGEMENT</span></span>,
              },
            ],
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
        label: <span className="user-name">{user.nickname || "用户"}</span>,
      },
      {
        type: "divider",
      },
      {
        key: "logout",
        icon: <LogoutOutlined />,
        label: "退出登录",
        onClick: handleLogout,
        className: "logout-item",
      },
    ],
  };

  return (
    <Layout className="main-layout">
      {/* 顶部状态栏 */}
      <Header className="top-status-bar">
        <div className="status-left">
          <ThunderboltOutlined className="system-logo" />
          <span className="system-name">ENDFIELD TRADING SYSTEM</span>
          <span className="status-separator">/</span>
          <span className="connection-status">
            <span className="status-dot"></span>
            ONLINE
          </span>
        </div>
        <div className="status-right">
          <Dropdown menu={dropdownMenu} trigger={["click"]} className="user-dropdown">
            <span className="user-display">
              <UserOutlined /> {user.nickname || "USER"}
            </span>
          </Dropdown>
          <span className="role-tag">{user.role === "ADMIN" ? "ADMIN" : "USER"}</span>
        </div>
      </Header>

      <Layout className="layout-body">
        <Sider
          className="side-nav"
          theme="dark"
          collapsed={collapsed}
          collapsedWidth={70}
          width={220}
        >
          <div className="side-nav-header">
            <ThunderboltOutlined className="nav-logo-icon" />
            {!collapsed && <span className="nav-title">终末地交易系统</span>}
          </div>

          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
            className="side-menu"
            inlineIndent={16}
          />

          <div className="side-nav-footer">
            <button
              className="collapse-btn"
              onClick={() => setCollapsed(!collapsed)}
            >
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              {!collapsed && <span>收起菜单</span>}
            </button>
          </div>
        </Sider>

        <Layout className="content-area">
          <Content className="main-content">
            <Outlet />
          </Content>

          {/* 底部信息栏 */}
          <footer className="content-footer">
            <div className="footer-left">
              <span>SYSTEM v2.0.26</span>
              <span className="footer-sep">|</span>
              <span>LATENCY: 12ms</span>
            </div>
            <div className="footer-right">
              <span>ENDFIELD PROTOCOL</span>
              <span className="copyright">© 2026</span>
            </div>
          </footer>
        </Layout>
      </Layout>
    </Layout>
  );
};

export default BasicLayout;
