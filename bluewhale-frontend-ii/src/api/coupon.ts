import {axios} from "../utils/request.ts";
import {COUPON_MODULE} from "./_prefix.ts";

type CouponInfo = {
    page: number,
    pageSize: number,
    shopId?: number,                // shopId = -1 indicates coupons of all stores.
};

type CouponCreateInfo = {
    name: string,
    description: string,
    type: boolean,
    isGlobal: boolean,
    shopId?: number,
    count: number,
    deadline: string,
    triggerCondition?: number,
    triggerReduction?: number,
};

type CouponOwnedInfo = {
    userId: number,
};

type CouponReceiveInfo = {
    userId: number,
    couponId: number,
};

type CalcFinalPriceInfo = {
    couponIds: string,
    originalPrice: number,
};

export const couponInfo = (couponInfo: CouponInfo) => {
    console.log(couponInfo)
    return axios.get(`${COUPON_MODULE}/fetch`, {params: couponInfo})
        .then((res: any) => {
            return res
        })
}

export const couponOwnedInfo = (couponOwnedInfo: CouponOwnedInfo) => {
    console.log(couponOwnedInfo)
    return axios.get(`${COUPON_MODULE}/getbyid`, {
        params: couponOwnedInfo,
    })
        .then((res: any) => {
            return res
        })
}

export const couponCreateInfo = (couponCreateInfo: CouponCreateInfo) => {
    console.log(couponCreateInfo)
    return axios.post(`${COUPON_MODULE}/creat`, couponCreateInfo, {})
        .then((res: any) => {
            return res
        })
}

export const couponReceiveInfo = (couponReceiveInfo: CouponReceiveInfo) => {
    console.log(couponReceiveInfo)
    return axios.post(`${COUPON_MODULE}/receive`, null, {params: couponReceiveInfo})
        .then((res: any) => {
            return res
        })
}

export const calcFinalPrice = (calFinalPriceInfo: CalcFinalPriceInfo) => {
    return axios.post(`${COUPON_MODULE}/calc`, null, {params: calFinalPriceInfo})
        .then((res: any) => {
            return res
        })
}