import request from "@/utils/request.ts";

export const me = () => {
    return request.get("/users/me");
}

export const avatarUpload = (data: any) => {
    return request.post("/users/me/avatars", data);
};

export const sendVerifyCode = () => {
    return request.get("/users/me/send-verify-code");
};

export const updateUser = (data: any) => {
    return request.put("/users/me", data);
};

export const sign = () => {
    return request.post("/vp/sign");
};

export const checkSign = () => {
    return request.get("/vp/sign/status");
};

export const queryUserInfo = (userId: any) => {
    return request.get(`/users/${userId}`);
};

export const followUser = (userId: any) => {
    return request.post(`/follows/me/following/${userId}`);
};

export const unfollowUser = (userId: any) => {
    return request.delete(`/follows/me/following/${userId}`);
};

export const checkFollowStatus = (userId: any) => {
    return request.get(`/follows/is-followed/${userId}`);
};

export const listMyFollowing = (cursor: any, offset: any) => {
    return request({
        url: "/follows/me/following",
        method: "GET",
        params: {cursor, offset}
    })
};

export const removeFansUser = (userId: any) => {
    return request.delete(`/follows/me/fans/${userId}`);
};

export const listMyFans = (cursor: any, offset: any) => {
    return request({
        url: "/follows/me/fans",
        method: "GET",
        params: {cursor, offset}
    })
};

export const buyEXP = (proton: any) => {
    return request({
        url: "/vp/buy-exp",
        method: "PUT",
        params: {proton}
    })
};

export const listUserFollowing = (cursor: any, offset: any,userId: any) => {
    return request({
        url: `/follows/${userId}/following`,
        method: "GET",
        params: {cursor, offset}
    })
};

export const listUserFans = (cursor: any, offset: any,userId: any) => {
    return request({
        url: `/follows/${userId}/fans`,
        method: "GET",
        params: {cursor, offset}
    })
};