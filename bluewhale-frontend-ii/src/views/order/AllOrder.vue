<script setup lang="ts">

import {onMounted, ref} from "vue";
import {fullScreenLoading} from "../../utils/visuals.ts";
import {exportExcel, orderCountInfo, orderInfo} from "../../api/order.ts";
import {parseDeliveryMode, parseOrderStep} from "../../utils";
import {router} from "../../router";
import Rate from "../../components/Rate.vue";
import ConfirmReceive from "../../components/ConfirmReceive.vue";
import PayOrder from "../../components/PayOrder.vue";
import Header from "../../components/Header.vue"
import ConfirmSend from "../../components/ConfirmSend.vue";
import {getProductById, getStaffIdByShoreId, getStaffsIdByShoreId} from "../../api/resolve_id.ts";
import ConfirmRefund from "../../components/ConfirmRefund.vue";
import {createSessionsInfo} from "../../api/chatbox.ts";

const orders = ref([]);

const role = sessionStorage.getItem('role');
const uid = sessionStorage.getItem('userId');
const shopId = sessionStorage.getItem('storeId');
const orderCount = ref(0);
const current_page = ref(1);
const page_loaded = ref(false);

const payOpen = ref(false);
const confirmOpen = ref(false);
const rateOpen = ref(false);
const sendOpen = ref(false);
const refundOpen = ref(false);
const state = ref("ALL")
const current_sid = ref(0);
const current_pid = ref(0);
const current_oid = ref(0);
const current_uid = ref(0);

const current_name = ref("");
const current_price = ref(0);
const current_count = ref(0);
const current_create_time = ref("");
const current_delivery_type = ref("");


const switch_page = () => {
	fetchOrders();
}

const fetchOrders = () => {
	page_loaded.value = false;
	orderInfo({
		page: current_page.value,
		pageSize: 8,
        state: state.value
	}).then(res => {
		orders.value = res.data.result;
		for ( let i = 0; i < orders.value.length; i++ ) {
			getProductById({
        productId: orders.value[i].pid
			}).then(res => {
				orders.value[i].productInfo = res.data.result;
			})
		}
		page_loaded.value = true;
	}).catch(() => {

	})
}

const redirectToProduct = (shop_id) => {
	router.push({
		path: `/storeDetail/${shop_id}`
	})
}

const generateForm = async (shopId:number) => {
  try {
    const response = await exportExcel(shopId);
    // 创建一个 Blob 对象，用来表示文件数据
    const blob = new Blob([response.data]);
    // 创建一个链接对象
    const url = window.URL.createObjectURL(blob);
    // 创建一个<a>标签用于下载
    const link = document.createElement('a');
    // 设置下载链接
    link.href = url;
    // 设置下载文件的名称
    link.download = 'order_data.xlsx';
    // 触发点击事件，执行下载
    link.click();
    // 释放资源
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('Error downloading Excel:', error);
  }
};

const handlePay = (sid, pid, oid, name, price, count, create_time, delivery_type) => {
	current_sid.value = sid;
	current_pid.value = pid;
	current_oid.value = oid;
	current_name.value = name;
	current_price.value = price;
	current_count.value = count;
	current_create_time.value = create_time.substring(0, 10) + " " + create_time.substring(11, 19);
	current_delivery_type.value = parseDeliveryMode(delivery_type);
	payOpen.value = true;
}

const handleReceive = (oid) => {
	current_oid.value = oid;
	confirmOpen.value = true;
}

const handleSend = (oid) => {
	current_oid.value = oid;
	sendOpen.value = true;
}

const handleComment = (sid, pid, uid,oid) => {
	current_pid.value = pid;
	current_uid.value = uid;
	current_sid.value = sid;
    current_oid.value = oid;
	rateOpen.value = true;
}

const handleRefund = (oid) => {
    current_oid.value = oid;
    refundOpen.value = true;
}

const handleContactStaff = (sid) => {
	getStaffsIdByShoreId({
		storeId: sid
	}).then(res => {
		createSessionsInfo({
			// TODO - Need a way of getting both sides' IDs
			staffId: res.data.result,
			customerId: parseInt(uid),
		}).then(() => {
			ElMessage.success("会话已建立，请在我的消息中查看！");
		}).catch(() => {
			ElMessage.error("会话建立失败！");
		})
	})
}

onMounted(async () => {
	fullScreenLoading()
	fetchOrders();
  orderCountInfo({
    uid:(role as String).match(/^MANAGER$|^CEO$/) ? -1 : parseInt(uid as string),
    shopId:shopId == 'null' ? 0 : parseInt(shopId as string)
  }).then(res => {
    orderCount.value = res.data.result;
    console.log(res)
    console.log(shopId == 'null' ? 0 : shopId)
  })
})

</script>

<template>
	<body style="">
		<Header style="width: 100%;"/>
<!--		<Header style="position: fixed; width: 100%; top: 0"/>-->
    <el-main class="main" style="margin: 0">
      <div style="width: 1000px; height: 220px">
        <h1 style="font-size: 88px; margin: 0; padding: 20px 0 0 0" v-if="role=='CUSTOMER'">我的订单</h1>
        <h1 style="font-size: 88px; margin: 0; padding: 20px 0 0 0" v-if="role!='CUSTOMER'">全部订单</h1>
        <router-link to="/home" v-slot="{navigate}">
          <el-button
              @click="navigate"
              style="margin: 0 0 50px 0; font-size: 24px; width: 135px; height: 50px">
            返回主页
          </el-button>
        </router-link>
            <el-button
				@click="generateForm(shopId == 'null' ? 0 : parseInt(shopId as string))"
				type="primary"
				v-if="role=='CEO' || role == 'STAFF'"
				style="margin: 0 0 50px 10px; font-size: 24px; width: 135px; height: 50px">
				生成报表
			</el-button>
      </div>

        <el-radio-group
	        size="large"
	        v-model="state"
	        @change="fetchOrders"
	        style="display: flex;justify-content: center;margin-bottom: 10px">>
	        <el-radio-button label="全部" value="ALL" />
	        <el-radio-button label="待支付" value="UNPAID" />
	        <el-radio-button label="待发货" value="UNSEND" />
	        <el-radio-button label="待收货" value="UNGET" />
	        <el-radio-button label="退款/售后" value="REFUND" />
	        <el-radio-button label="待评价" value="UNCOMMENT" />
        </el-radio-group>

			<el-scrollbar style="height: 480px; margin: 0;">
				<el-empty v-if="!orders.length" description="当前没有订单" />
				<el-timeline v-for="i in Math.min(8, orders.length)" style="width: 1000px">
					<el-timeline-item
						:timestamp="orders[i - 1].createTime.substring(0, 10) + ' ' + orders[i - 1].createTime.substring(11, 19)"
						placement="top"
					>
						<el-card class="card">
							<div class="card-item">
								<img
									style="height: 180px; width: 180px; min-width: 180px; min-height: 180px;display: block; margin: 0 20px 0 0"
									v-if="orders[i - 1].productInfo !== undefined"
									:src="orders[i - 1].productInfo.picUrl[0]"
									alt=""
								>
								<div>
									<el-button
										type="text"
										@click="redirectToProduct(orders[i - 1].shopId)"
										v-if="orders[i - 1].productInfo !== undefined"
										style="font-size: 24px; margin-top: 10px">
										{{ orders[i - 1].productInfo.name }}
									</el-button>
									<div class="card-item">
										<div style="margin-right: 250px">
											<p>提货方式：{{parseDeliveryMode(orders[i - 1].deliveryType)}}</p>
											<p>订单编号：{{orders[i - 1].pid * 65536 + orders[i - 1].shopId * 256 + 834289395}}</p>
										</div>
										<div v-if="role == 'CUSTOMER'">
											<p></p><span>当前可进行操作：</span>
											<el-button
												type="text"
												@click="handleContactStaff(orders[i - 1].shopId)"
												style="font-size: 20px">
												联系客服
											</el-button>
											<el-button
												type="text"
												@click="handlePay(
													orders[i - 1].shopId,
													orders[i - 1].pid,
													orders[i - 1].oid,
													orders[i - 1].productInfo.name,
													orders[i - 1].productInfo.price,
													orders[i - 1].num,
													orders[i - 1].createTime,
													orders[i - 1].deliveryType,
												)"
												v-if="parseOrderStep(orders[i - 1].type) == 0"
												style="font-size: 20px">
												支付
											</el-button>
					                        <el-button
					                            type="text"
					                            @click="handleReceive(orders[i - 1].oid)"
					                            v-if="parseOrderStep(orders[i - 1].type) == 2"
					                            style="font-size: 20px">
					                             收货/提货
					                        </el-button>
											<el-button
												type="text"
												@click="handleComment(orders[i - 1].shopId, orders[i - 1].pid, orders[i - 1].uid, orders[i - 1].oid)"
												v-if="parseOrderStep(orders[i - 1].type) == 3"
												style="font-size: 20px">
												评价
											</el-button>
					                        <el-button
					                            type="text"
					                            @click="handleRefund(orders[i - 1].oid)"
					                            v-if="[1, 2, 3].includes(parseOrderStep(orders[i - 1].type) as number)"
					                            style="font-size: 20px">
					                            退款
					                        </el-button>
										</div>
										<div v-if="role == 'STAFF'">
											<p></p><span
											v-if="parseOrderStep(orders[i - 1].type) == 1"
										>当前可进行操作：</span>
											<el-button
												type="text"
												@click="handleSend(orders[i - 1].oid)"
												v-if="parseOrderStep(orders[i - 1].type) == 1"
												style="font-size: 20px">
												发货
											</el-button>
										</div>
									</div>

									<el-steps
										style="width: 800px"
										:space="200"
										:active="parseOrderStep(orders[i - 1].type)"
                                        v-if="orders[i - 1].type !== 'REFUND'"
										finish-status="success"
									>
										<el-step title="支付" description="顾客付款" />
										<el-step title="发货" description="商家发货" />
										<el-step title="收货" description="顾客签收快递或上门提货" />
										<el-step title="评价" description="顾客发表评价/评论" />
									</el-steps>
				                    <div v-else>
				                        订单状态：已退款
				                    </div>
								</div>
							</div>
						</el-card>
					</el-timeline-item>
				</el-timeline>
			</el-scrollbar>


			<div style="display: flex; flex-direction: column; align-items: center">
				<el-pagination
					background
					layout="prev, pager, next"
					style="margin: 20px"
					v-model:current-page="current_page"
					@current-change="switch_page"
					:default-page-size=8
					:total="orderCount"
				/>
			</div>
	    </el-main>
    </body>


	<Rate
		v-model="rateOpen"
		:product_id="current_pid"
		:shop_id="current_sid"
		:user_id="current_uid"
    :order_id="current_oid"
	/>
	<PayOrder
		v-model="payOpen"
		:shop_id="current_sid"
		:product_id="current_pid"
		:order_id="current_oid"
		:name="current_name"
		:price="current_price"
		:count="current_count"
		:create_time="current_create_time"
		:delivery_type="current_delivery_type"
	/>
	<ConfirmSend
		v-model="sendOpen"
		:order_id="current_oid"
	/>
	<ConfirmReceive
		v-model="confirmOpen"
		:order_id="current_oid"
	/>
    <ConfirmRefund
	    v-model="refundOpen"
	    :order_id="current_oid"
    />


</template>

<style scoped>
.main {
	font-family: 'Nunito', sans-serif;
	font-weight: 400;
	color: #333;
	background-image: url("../../assets/img/background/delivery2.jpg");
	background-size: cover;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.card-item {
	display: flex;
	flex-direction: row;
}


</style>