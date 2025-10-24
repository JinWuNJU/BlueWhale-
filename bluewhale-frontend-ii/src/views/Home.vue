<!--Lab2新增-全部商店界面/主页-->
<script lang="ts">
import Header from "../components/Header.vue"
import {fullScreenLoadingShort} from "../utils/visuals.ts";
import {couponInfo, couponOwnedInfo} from "../api/coupon.ts";
import {orderInfo} from "../api/order.ts";

export default {
	data() {
		return {
			card_images: [
				"src/assets/img/cart.png",
				"src/assets/img/order.png",
				"src/assets/img/coupon.png",
			],
			card_titles: [
				"全部商店",
				sessionStorage.role == 'CUSTOMER' ? "我的订单" : "订单信息",
				sessionStorage.role == 'CUSTOMER' ? "我的优惠券" : "优惠券信息",
			],
			card_contents: [
				"",
				"",
				"",
			],
			target_pages: [
				"/allStore",
				"/allOrder",
				"/coupon",
			],
			undone_orders: 0,
			unclaimed_coupons: 0,
			already_owned_coupons: []
		}
	},
	components: {
		Header
	},
	mounted() {
		fullScreenLoadingShort();
		orderInfo({
			page: 1,
			pageSize: 2 << 16,
			state: 'ALL'
		}).then(res => {
			this.undone_orders = res.data.result.length
			orderInfo({
				page: 1,
				pageSize: 2 << 16,
				state: 'DONE'
			}).then(res => {
				this.undone_orders -= res.data.result.length
			})
		});
		couponInfo({
			page: 1,
			pageSize: 2 << 16,
			shopId: -1,
		}).then(res => {
			couponOwnedInfo({
				userId: sessionStorage.userId,
			}).then(res => {
				for ( let i = 0; i < res.data.result.length; i++ ) {
					this.already_owned_coupons.push(res.data.result[i].couponGroupId)
				}
			}).then(() => {
					this.unclaimed_coupons = res.data.result
						.filter(c => (c.count > 0 && !this.already_owned_coupons.includes(c.couponId))).length;
				}
			)
		});
	}
}
</script>

<template>
	<Header/>
	<el-main class="main">
		<h1 style="color: white; font-size: 66px; margin-left: 100px">欢迎来到蓝鲸商城...</h1>
		<el-carousel
			class="card"
			:interval="4000"
			type="card"
			height="400px"
			style="margin: 100px">
			<el-carousel-item 
				v-for="idx in card_titles.length"
				:key="idx">
				<router-link
					:to="this.target_pages[idx - 1]"
					v-slot="{navigate}"
					style="text-decoration: none">
					<div class="container" @click="navigate" style="margin-top: 100px">
						<el-badge :value="(idx==3)?unclaimed_coupons:((idx==2)?undone_orders:'')">
							<img
								class="icon"
								alt=""
								:src="this.card_images[idx - 1]"
								width="100"
								height="100"
							>
						</el-badge>
						<h3>{{ this.card_titles[idx - 1] }}</h3>
						<span>{{ this.card_contents[idx - 1] }}</span>
					</div>
				</router-link>
			</el-carousel-item>
		</el-carousel>
	</el-main>
</template>


<style scoped>
.main {
	background-image: url("../assets/img/background/mall2.jpg");
	background-size: cover;
}

.container {
	display: flex;
	justify-content: center;
	align-content: center;
	align-items: center;
	flex-direction: column;
}

.el-carousel__item h3 {
	color: #475669;
	opacity: 0.75;
	margin: 20px;
	text-align: center;
	font-size: 24px;
}

.el-carousel__item {
	border: 8px solid rgb(255, 255, 255);
}
.el-carousel__item:nth-child(3n) {
	background-color: #cffff1;
}
.el-carousel__item:nth-child(3n + 1) {
	background-color: #e9ffcf;
}
.el-carousel__item:nth-child(3n + 2) {
	background-color: #e6cfff;
}
</style>
