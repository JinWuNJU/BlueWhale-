// Lab2新增
// 开发时请解除3-4行的注释
import {axios} from '../utils/request'
import {CREATE_STORE_MODULE, STORE_MODULE} from './_prefix'

type StoreInfo = {
    page: number,
    pageSize: number,
    searchText?: string,
    sortBy?: string,
}

type StoreCreateInfo = {
    name: string,
    picUrl: string,
    description: string,
    otherInfo: string,
}

export const storeInfo = (storeInfo: StoreInfo) => {
    return axios.get(`${STORE_MODULE}`, {params: storeInfo})
        .then((res: any) => {
            return res
        })
}

export const storeCountInfo = () => {
    return axios.get(`${STORE_MODULE}/getCount`)
        .then((res: any) => {
            return res
        })
}

export const storeCreate = (storeCreateInfo: StoreCreateInfo) => {
    console.log(storeCreateInfo)
    return axios.post(`${CREATE_STORE_MODULE}`, storeCreateInfo, {})
        .then((res: any) => {
            return res
        })
}