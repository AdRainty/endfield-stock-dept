import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// 页面导入
import Login from "./pages/Login";
import Layout from "./layouts/BasicLayout";
import Market from "./pages/Market";
import Trade from "./pages/Trade";
import Position from "./pages/Position";
import AdminUsers from "./pages/AdminUsers";
import AdminExchange from "./pages/AdminExchange";

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
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route index element={<Navigate to="/market" replace />} />
          <Route path="market" element={<Market />} />
          <Route path="trade" element={<Trade />} />
          <Route path="position" element={<Position />} />
          <Route path="admin" element={<Navigate to="/admin/users" replace />} />
          <Route path="admin/users" element={<AdminUsers />} />
          <Route path="admin/exchange" element={<AdminExchange />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
