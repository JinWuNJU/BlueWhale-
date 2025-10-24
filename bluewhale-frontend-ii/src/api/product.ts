// Lab2新增
// 开发时请解除3-4行的注释
import {axios} from '../utils/request'
import {STORE_MODULE} from './_prefix'

type ProductInfo = {
    shopId: number,
    page: number,
    pageSize: number,
    searchText?: string,
    sortBy?: string,
    type?:string,
    subtype?:string,
    priceMin?:number,
    priceMax?:number,
}

type ProductCreateInfo = {
    shopId: number,
    reserve: number,
    name: string,
    type: string,
    subtype: string,
    description: string,
    picUrl: any[],
    price: number,
}

type ProductModifyStockInfo = {
    shopId: number,
    pId: number,
    reserve_num: number,
}

type ProductCountInfo = {
    shopId: number
}

export const productInfo = (productInfo: ProductInfo) => {
    return axios.get(`${STORE_MODULE}/${productInfo.shopId}`, {params: productInfo})
        .then((res: any) => {
            return res
        })
}

export const productCountInfo = (productCountInfo: ProductCountInfo) => {
    return axios.get(`${STORE_MODULE}/getProductCount`, {params: productCountInfo})
        .then((res: any) => {
            return res
        })
}

export const productCreate = (productCreateInfo: ProductCreateInfo) => {
    console.log(productCreateInfo)
    return axios.post(`${STORE_MODULE}/${productCreateInfo.shopId}`, productCreateInfo, {})
        .then((res: any) => {
            return res
        })
}

export const productModifyStock = (productModifyStockInfo: ProductModifyStockInfo) => {
    console.log(productModifyStockInfo)
    return axios.post(`${STORE_MODULE}/${productModifyStockInfo.shopId}/${productModifyStockInfo.pId}`, null, {params: productModifyStockInfo})
        .then((res: any) => {
            return res
        })
}