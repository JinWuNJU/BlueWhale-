import {axios} from "../utils/request.ts";
import {USER_MODULE} from "./_prefix.ts";

type AddFavInfo = {
    userId: number,
    sid?: number,
    pid?: number,
    uid?: number,
    type: number,
};

type UnfavInfo = {
    userId: number,
    sid?: number,
    pid?: number,
    uid?: number,
    type: number,
};

type GetFavInfo = {
    userId: number,
    type: number,
};

export const addFavInfo = (addFavInfo: AddFavInfo) => {
    console.log(addFavInfo)
    return axios.post(`${USER_MODULE}/addFav`, addFavInfo, {})
        .then((res: any) => {
            return res
        })
}

export const unfavInfo = (unfavInfo: UnfavInfo) => {
    console.log(unfavInfo)
    return axios.post(`${USER_MODULE}/unfav`, unfavInfo, {})
        .then((res: any) => {
            return res
        })
}

export const getFavInfo = (getFavInfo: GetFavInfo) => {
    console.log(getFavInfo)
    return axios.get(`${USER_MODULE}/getFav`, {params: getFavInfo})
        .then((res: any) => {
            return res
        })
}
