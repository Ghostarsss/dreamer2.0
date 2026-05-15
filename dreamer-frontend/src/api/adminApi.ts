import request from "@/utils/request.ts";

export const listUsers = (page: any) => {
    return request({
        url: `/admin/users`,
        method: "GET",
        params: page
    })
};

export const noticeUser = (userId: any, data: any) => {
    return request({
        url: `/admin/users/${userId}/messages`,
        method: "POST",
        data
    })
};

export const updateUserRole = (userId: any, data: any) => {
    return request({
        url: `/admin/users/${userId}/roles`,
        method: "PUT",
        data
    })
};

export const updateUserPassword = (userId: any, data: any) => {
    return request({
        url: `/admin/users/password-edit/${userId}`,
        method: "PUT",
        data
    })
};

export const banUser = (userId: any) => {
    return request({
        url: `/admin/users/${userId}/ban`,
        method: "PUT",
    })
};

export const removeUser = (userId: any) => {
    return request({
        url: `/admin/users/${userId}`,
        method: "DELETE",
    })
};

export const listPosts = (page: any) => {
    return request({
        url: `/admin/posts`,
        method: "GET",
        params: page
    })
};

export const checkPost = (postId: any, data: any) => {
    return request({
        url: `/admin/posts/${postId}`,
        method: "PUT",
        data
    })
};

export const listFeedbacks = (page: any) => {
    return request({
        url: `/feedbacks/admin`,
        method: "GET",
        params: page
    })
};

export const handleFeedback = (feedbackId: any, data: any) => {
    return request({
        url: `/feedbacks/admin/${feedbackId}`,
        method: "POST",
        data
    })
};

export const listNotices = () => {
    return request({
        url: `/admin/notices`,
        method: "GET",
    })
};

export const publishNotice = (data: any) => {
    return request({
        url: `/admin/notices`,
        method: "POST",
        data
    })
};

export const removeNotice = (noticeId: any) => {
    return request({
        url: `/admin/notices/${noticeId}`,
        method: "DELETE",
    })
};

export const getStatistics = () => {
    return request({
        url: `/admin/statistics`,
        method: "GET",
    })
};

export const getView = () => {
    return request({
        url: `/system/view`,
        method: "GET",
    })
};