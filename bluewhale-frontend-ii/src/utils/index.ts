//将身份转化为中文显示
export function parseRole(role: string | null) {
    if (role === 'MANAGER') {
        return "管理员"
    } else if (role === 'CUSTOMER') {
        return "顾客"
    } else if (role === 'STAFF') {
        return "商家"
    } else if (role === 'CEO') {
        return "CEO"
    }
}

export function parseProductCategory(category: string | null) {
    if (category === 'FOOD') {
        return "食品"
    } else if (category === 'CLOTHES') {
        return "服饰"
    } else if (category === 'FURNITURE') {
        return "家具"
    } else if (category === 'ELECTRONICS') {
        return "电子产品"
    } else if (category === 'ENTERTAINMENT') {
        return "娱乐"
    } else if (category === 'SPORTS') {
        return "体育产品"
    } else if (category === 'LUXURY') {
        return "奢侈品"
    }
}

export function parseProductSubCategory(category: string | null) {
    if (category === 'FRUIT') {
        return "水果"
    } else if (category === 'VEGETABLE') {
        return "蔬菜"
    } else if (category === 'MEAT') {
        return "肉类"
    } else if (category === 'SEAFOOD') {
        return "海鲜"
    } else if (category === 'SNACKS') {
        return "零食"
    } else if (category === 'DRINK') {
        return "饮品"
    } else if (category === 'MALE') {
        return "男装"
    } else if (category === 'FEMALE') {
        return "女装"
    } else if (category === 'CHILD') {
        return "童装"
    } else if (category === 'MOBILES') {
        return "手机"
    } else if (category === 'PCS') {
        return "电脑"
    } else if (category === 'CAMERA') {
        return "相机"
    } else if (category === 'AUDIO') {
        return "音响"
    } else if (category === 'OTHERS') {
        return "其它"
    } else if (category === 'OMITTED') {
        return "..."
    }
}

export function parseDeliveryMode(mode: string | null) {
    if (mode === 'DELIVERY') {
        return "快递送达"
    } else if (mode === 'PICKUP') {
        return "到店自提"
    }
}

export function parseOrderStatus(status: string | null) {
    if (status === 'UNPAID') {
        return "待支付"
    } else if (status === 'UNSEND') {
        return "待发货"
    } else if (status === 'UNGET') {
        return "待收货"
    } else if (status === 'UNCOMMENT') {
        return "待评价"
    } else if (status === 'DONE') {
        return "已完成"
    } else if (status === 'REFUND'){
        return "已退款"
    }
}

export function parseOrderStep(status: string | null) {
    if (status === 'UNPAID') {
        return 0
    } else if (status === 'UNSEND') {
        return 1
    } else if (status === 'UNGET') {
        return 2
    } else if (status === 'UNCOMMENT') {
        return 3
    } else if (status === 'DONE'){
        return 4;
    } else if (status === 'REFUND'){
        return 5;
    }
}

export function parseCouponType(status: string | null) {
    if (status === 'FULL_REDUCTION') {
        return "满减券"
    } else if (status === 'SPECIAL') {
        return "蓝鲸券"
    }
}

//将时间转化为日常方式
export function parseTime(time: string) {
    let times = time.split(/T|\./)
    return times[0] + " " + times[1]
}



