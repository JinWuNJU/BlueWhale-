import {axios} from "../utils/request.ts";
import {API_MODULE, COUPON_MODULE, ORDER_MODULE, STORE_MODULE, USER_MODULE} from "./_prefix.ts";

type UserId = {
    userId: number,
};
type StoreId = {
    storeId: number,
};
type ProductId = {
    productId: number,
};
type OrderId = {
    orderId: number,
};
type CouponID = {
    couponId: number,
};

export const getUserById = (userId: UserId) => {
    console.log("Getting information of user: " + userId.userId)
    return axios.get(`${USER_MODULE}/getuser`, {params: userId})
        .then((res: any) => {
            return res
        })
}

export const getStoreById = (storeId: StoreId) => {
    console.log("Getting information of store: " + storeId.storeId)
    return axios.get(`${API_MODULE}/getshop`, {params: storeId})
        .then((res: any) => {
            return res
        })
}

export const getProductById = (productId: ProductId) => {
    console.log("Getting information of product: " + productId.productId)
    return axios.get(`${STORE_MODULE}/getproduct`, {params: productId})
        .then((res: any) => {
            return res
        })
}

export const getOrderById = (orderId: OrderId) => {
    console.log("Getting information of order: " + orderId.orderId)
    return axios.get(`${ORDER_MODULE}/getorder`, {params: orderId})
        .then((res: any) => {
            return res
        })
}

export const getCouponById = (couponId: CouponID) => {
    console.log("Getting information of coupon: " + couponId.couponId)
    return axios.get(`${COUPON_MODULE}/getcoupon`, {params: couponId})
        .then((res: any) => {
            return res
        })
}

export const getStaffsIdByShoreId = (storeId: StoreId) => {
    console.log("Getting staff ids of store: " + storeId.storeId)
    return axios.get(`${USER_MODULE}/getstaff`, {params: storeId})
        .then((res: any) => {
            return res
        })
}