import {createRouter, createWebHashHistory} from "vue-router"

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            redirect: '/entrance',
        },
        {
            path: '/entrance',
            component: () => import('../views/Entrance.vue'),
            meta: {title: 'BlueWhale'}
        },
        {
            path: '/login',
            component: () => import('../views/user/Login.vue'),
            meta: {title: 'BlueWhale - 登录'}
        },
        {
            path: '/register',
            component: () => import('../views/user/Register.vue'),
            meta: {title: 'BlueWhale - 注册'}
        },
        {
            path: '/createStore',
            name: 'createStore',
            component: () => import('../views/store/CreateStore.vue'),
            meta: {
                title: 'BlueWhale - 创建商店',
                permission: ['MANAGER']
            }
        },
        {
            path: '/dashboard',
            name: 'Dashboard',
            component: () => import('../views/user/Dashboard.vue'),
            meta: {title: 'BlueWhale - 个人信息'}
        },
        {
            path: '/allStore',
            name: 'allStore',
            component: () => import('../views/store/AllStore.vue'),
            meta: {title: 'BlueWhale - 商店列表'},
        },
        {
            path: '/allOrder',
            name: 'allOrder',
            component: () => import('../views/order/AllOrder.vue'),
            meta: {title: 'BlueWhale - 订单信息'},
        },
        {
            path: '/coupon',
            name: 'coupon',
            component: () => import('../views/coupon/Coupon.vue'),
            meta: {title: 'BlueWhale - 优惠券'},
        },
        {
            path: '/favorite',
            name: 'favorite',
            component: () => import('../views/favorite/Favorite.vue'),
            meta: {title: 'BlueWhale - 我的收藏'},
        },
        {
            path: '/msg',
            name: 'msg',
            component: () => import('../views/message/Message.vue'),
            meta: {title: 'BlueWhale - 我的消息'},
        },
        {
            path: '/home',
            name: 'home',
            component: () => import('../views/Home.vue'),
            meta: {title: 'BlueWhale - 主页'}
        },
        {
            path: '/storeDetail/:storeId',
            name: 'storeDetail',
            component: () => import('../views/store/StoreDetail.vue'),
            meta: {title: 'BlueWhale - 商店详情'},
            props: true
        },
        {
            path: '/about',
            name: 'about',
            component: () => import('../views/About.vue'),
            meta: {title: 'BlueWhale - 关于我们'}
        },
        {
            path: '/404',
            name: '404',
            component: () => import('../views/NotFound.vue'),
            meta: {title: 'BlueWhale - 404'}
        },
        {
            path: '/:catchAll(.*)',
            redirect: '/404'
        }
    ]
})

router.beforeEach((to, _, next) => {
    const token: string | null = sessionStorage.getItem('token');
    const role: string | null = sessionStorage.getItem('role');

    if (to.meta.title) {
        document.title = to.meta.title
    }

    if ( token ) {
        if ( to.meta.permission ) {
            if ( to.meta.permission.includes(role!) ) {
                next()
            } else {
                next('/404')
            }
        } else {
            next()
        }
    } else {
        if ( to.path === '/login' ) {
            next();
        } else if ( to.path === '/register' ) {
            next()
        } else if ( to.path === '/entrance' ) {
            next()
        } else if ( to.path === '/about' ) {
            next()
        } else {
            next('/entrance')
        }
    }
})

export {router}
