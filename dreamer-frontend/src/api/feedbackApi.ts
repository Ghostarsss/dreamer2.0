import request from "@/utils/request.ts";

export const addFeedback = (data: any) => {
    return request({
        url: "/feedbacks",
        method: "post",
        data
    })
};