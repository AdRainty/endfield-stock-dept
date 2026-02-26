import React, { useState, useEffect } from "react";
import { Card, QRCode, message, Tabs, Spin } from "antd";
import { WechatOutlined, UserOutlined } from "@ant-design/icons";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Login = () => {
  const navigate = useNavigate();
  const [qrCodeValue, setQrCodeValue] = useState("");
  const [scene, setScene] = useState("");
  const [loading, setLoading] = useState(false);

  // 获取二维码
  const getQrCode = async () => {
    try {
      const res = await axios.post("/api/auth/wx-qrcode");
      if (res.data.code === 0) {
        setScene(res.data.data.scene);
        setQrCodeValue(res.data.data.qrCodeBase64);
        // 开始轮询检查状态
        pollQrStatus(res.data.data.scene);
      }
    } catch (error) {
      console.error("获取二维码失败", error);
    }
  };

  // 轮询二维码状态
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

    // 5 分钟后停止轮询
    setTimeout(() => clearInterval(timer), 300000);
  };

  useEffect(() => {
    getQrCode();
  }, []);

  // 模拟登录（测试用）
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

  return (
    <div style={{
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      minHeight: "100vh",
      background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
    }}>
      <Card style={{ width: 400 }} title={<span><WechatOutlined /> 微信登录</span>}>
        <Tabs
          items={[
            {
              key: "qrcode",
              label: "扫码登录",
              children: (
                <div style={{ textAlign: "center", padding: "20px 0" }}>
                  {scene ? (
                    <QRCode value={scene} size={180} />
                  ) : (
                    <Spin />
                  )}
                  <p style={{ marginTop: 16, color: "#666" }}>
                    使用微信扫描二维码登录
                  </p>
                </div>
              ),
            },
            {
              key: "mock",
              label: "快捷登录",
              children: (
                <div style={{ textAlign: "center", padding: "40px 0" }}>
                  <p style={{ marginBottom: 16, color: "#666" }}>
                    测试环境快捷登录
                  </p>
                  <button
                    onClick={mockLogin}
                    disabled={loading}
                    style={{
                      padding: "8px 24px",
                      fontSize: 16,
                      background: "#1890ff",
                      color: "white",
                      border: "none",
                      borderRadius: 4,
                      cursor: loading ? "not-allowed" : "pointer",
                    }}
                  >
                    {loading ? "登录中..." : "一键登录"}
                  </button>
                </div>
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
};

export default Login;
