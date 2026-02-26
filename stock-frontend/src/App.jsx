import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ConfigProvider, theme } from "antd";
import zhCN from "antd/locale/zh_CN";

// 页面导入
import Login from "./pages/Login";
import Layout from "./layouts/BasicLayout";
import Market from "./pages/Market";
import Trade from "./pages/Trade";
import Position from "./pages/Position";
import Admin from "./pages/Admin";

// 简单路由守卫
const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  return (
    <ConfigProvider locale={zhCN} theme={{ algorithm: theme.defaultAlgorithm }}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/market" replace />} />
            <Route path="market" element={<Market />} />
            <Route path="trade" element={<Trade />} />
            <Route path="position" element={<Position />} />
            <Route path="admin" element={<Admin />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
