import React, { useState, useEffect } from "react";
import { Card, QRCode, message, Tabs, Spin } from "antd";
import { WechatOutlined, UserOutlined, RocketOutlined, LoginOutlined, ThunderboltOutlined } from "@ant-design/icons";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Login = () => {
  const navigate = useNavigate();
  const [qrCodeValue, setQrCodeValue] = useState("");
  const [scene, setScene] = useState("");
  const [loading, setLoading] = useState(false);
  const [bootSequence, setBootSequence] = useState(true);

  // 启动序列效果
  useEffect(() => {
    const timer = setTimeout(() => setBootSequence(false), 2000);
    return () => clearTimeout(timer);
  }, []);

  // 获取二维码
  const getQrCode = async () => {
    try {
      const res = await axios.post("/api/auth/wx-qrcode");
      if (res.data.code === 0) {
        setScene(res.data.data.scene);
        setQrCodeValue(res.data.data.qrCodeBase64);
        pollQrStatus(res.data.data.scene);
      }
    } catch (error) {
      console.error("获取二维码失败", error);
    }
  };

  const pollQrStatus = (scene) => {
    const timer = setInterval(async () => {
      try {
        const res = await axios.get(`/api/auth/wx-qrcode/${scene}`);
        if (res.data.code === 0) {
          const { status, user, token } = res.data.data;
          if (status === "SUCCESS" && token) {
            clearInterval(timer);
            localStorage.setItem("token", token);
            localStorage.setItem("user", JSON.stringify(user));
            message.success("登录成功");
            navigate("/");
          } else if (status === "EXPIRED") {
            clearInterval(timer);
            message.error("二维码已过期，请刷新重试");
          }
        }
      } catch (error) {
        console.error("轮询状态失败", error);
      }
    }, 2000);

    setTimeout(() => clearInterval(timer), 300000);
  };

  useEffect(() => {
    getQrCode();
  }, []);

  const mockLogin = async () => {
    setLoading(true);
    try {
      const res = await axios.post("/api/auth/wx-login", {
        code: "mock_code_" + Date.now(),
        nickname: "测试用户",
        avatar: "",
      });
      if (res.data.code === 0) {
        localStorage.setItem("token", res.data.data.token);
        localStorage.setItem("user", JSON.stringify(res.data.data.user));
        message.success("登录成功");
        navigate("/");
      }
    } catch (error) {
      message.error("登录失败：" + (error.response?.data?.message || "未知错误"));
    } finally {
      setLoading(false);
    }
  };

  if (bootSequence) {
    return (
      <div className="boot-screen">
        <div className="boot-content">
          <div className="terminal-header">
            <RocketOutlined className="terminal-icon" />
            <span>ENDFIELD TRADING TERMINAL</span>
          </div>
          <div className="boot-lines">
            <div className="boot-line">INITIALIZING SYSTEM...</div>
            <div className="boot-line">LOADING MODULES...</div>
            <div className="boot-line">CONNECTING TO EXCHANGE...</div>
            <div className="boot-line active">AWAITING AUTHENTICATION</div>
          </div>
          <div className="boot-loader">
            <div className="loader-bar"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="login-container">
      {/* 背景装饰 */}
      <div className="login-bg-grid"></div>
      <div className="login-bg-gradient"></div>

      {/* 装饰性边框 */}
      <div className="corner-decoration top-left"></div>
      <div className="corner-decoration top-right"></div>
      <div className="corner-decoration bottom-left"></div>
      <div className="corner-decoration bottom-right"></div>

      {/* 主标题 */}
      <div className="login-header">
        <div className="header-logo">
          <ThunderboltOutlined className="logo-icon" />
        </div>
        <h1 className="header-title">
          <span className="title-cn">终末地调度券交易系统</span>
          <span className="title-en">ENDFIELD TRADING SYSTEM</span>
        </h1>
        <div className="header-subtitle">
          <span className="subtitle-tag">TACTICAL TRADING TERMINAL</span>
          <span className="subtitle-version">v2.0.26</span>
        </div>
      </div>

      {/* 登录卡片 */}
      <div className="login-card-wrapper">
        <Card className="login-card" title={
          <div className="card-title">
            <WechatOutlined />
            <span>身份验证 / AUTHENTICATION</span>
          </div>
        }>
          <Tabs
            className="login-tabs"
            items={[
              {
                key: "qrcode",
                label: "扫码登录",
                children: (
                  <div className="tab-content qrcode-tab">
                    <div className="qrcode-wrapper">
                      {scene ? (
                        <>
                          <div className="qrcode-frame">
                            <QRCode value={scene} size={180} bgColor="#ffffff" fgColor="#000000" />
                            <div className="qrcode-overlay"></div>
                          </div>
                          <div className="scan-line"></div>
                        </>
                      ) : (
                        <div className="qrcode-loading">
                          <Spin size="large" />
                          <div className="loading-text">GENERATING QR CODE...</div>
                        </div>
                      )}
                    </div>
                    <p className="qrcode-instruction">
                      <span className="instruction-icon">⟡</span>
                      使用微信扫描二维码进行身份验证
                    </p>
                    <div className="qrcode-status">
                      <div className="status-indicator"></div>
                      <span>等待扫描...</span>
                    </div>
                  </div>
                ),
              },
              {
                key: "mock",
                label: "快捷登录",
                children: (
                  <div className="tab-content mock-tab">
                    <div className="mock-content">
                      <UserOutlined className="mock-icon" />
                      <p className="mock-text">测试环境快速接入</p>
                      <p className="mock-subtext">TEST ENVIRONMENT QUICK ACCESS</p>
                      <button
                        className="mock-login-btn"
                        onClick={mockLogin}
                        disabled={loading}
                      >
                        {loading ? (
                          <>
                            <Spin size="small" />
                            <span>验证中...</span>
                          </>
                        ) : (
                          <>
                            <ThunderboltOutlined />
                            <span>快速登录</span>
                          </>
                        )}
                      </button>
                    </div>
                  </div>
                ),
              },
            ]}
          />
        </Card>

        {/* 卡片底部装饰 */}
        <div className="card-footer-decoration">
          <div className="decoration-line"></div>
          <div className="decoration-dots">
            <span className="dot"></span>
            <span className="dot"></span>
            <span className="dot"></span>
          </div>
        </div>
      </div>

      {/* 底部信息 */}
      <div className="login-footer">
        <div className="footer-info">
          <span className="info-item">SYSTEM STATUS: ONLINE</span>
          <span className="info-separator">|</span>
          <span className="info-item">LATENCY: 12ms</span>
          <span className="info-separator">|</span>
          <span className="info-item">SECURE CONNECTION</span>
        </div>
      </div>
    </div>
  );
};

export default Login;
