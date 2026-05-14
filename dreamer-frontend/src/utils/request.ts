import axios from "axios";
import router from "@/router/router.ts";
import { ElMessage } from "element-plus";

const request = axios.create({
    baseURL: "/api",
    timeout: 10000,
    withCredentials: true
});

// 白名单：不需要登录就能访问的接口
const excludePath = [
    "/login",
    "/register",
    "/posts/new",
    "/posts/hot",
    "/comments/**",
    "/letters/opened",
];

// 请求拦截器
request.interceptors.request.use((config) => {
    const token = localStorage.getItem("satoken");
    const reqUrl = config.url || ""; // 防止 undefined

    // 匹配白名单（支持 /** 通配符）
    const isExclude = excludePath.some(path => {
        // 处理通配符：/xxx/** → 匹配所有以该路径开头的地址
        if (path.endsWith("/**")) {
            const prefix = path.slice(0, -3);
            return reqUrl.startsWith(prefix);
        }
        // 普通路径：精确匹配
        return reqUrl === path;
    });

    // 不在白名单 + 没有token → 拦截
    if (!isExclude && !token) {
        ElMessage.warning("请先登录");
        router.push("/login");
        return Promise.reject("未登录，请求已中断");
    }

    // 携带 token
    if (token) {
        config.headers.satoken = token;
    }

    return config;
});

// 响应拦截器
request.interceptors.response.use(
    (res) => res,
    (err) => {
        console.error("请求异常：", err);
        return Promise.reject(err);
    }
);

export default request;