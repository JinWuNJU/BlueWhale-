import {axios} from '../utils/request'
import {USER_MODULE} from './_prefix'

type LoginInfo = {
    phone: string,
    password: string
}

type RegisterInfo = {
    role: string,
    name: string,
    phone: string,
    password: string,
    address: string,
    shopId?: number,
    shopName?: string//商店名称
}

type UpdateInfo = {
    name?: string,
    password?: string,
    address?: string,
    avatarUrl?: string,
}

// 如果有“Vue: This may be converted to an async function”警告，可以不管
// 用户登录
export const userLogin = (loginInfo: LoginInfo) => {
    return axios.post(`${USER_MODULE}/login`, null, {params: loginInfo})
        .then((res: any) => {
            return res
        })
}

// 用户注册
export const userRegister = (registerInfo: RegisterInfo) => {
    return axios.post(`${USER_MODULE}/register`, registerInfo,
        {headers: {'Content-Type': 'application/json'}})
        .then((res: any) => {
            return res
        })
}

// 获取用户信息
export const userInfo = () => {
    return axios.get(`${USER_MODULE}`)
        .then((res: any) => {
            return res
        })
}

// 更新用户信息
export const userInfoUpdate = (updateInfo: UpdateInfo) => {
    console.log(updateInfo)
    return axios.post(`${USER_MODULE}`, updateInfo, {headers: {'Content-Type': 'application/json'}})
        .then((res: any) => {
            return res
        })
}
