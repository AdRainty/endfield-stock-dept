import axios from "axios";
import { message } from "antd";

// 创建 axios 实例
const request = axios.create({
  baseURL: "/api",
  timeout: 30000,
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 如果返回的状态码不是 0，说明接口有错误
    if (res.code !== 0) {
      message.error(res.message || "请求失败");
      // 401: 未登录
      if (res.code === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.href = "/login";
      }
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    return res;
  },
  (error) => {
    console.error("请求错误:", error);
    message.error(error.message || "网络错误");
    return Promise.reject(error);
  }
);

export default request;
