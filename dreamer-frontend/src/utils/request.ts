import axios from "axios";

const request = axios.create({
    baseURL: "/api", //TODO 改成 plus
    timeout: 10000,
    withCredentials: true
});

// 请求：只干一件事（带 token）
request.interceptors.request.use((config) => {
    const token = localStorage.getItem("satoken");

    if (token) {
        config.headers.satoken = token;
    }

    return config;
});


export default request;