import axios from "axios";
import router from "@/router/router.ts";
import {ElMessage} from "element-plus";

const request = axios.create({
    baseURL: "/api",
    timeout: 10000,
    withCredentials: true
});

// 登录、注册接口排除，不做登录拦截
const excludePath = [
    "/login",
    "/register",
    "/post/new",
    "/post"
];

request.interceptors.request.use((config) => {
    const token = localStorage.getItem("satoken");
    // 当前请求接口地址
    const reqUrl = config.url;

    // 判断：不在排除名单 并且 没有token → 跳登录
    const isExclude = excludePath.some(path => reqUrl && reqUrl.includes(path));
    if (!isExclude && !token) {
        router.push("/login");
        ElMessage.warning("请先登录")
        // 中断请求
        return Promise.reject("未登录，已拦截");
    }

    // 有token就带上
    if (token) {
        config.headers.satoken = token;
    }

    return config;
});

// 保留默认响应拦截即可
request.interceptors.response.use(
    res => res,
    err => Promise.reject(err)
);

export default request;