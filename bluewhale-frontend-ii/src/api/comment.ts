import {axios} from "../utils/request.ts";
import {STORE_MODULE} from "./_prefix.ts";

type CommentInfo = {
    shopId: number,
    pid: number,
    sortBy?: number,
};

type CommentPostInfo = {
    userId: number,
    shopId: number,
    pid: number,
    comment: string,
    score: number,
    oid:number
};

export const commentInfo = (commentInfo: CommentInfo) => {
    console.log(commentInfo)
    return axios.get(`${STORE_MODULE}/${commentInfo.shopId}/${commentInfo.pid}`)
        .then((res: any) => {
            return res
        })
}

export const commentPost = (commentPostInfo: CommentPostInfo) => {
    console.log(commentPostInfo)
    return axios.post(`${STORE_MODULE}/${commentPostInfo.shopId}/${commentPostInfo.pid}/comment`, commentPostInfo, {})
        .then((res: any) => {
            return res
        })
}