import React, { useState, useEffect } from "react";
import { UserOutlined, MailOutlined, CalendarOutlined, TrophyOutlined, RiseOutlined, WalletOutlined } from "@ant-design/icons";
import { Input, Button, Upload, message, Avatar } from "antd";
import axios from "axios";

const Profile = () => {
  const [user, setUser] = useState(null);
  const [stats, setStats] = useState(null);
  const [editing, setEditing] = useState(false);
  const [nickname, setNickname] = useState("");
  const [avatar, setAvatar] = useState("");

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const res = await axios.get("/api/user/info");
      if (res.data.code === 0) {
        setUser(res.data.data);
        setNickname(res.data.data.nickname);
        setAvatar(res.data.data.avatar);
      }
    } catch (error) {
      console.error("获取用户信息失败", error);
    }
  };

  // 获取用户统计
  const getUserStats = async () => {
    try {
      const res = await axios.get("/api/user/stats");
      if (res.data.code === 0) {
        setStats(res.data.data);
      }
    } catch (error) {
      console.error("获取用户统计失败", error);
    }
  };

  useEffect(() => {
    getUserInfo();
    getUserStats();
  }, []);

  // 更新用户信息
  const handleUpdateProfile = async () => {
    try {
      const res = await axios.post("/api/user/profile", {
        nickname,
        avatar,
      });
      if (res.data.code === 0) {
        message.success("更新成功");
        setEditing(false);
        getUserInfo();
      }
    } catch (error) {
      message.error("更新失败");
    }
  };

  // 计算加入时长
  const calculateJoinDuration = (createdAt) => {
    if (!createdAt) return "";
    const joinDate = new Date(createdAt);
    const now = new Date();
    const diffMs = now - joinDate;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays < 30) {
      return `${diffDays}天`;
    } else if (diffDays < 365) {
      return `${Math.floor(diffDays / 30)}个月`;
    } else {
      return `${Math.floor(diffDays / 365)}年${Math.floor((diffDays % 365) / 30)}个月`;
    }
  };

  if (!user) {
    return <div className="loading-state">加载中...</div>;
  }

  return (
    <div className="profile-page">
      <div className="page-header">
        <h1 className="page-title">个人中心</h1>
        <span className="page-subtitle">PERSONAL CENTER</span>
      </div>

      <div className="profile-content">
        {/* 左侧：个人信息 */}
        <div className="profile-section">
          <div className="section-header">
            <UserOutlined className="section-icon" />
            <span className="section-title">个人信息</span>
          </div>

          <div className="profile-card">
            <div className="profile-avatar-section">
              <Avatar src={avatar} size={100} icon={<UserOutlined />} />
              <div className="avatar-upload">
                <Input
                  value={avatar}
                  onChange={(e) => setAvatar(e.target.value)}
                  placeholder="头像 URL"
                  disabled={!editing}
                  className="avatar-input"
                />
              </div>
            </div>

            <div className="profile-info">
              <div className="info-row">
                <span className="info-label"><UserOutlined /> 昵称</span>
                {editing ? (
                  <Input
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    className="info-input"
                  />
                ) : (
                  <span className="info-value">{nickname || "未设置"}</span>
                )}
              </div>

              <div className="info-row">
                <span className="info-label"><MailOutlined /> 用户 ID</span>
                <span className="info-value">{user.id}</span>
              </div>

              <div className="info-row">
                <span className="info-label"><CalendarOutlined /> 加入时间</span>
                <span className="info-value">
                  {user.createdAt ? new Date(user.createdAt).toLocaleDateString("zh-CN") : "-"}
                </span>
              </div>

              <div className="info-row">
                <span className="info-label"><CalendarOutlined /> 加入时长</span>
                <span className="info-value">{calculateJoinDuration(user.createdAt)}</span>
              </div>

              <div className="info-row">
                <span className="info-label">角色</span>
                <span className={`role-tag ${user.role === "ADMIN" ? "admin" : "user"}`}>
                  {user.role === "ADMIN" ? "管理员" : "普通用户"}
                </span>
              </div>
            </div>

            <div className="profile-actions">
              {editing ? (
                <>
                  <Button type="primary" onClick={handleUpdateProfile}>保存</Button>
                  <Button onClick={() => {
                    setEditing(false);
                    setNickname(user.nickname);
                    setAvatar(user.avatar);
                  }}>取消</Button>
                </>
              ) : (
                <Button type="primary" onClick={() => setEditing(true)}>编辑资料</Button>
              )}
            </div>
          </div>
        </div>

        {/* 右侧：投资战绩 */}
        <div className="profile-section">
          <div className="section-header">
            <TrophyOutlined className="section-icon" />
            <span className="section-title">投资战绩</span>
          </div>

          {stats ? (
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon" style={{ background: "rgba(24, 144, 255, 0.1)" }}>
                  <WalletOutlined style={{ color: "#1890ff", fontSize: 24 }} />
                </div>
                <div className="stat-content">
                  <div className="stat-label">总资产</div>
                  <div className="stat-value">
                    ¥{stats.totalAsset?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || "0.00"}
                  </div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ background: "rgba(82, 196, 26, 0.1)" }}>
                  <RiseOutlined style={{ color: "#52c41a", fontSize: 24 }} />
                </div>
                <div className="stat-content">
                  <div className="stat-label">总收益</div>
                  <div className={`stat-value ${stats.totalProfitLoss >= 0 ? "profit" : "loss"}`}>
                    {stats.totalProfitLoss >= 0 ? "+" : ""}{stats.totalProfitLoss?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || "0.00"}
                  </div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ background: "rgba(255, 77, 79, 0.1)" }}>
                  <RiseOutlined style={{ color: "#ff4d4f", fontSize: 24 }} />
                </div>
                <div className="stat-content">
                  <div className="stat-label">今日收益</div>
                  <div className={`stat-value ${stats.todayProfitLoss >= 0 ? "profit" : "loss"}`}>
                    {stats.todayProfitLoss >= 0 ? "+" : ""}{stats.todayProfitLoss?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || "0.00"}
                  </div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ background: "rgba(250, 173, 20, 0.1)" }}>
                  <TrophyOutlined style={{ color: "#faad14", fontSize: 24 }} />
                </div>
                <div className="stat-content">
                  <div className="stat-label">收益率</div>
                  <div className={`stat-value ${stats.totalReturnRate >= 0 ? "profit" : "loss"}`}>
                    {stats.totalReturnRate >= 0 ? "+" : ""}{(stats.totalReturnRate || 0).toFixed(2)}%
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="empty-state">暂无战绩数据</div>
          )}
        </div>
      </div>

      <style jsx>{`
        .profile-page {
          padding: 24px;
          max-width: 1400px;
          margin: 0 auto;
        }

        .page-header {
          margin-bottom: 24px;
        }

        .page-title {
          font-size: 24px;
          font-weight: 600;
          color: #e6edf3;
          margin: 0;
        }

        .page-subtitle {
          font-size: 12px;
          color: #8b949e;
          text-transform: uppercase;
          letter-spacing: 2px;
        }

        .profile-content {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 24px;
        }

        .profile-section {
          background: #0d1117;
          border: 1px solid #30363d;
          border-radius: 8px;
          padding: 20px;
        }

        .section-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 20px;
          padding-bottom: 12px;
          border-bottom: 1px solid #30363d;
        }

        .section-icon {
          font-size: 18px;
          color: #1890ff;
        }

        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #e6edf3;
        }

        .profile-card {
          display: flex;
          flex-direction: column;
          gap: 20px;
        }

        .profile-avatar-section {
          display: flex;
          align-items: center;
          gap: 20px;
          padding-bottom: 20px;
          border-bottom: 1px solid #30363d;
        }

        .avatar-upload {
          flex: 1;
        }

        .avatar-input {
          background: #161b22;
          border: 1px solid #30363d;
          color: #e6edf3;
        }

        .profile-info {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .info-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .info-label {
          color: #8b949e;
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
        }

        .info-value {
          color: #e6edf3;
          font-size: 14px;
        }

        .info-input {
          width: 200px;
          background: #161b22;
          border: 1px solid #30363d;
          color: #e6edf3;
        }

        .role-tag {
          padding: 4px 12px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;
        }

        .role-tag.admin {
          background: rgba(255, 77, 79, 0.15);
          color: #ff4d4f;
        }

        .role-tag.user {
          background: rgba(24, 144, 255, 0.15);
          color: #1890ff;
        }

        .profile-actions {
          display: flex;
          gap: 12px;
          padding-top: 12px;
          border-top: 1px solid #30363d;
        }

        .stats-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 16px;
        }

        .stat-card {
          background: #161b22;
          border: 1px solid #30363d;
          border-radius: 8px;
          padding: 20px;
          display: flex;
          align-items: center;
          gap: 16px;
        }

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .stat-content {
          flex: 1;
        }

        .stat-label {
          font-size: 12px;
          color: #8b949e;
          margin-bottom: 4px;
        }

        .stat-value {
          font-size: 20px;
          font-weight: 600;
          color: #e6edf3;
        }

        .stat-value.profit {
          color: #52c41a;
        }

        .stat-value.loss {
          color: #ff4d4f;
        }

        .loading-state, .empty-state {
          display: flex;
          justify-content: center;
          align-items: center;
          height: 200px;
          color: #8b949e;
          font-size: 14px;
        }

        @media (max-width: 768px) {
          .profile-content {
            grid-template-columns: 1fr;
          }

          .stats-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
};

export default Profile;
