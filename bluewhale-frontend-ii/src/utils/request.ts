// @ts-ignore
import axios from 'axios'

//创建一个axios的实例service
const service = axios.create()

//判断是否登录
function hasToken() {
    return !(sessionStorage.getItem('token') == '')
}

//当前实例的拦截器，对所有要发送给后端的请求进行处理，在其中加入token
service.interceptors.request.use(
    (config: { headers: { [x: string]: string | null } }) => {
        if(hasToken()) {
            config.headers['token'] = sessionStorage.getItem('token')
        }
        return config
    },
    (error: any) => {
        console.log(error);
        return Promise.reject();
    }
)

//当前实例的拦截器，对所有从后端收到的请求进行处理，检验http的状态码
service.interceptors.response.use(
    (response: { status: number }) => {
        if (response.status === 200) {
            return response;
        } else {
            return Promise.reject();
        }
    },
    (error: any) => {
        console.log(error);
        return Promise.reject();
    }
)

//设置为全局变量
export {
    service as axios
}
