import request from "@/utils/request.ts";

export const listMessage = () => {
    return request.get("/messages");
};

export const clearMessage = () => {
    return request.delete("/messages");
};

export const countUnReadMessage = () => {
    return request.get("/messages/count");
};