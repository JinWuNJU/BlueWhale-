import {axios} from "../utils/request.ts";
import {ALI_MODULE, ORDER_MODULE} from "./_prefix.ts";

type OrderInfo = {
    page: number,
    pageSize: number,
    searchText?: string,
    sortBy?: string,
    state?:string
};

type OrderCreateInfo = {
    type: string,
    shopId: number,
    pid: number,
    uid: number,
    otherInfo: string,
    deliveryType: string,
    num: number,
};

type OrderPayInfo = {
    order_id: number,
    coupon_id?: string,
    product_name: string,
    price: number,
};

type OrderCountInfo = {
    uid: number,
    shopId: number
}

type OrderRefundInfo = {
    order_id: number
}

export const orderInfo = (orderInfo: OrderInfo) => {
    console.log(orderInfo)
    return axios.get(`${ORDER_MODULE}`, {params: orderInfo})
        .then((res: any) => {
            return res
        })
}

export const orderCountInfo = (orderCountInfo: OrderCountInfo) => {
    console.log(orderInfo)
    return axios.get(`${ORDER_MODULE}/getCount`, {params: orderCountInfo})
        .then((res: any) => {
            return res
        })
}


export const exportExcel = (shopId: number): Promise<any> => {
    console.log("Fetching order data for shop:", shopId);
    return axios.post(`${ORDER_MODULE}/excel`, null, {
        params: { shopId }, // 将 shopId 作为查询参数发送
        responseType: "blob"
    }).then((response: any) => {
        console.log("Excel data received:", response);
        return response;
    }).catch((error: any) => {
        console.error("Error exporting Excel:", error);
        throw error;
    });
};

export const orderCreate = (orderCreateInfo: OrderCreateInfo) => {
    console.log(orderCreateInfo)
    return axios.post(`${ORDER_MODULE}`, orderCreateInfo, {})
        .then((res: any) => {
            return res
        })
}

export const orderDelivery = (orderId: number) => {
    console.log(orderId)
    return axios.post(`${ORDER_MODULE}/${orderId}/deliver`, null)
        .then((res: any) => {
            return res
        })
}

export const orderReceive = (orderId: number) => {
    console.log(orderId)
    return axios.post(`${ORDER_MODULE}/${orderId}/getDeliver`, null)
        .then((res: any) => {
            return res
        })
}

export const orderPay = (orderPayInfo: OrderPayInfo):Promise<any> => {
    const currentUrl = window.location.href;
    console.log(orderPayInfo)

    return axios.post(`${ALI_MODULE}/pay`, null,{params: {...orderPayInfo,previousUrl:currentUrl},responseType:undefined})
        .then((response: any) => {
            console.log(response.data)
            return response.data
            //window.location.href = res.data;
        }).catch((error: any) => {
            console.error("Error displaying Alipay page:", error);
            throw error;
        });
}

export const orderRefund = (orderRefundInfo: OrderRefundInfo) => {
    console.log(orderRefundInfo)
    return axios.get(`${ALI_MODULE}/refund`, {params:orderRefundInfo})
        .then((res: any) => {
            return res
        })
}