import {axios} from "../utils/request.ts";
import {CHATBOX_MODULE} from "./_prefix.ts";

type FetchSessionsInfo = {
    customerId?: number
    staffId?: number
};

type createSessionInfo = {
    customerId: number
    staffId: number
};

type DeleteSessionsInfo = {
    customerId?: number
    staffId?: number
};

type fetchMessagesInfo = {
    chatBoxId: number,
};

type sendMessageInfo = {
    cid: number,
    msg: string,
    sender: boolean,
};

export const fetchSessionsInfo = (fetchSessionsInfo: FetchSessionsInfo) => {
    console.log(fetchSessionsInfo)
    return axios.get(`${CHATBOX_MODULE}/session/fetch`, {params: fetchSessionsInfo})
        .then((res: any) => {
            return res
        })
}

export const createSessionsInfo = (createSessionsInfo: createSessionInfo) => {
    console.log(createSessionsInfo)
    return axios.post(`${CHATBOX_MODULE}/session/create`, createSessionsInfo, {})
        .then((res: any) => {
            return res
        })
}

export const deleteSessionsInfo = (deleteSessionsInfo: DeleteSessionsInfo) => {
    console.log(deleteSessionsInfo)
    return axios.get(`${CHATBOX_MODULE}/session/delete`, {params: deleteSessionsInfo})
        .then((res: any) => {
            return res
        })
}

export const fetchMessagesInfo = (fetchMessagesInfo: fetchMessagesInfo) => {
    console.log(fetchMessagesInfo)
    return axios.get(`${CHATBOX_MODULE}/msg/fetch`, {params: fetchMessagesInfo})
        .then((res: any) => {
            return res
        })
}

export const sendMessageInfo = (sendMessageInfo: sendMessageInfo) => {
    console.log(sendMessageInfo)
    return axios.post(`${CHATBOX_MODULE}/msg/send`, sendMessageInfo, {})
        .then((res: any) => {
            return res
        })
}