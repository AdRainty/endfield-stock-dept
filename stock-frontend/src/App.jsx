import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// 页面导入
import Login from "./pages/Login";
import Layout from "./layouts/BasicLayout";
import Welcome from "./pages/Welcome";
import Market from "./pages/Market";
import Position from "./pages/Position";
import OrdersHistory from "./pages/OrdersHistory";
import AdminUsers from "./pages/AdminUsers";
import AdminExchange from "./pages/AdminExchange";
import Profile from "./pages/Profile";
import Leaderboard from "./pages/Leaderboard";

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
          <Route index element={<Navigate to="/orders" replace />} />
          <Route path="welcome" element={<Welcome />} />
          <Route path="market" element={<Market />} />
          <Route path="orders" element={<OrdersHistory />} />
          <Route path="position" element={<Position />} />
          <Route path="profile" element={<Profile />} />
          <Route path="leaderboard" element={<Leaderboard />} />
          <Route path="admin" element={<Navigate to="/admin/users" replace />} />
          <Route path="admin/users" element={<AdminUsers />} />
          <Route path="admin/exchange" element={<AdminExchange />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
