import React, { useState, useEffect, useMemo } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, Cell } from "recharts";
import { CaretUpOutlined, CaretDownOutlined } from "@ant-design/icons";

/**
 * K 线图表组件
 * 支持分时、日 K、月 K、年 K
 */
const KlineChart = ({ exchangeId, instrumentCode }) => {
  const [period, setPeriod] = useState("1d"); // 1m-分时 1d-日 K 1M-月 K 1Y-年 K
  const [klineData, setKlineData] = useState([]);
  const [loading, setLoading] = useState(false);

  // 周期选项
  const periodOptions = [
    { value: "1m", label: "分时" },
    { value: "1d", label: "日 K" },
    { value: "1M", label: "月 K" },
    { value: "1Y", label: "年 K" },
  ];

  // 获取 K 线数据
  const fetchKline = async () => {
    if (!exchangeId || !instrumentCode) return;
    setLoading(true);
    try {
      const limit = period === "1m" ? 240 : period === "1d" ? 60 : period === "1M" ? 24 : 10;
      const res = await fetch(
        `/api/market/kline?exchangeId=${exchangeId}&instrumentCode=${instrumentCode}&period=${period}&limit=${limit}`
      );
      const result = await res.json();
      if (result.code === 0) {
        setKlineData(result.data || []);
      }
    } catch (error) {
      console.error("获取 K 线数据失败", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchKline();
  }, [exchangeId, instrumentCode, period]);

  // 格式化时间显示
  const formatTime = (timeStr, periodType) => {
    const date = new Date(timeStr);
    switch (periodType) {
      case "1m":
        return date.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" });
      case "1d":
        return date.toLocaleDateString("zh-CN", { month: "2-digit", day: "2-digit" });
      case "1M":
        return date.toLocaleDateString("zh-CN", { month: "2-digit" });
      case "1Y":
        return date.getFullYear().toString();
      default:
        return timeStr;
    }
  };

  // 计算最高价和最低价用于 Y 轴范围
  const { yAxisDomain, priceChange } = useMemo(() => {
    if (klineData.length === 0) {
      return { yAxisDomain: [0, 100], priceChange: { amount: 0, percent: 0 } };
    }

    const high = Math.max(...klineData.map(d => parseFloat(d.high || 0)));
    const low = Math.min(...klineData.map(d => parseFloat(d.low || 0)));
    const padding = (high - low) * 0.1;

    const lastClose = klineData[klineData.length - 1]?.close || 0;
    const firstOpen = klineData[0]?.open || 0;
    const amount = lastClose - firstOpen;
    const percent = firstOpen > 0 ? (amount / firstOpen) * 100 : 0;

    return {
      yAxisDomain: [Math.max(0, low - padding), high + padding],
      priceChange: {
        amount: amount.toFixed(2),
        percent: percent.toFixed(2),
      },
    };
  }, [klineData]);

  // 自定义蜡烛图形状
  const Candlestick = (props) => {
    const { x, y, width, height, payload } = props;
    const { open, close, high, low } = payload;

    if (!open || !close || !high || !low) return null;

    const isRise = close >= open;
    const color = isRise ? "#ff4d4f" : "#52c41a";

    const yScale = height / (Math.max(...klineData.map(d => parseFloat(d.high || 0))) - Math.min(...klineData.map(d => parseFloat(d.low || 0))) || 1);

    const openY = y + (high - open) * yScale;
    const closeY = y + (high - close) * yScale;
    const highY = y + (high - high) * yScale;
    const lowY = y + (high - low) * yScale;

    const bodyTop = Math.min(openY, closeY);
    const bodyHeight = Math.max(Math.abs(openY - closeY), 1);

    return (
      <g>
        {/* 影线 */}
        <line x1={x + width / 2} y1={highY} x2={x + width / 2} y2={lowY} stroke={color} strokeWidth={1} />
        {/* 实体 */}
        <rect x={x + width * 0.1} y={bodyTop} width={width * 0.8} height={bodyHeight} fill={color} />
      </g>
    );
  };

  return (
    <div className="kline-chart-container">
      {/* 周期选择器 */}
      <div className="kline-header">
        <div className="kline-title">
          <span>K 线图表</span>
          {priceChange.amount !== "0.00" && (
            <span className={`kline-change ${parseFloat(priceChange.percent) >= 0 ? "rise" : "fall"}`}>
              {parseFloat(priceChange.percent) >= 0 ? "+" : ""}{priceChange.amount} ({parseFloat(priceChange.percent) >= 0 ? "+" : ""}{priceChange.percent}%)
            </span>
          )}
        </div>
        <div className="kline-period-selector">
          {periodOptions.map((opt) => (
            <button
              key={opt.value}
              className={`period-btn ${period === opt.value ? "active" : ""}`}
              onClick={() => setPeriod(opt.value)}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {/* 图表内容 */}
      <div className="kline-content">
        {loading ? (
          <div className="loading-state">加载中...</div>
        ) : klineData.length === 0 ? (
          <div className="empty-state">暂无 K 线数据</div>
        ) : (
          <div className="kline-chart">
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={klineData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#333" />
                <XAxis
                  dataKey="time"
                  tickFormatter={(t) => formatTime(t, period)}
                  stroke="#666"
                  tick={{ fontSize: 12 }}
                />
                <YAxis
                  domain={yAxisDomain}
                  stroke="#666"
                  tick={{ fontSize: 12 }}
                  tickFormatter={(v) => v.toFixed(2)}
                />
                <Tooltip
                  content={({ active, payload }) => {
                    if (active && payload && payload.length) {
                      const data = payload[0].payload;
                      return (
                        <div className="kline-tooltip">
                          <div className="tooltip-row"><span>时间:</span> {data.time}</div>
                          <div className="tooltip-row"><span>开盘:</span> {data.open?.toFixed(2)}</div>
                          <div className="tooltip-row"><span>最高:</span> {data.high?.toFixed(2)}</div>
                          <div className="tooltip-row"><span>最低:</span> {data.low?.toFixed(2)}</div>
                          <div className="tooltip-row"><span>收盘:</span> {data.close?.toFixed(2)}</div>
                          <div className="tooltip-row"><span>成交量:</span> {data.volume?.toLocaleString()}</div>
                        </div>
                      );
                    }
                    return null;
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="close"
                  stroke="#1890ff"
                  strokeWidth={1.5}
                  dot={false}
                  isAnimationActive={false}
                />
              </LineChart>
            </ResponsiveContainer>

            {/* 成交量图表 */}
            <ResponsiveContainer width="100%" height={80}>
              <BarChart data={klineData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#333" />
                <XAxis dataKey="time" hide />
                <YAxis hide />
                <Tooltip />
                <Bar dataKey="volume" fill="#1890ff" opacity={0.5}>
                  {klineData.map((entry, index) => (
                    <Cell
                      key={index}
                      fill={entry.close >= entry.open ? "#ff4d4f" : "#52c41a"}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <style jsx>{`
        .kline-chart-container {
          background: #0d1117;
          border: 1px solid #30363d;
          border-radius: 8px;
          padding: 16px;
          margin-top: 16px;
        }

        .kline-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
        }

        .kline-title {
          font-size: 16px;
          font-weight: 600;
          color: #e6edf3;
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .kline-change {
          font-size: 14px;
          font-weight: 500;
        }

        .kline-change.rise {
          color: #ff4d4f;
        }

        .kline-change.fall {
          color: #52c41a;
        }

        .kline-period-selector {
          display: flex;
          gap: 8px;
        }

        .period-btn {
          padding: 4px 12px;
          background: transparent;
          border: 1px solid #30363d;
          color: #8b949e;
          border-radius: 4px;
          cursor: pointer;
          font-size: 13px;
          transition: all 0.2s;
        }

        .period-btn:hover {
          border-color: #1890ff;
          color: #1890ff;
        }

        .period-btn.active {
          background: #1890ff;
          border-color: #1890ff;
          color: #fff;
        }

        .kline-content {
          min-height: 380px;
        }

        .loading-state,
        .empty-state {
          display: flex;
          justify-content: center;
          align-items: center;
          height: 380px;
          color: #8b949e;
          font-size: 14px;
        }

        .kline-chart {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .kline-tooltip {
          background: #161b22;
          border: 1px solid #30363d;
          border-radius: 4px;
          padding: 8px 12px;
          font-size: 12px;
        }

        .tooltip-row {
          display: flex;
          justify-content: space-between;
          gap: 16px;
          padding: 2px 0;
        }

        .tooltip-row span:first-child {
          color: #8b949e;
        }
      `}</style>
    </div>
  );
};

export default KlineChart;
