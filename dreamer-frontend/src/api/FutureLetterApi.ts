import request from "@/utils/request.ts";

export const pageOpenedLetter = (page: any,size: any) => {
    return request({
        url: "/letters/opened",
        method: "GET",
        params: {page, size}
    })
};

export const deleteLetter = (letterId: any) => {
    return request({
        url: `/letters/${letterId}`,
        method: "DELETE",
    })
};

export const checkExistUnopenedLetter = () => {
    return request({
        url: `/letters/exist-unopened`,
        method: "GET",
    })
};

export const uploadImg = (formData: any) => {
    return request({
        url: `/letters/images`,
        method: "POST",
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export const postLetter = (data: any) => {
    return request({
        url: `/letters`,
        method: "POST",
        data,
    })
}

export const openLetters = () => {
    return request({
        url: `/letters/to-be-opened`,
        method: "GET",
    })
}