import request from "@/utils/request.ts";

export const listNewPosts = (params: any) => {
    return request.get("/posts/new", {params});
};

export const like = (postId: any) => {
    return request.put(`/likes/post/${postId}`);
};

export const addPost = (data: any) => {
    return request.post("/posts", data);
};

export const addComment = (data: any) => {
    return request.post("/comments", data);
};

export const listCommentsByPostId = (postId: any) => {
    return request.get(`/comments/${postId}`);
};

export const likeCommentByCommentId = (commentId: any) => {
    return request.put(`/likes/comment/${commentId}`);
};

export const removeCommentByCommentId = (commentId: any) => {
    return request.delete(`/comments/${commentId}`);
};

export const protonPostByPostId = (postId: any, protons: any) => {
    return request.post(`/posts/${postId}/protons`, {}, {params: protons});
};

export const listHotPosts = (params: any) => {
    return request.get("/posts/hot", {params});
};

export const listMinePosts = (params: any) => {
    return request.get("/posts/me", {params});
};

export const listFollowingPosts = (params: any) => {
    return request.get("/posts/following", {params});
};

export const deletePostByPostId = (postId: any) => {
    return request.delete(`/posts/${postId}`);
};

export const updatePostByPostId = (postId: any,data: any) => {
    return request.put(`/posts/${postId}`, data);
};

export const getPostByPostId = (postId: any) => {
    return request.get(`/posts/${postId}`);
};