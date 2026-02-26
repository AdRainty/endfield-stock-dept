import React, { useState, useEffect } from "react";
import { TrophyOutlined, GoldOutlined, CrownOutlined, CalendarOutlined, RiseOutlined } from "@ant-design/icons";
import { Tabs } from "antd";
import axios from "axios";

const Leaderboard = () => {
  const [dailyRank, setDailyRank] = useState([]);
  const [totalRank, setTotalRank] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("daily");

  // 获取排行榜数据
  const getLeaderboard = async (type) => {
    setLoading(true);
    try {
      const res = await axios.get(`/api/user/leaderboard?type=${type}`);
      if (res.data.code === 0) {
        if (type === "daily") {
          setDailyRank(res.data.data);
        } else {
          setTotalRank(res.data.data);
        }
      }
    } catch (error) {
      console.error("获取排行榜失败", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getLeaderboard("daily");
    getLeaderboard("total");
  }, []);

  // 计算加入时长
  const calculateJoinDuration = (createdAt) => {
    if (!createdAt) return "--";
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

  // 获取排名图标
  const getRankIcon = (rank) => {
    if (rank === 1) return <CrownOutlined style={{ color: "#ffd700", fontSize: 24 }} />;
    if (rank === 2) return <GoldOutlined style={{ color: "#c0c0c0", fontSize: 24 }} />;
    if (rank === 3) return <TrophyOutlined style={{ color: "#cd7f32", fontSize: 24 }} />;
    return <span className="rank-number">{rank}</span>;
  };

  const renderRankList = (data) => (
    <div className="rank-list">
      {data.length === 0 ? (
        <div className="empty-state">暂无排名数据</div>
      ) : (
        data.map((item, index) => (
          <div
            key={item.userId}
            className={`rank-item ${index < 3 ? "top-rank" : ""}`}
          >
            <div className="rank-info">
              <div className="rank-avatar">
                {getRankIcon(index + 1)}
              </div>
              <div className="rank-user">
                <div className="user-nickname">{item.nickname || `用户${item.userId}`}</div>
                <div className="user-join">
                  <CalendarOutlined /> 加入 {calculateJoinDuration(item.createdAt)}
                </div>
              </div>
            </div>
            <div className="rank-profit">
              <RiseOutlined />
              <span className={`profit-value ${item.profitLoss >= 0 ? "positive" : "negative"}`}>
                {item.profitLoss >= 0 ? "+" : ""}{item.profitLoss?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
              <span className="profit-rate">
                {item.returnRate >= 0 ? "+" : ""}{(item.returnRate || 0).toFixed(2)}%
              </span>
            </div>
          </div>
        ))
      )}
    </div>
  );

  return (
    <div className="leaderboard-page">
      <div className="page-header">
        <h1 className="page-title">
          <TrophyOutlined className="title-icon" />
          实盘排行榜
        </h1>
        <span className="page-subtitle">TRADING LEADERBOARD</span>
      </div>

      <div className="leaderboard-content">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: "daily",
              label: "日收益榜",
              children: loading ? (
                <div className="loading-state">加载中...</div>
              ) : (
                renderRankList(dailyRank)
              ),
            },
            {
              key: "total",
              label: "总收益榜",
              children: loading ? (
                <div className="loading-state">加载中...</div>
              ) : (
                renderRankList(totalRank)
              ),
            },
          ]}
          className="leaderboard-tabs"
        />
      </div>

      <style jsx>{`
        .leaderboard-page {
          padding: 24px;
          max-width: 1000px;
          margin: 0 auto;
        }

        .page-header {
          display: flex;
          flex-direction: column;
          align-items: center;
          margin-bottom: 24px;
          text-align: center;
        }

        .page-title {
          font-size: 28px;
          font-weight: 600;
          color: #e6edf3;
          margin: 0;
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .title-icon {
          color: #ffd700;
          font-size: 32px;
        }

        .page-subtitle {
          font-size: 12px;
          color: #8b949e;
          text-transform: uppercase;
          letter-spacing: 2px;
          margin-top: 4px;
        }

        .leaderboard-content {
          background: #0d1117;
          border: 1px solid #30363d;
          border-radius: 8px;
          padding: 20px;
        }

        .leaderboard-tabs :global(.ant-tabs-nav-list) {
          margin: 0 auto;
        }

        .leaderboard-tabs :global(.ant-tabs-tab) {
          color: #8b949e;
          font-size: 15px;
        }

        .leaderboard-tabs :global(.ant-tabs-tab-active) {
          color: #1890ff;
        }

        .leaderboard-tabs :global(.ant-tabs-ink-bar) {
          background: #1890ff;
        }

        .rank-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
          padding: 8px 0;
        }

        .rank-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px 20px;
          background: #161b22;
          border: 1px solid #30363d;
          border-radius: 8px;
          transition: all 0.2s;
        }

        .rank-item:hover {
          border-color: #1890ff;
          transform: translateX(4px);
        }

        .rank-item.top-rank {
          background: linear-gradient(135deg, rgba(255, 215, 0, 0.08) 0%, rgba(22, 27, 34, 0.8) 100%);
          border-color: rgba(255, 215, 0, 0.3);
        }

        .rank-info {
          display: flex;
          align-items: center;
          gap: 16px;
        }

        .rank-avatar {
          width: 48px;
          height: 48px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #0d1117;
          border-radius: 50%;
        }

        .rank-number {
          font-size: 20px;
          font-weight: 600;
          color: #8b949e;
        }

        .rank-user {
          display: flex;
          flex-direction: column;
          gap: 4px;
        }

        .user-nickname {
          font-size: 15px;
          font-weight: 500;
          color: #e6edf3;
        }

        .user-join {
          font-size: 12px;
          color: #8b949e;
          display: flex;
          align-items: center;
          gap: 4px;
        }

        .rank-profit {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 4px;
        }

        .profit-value {
          font-size: 18px;
          font-weight: 600;
        }

        .profit-value.positive {
          color: #52c41a;
        }

        .profit-value.negative {
          color: #ff4d4f;
        }

        .profit-rate {
          font-size: 13px;
          color: #8b949e;
        }

        .loading-state, .empty-state {
          display: flex;
          justify-content: center;
          align-items: center;
          height: 200px;
          color: #8b949e;
          font-size: 14px;
        }
      `}</style>
    </div>
  );
};

export default Leaderboard;
