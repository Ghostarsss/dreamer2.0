import request from "@/utils/request.ts";

export const sendVerifyCode = (data: any) => {
    return request.post("/register/send-verify-code", data);
};

export const avatarUpload = (data: any) => {
    return request.post("/register/avatars", data);
};

export const register = (data: any) => {
    return request.post("/register", data);
};

export const login = (data: any) => {
    return request.post("/login", data);
};

export const logoutApi = () => {
    return request.delete("/login/out");
};