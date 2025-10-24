<script lang="ts">

import {orderPay} from "../api/order.ts";
import {Calendar, Coin, DocumentCopy, Goods, Money} from "@element-plus/icons-vue";
import {calcFinalPrice, couponOwnedInfo} from "../api/coupon.ts";
import {getCouponById} from "../api/resolve_id.ts";

export default {
	components: {Coin, Calendar, Money, Goods, DocumentCopy},
	data() {
		return {
			finalPrice: 2 << 20,
			coupons: [],
			couponSelected: 0,
			couponSelectedId: [],
		}
	},
	methods: {
		fetchCoupons() {
			couponOwnedInfo({
				userId: sessionStorage.userId,
			}).then(res => {
				this.coupons = res.data.result;
				for ( let i = 0; i < this.coupons.length; i++ ) {
					getCouponById({
						couponId: this.coupons[i].couponGroupId
					}).then(res => {
						this.coupons[i].couponInfo = res.data.result;
					})
				}
			});
		},
		handleConfirm() {
			orderPay({
				order_id: this.order_id,
				coupon_id: this.couponSelectedId.length != 0 ? this.couponSelectedId.join(",") : "-1",
		        price: this.finalPrice == 0 ? this.price * this.count : this.finalPrice,
		        product_name: this.name
			}).then(res => {
		        // const newWindow = window.open('', '_blank');
		        // newWindow.document.write(res);
		        document.write(res)
		        console.log(res)
			})
		},
		handleClose() {
			location.reload();
		},
		handleChangeCoupon() {
			this.finalPrice = this.price * this.count;
			calcFinalPrice({
				couponIds: this.couponSelectedId.join(","),
				originalPrice: this.finalPrice,
			}).then(res => {
				console.log(res.data.result)
				this.finalPrice = res.data.result.toFixed(2);
			})

		},
		handleSelectCoupon(id) {
			this.couponSelected++;
			this.couponSelectedId.push(id);
			this.handleChangeCoupon()
		},
		handleCancelCoupon(id) {
			this.couponSelected--;
			this.couponSelectedId = this.couponSelectedId.filter(c => c != id);
			this.handleChangeCoupon()
		},
	},
	props: [
		"shop_id",
		"product_id",
		"order_id",
		"user_id",
		"name",
		"price",
		"count",
		"create_time",
		"delivery_type"
	],
	mounted() {
		this.fetchCoupons();
	}
}
</script>

<template>
	<el-dialog
		title="支付订单"
		width="500"
		destroy-on-close
    draggable
		center
	>
		<p style="margin-top: 0; margin-bottom: 10px; font-weight: bold; font-size: 20px">订单信息：</p>
		<div style="display: flex; justify-content: space-between; margin-bottom: 20px">
			<div>
				<span>商品名称：{{ name }}</span><br>
				<span>商品数量：{{ count }}</span><br>
				<span>商品单价：￥ {{ price }}</span><br>
				<span>商品总价：</span>
				<span style="font-weight: bold">￥ {{ (count * price).toFixed(2) }}</span><br>
			</div>
			<div>
				<span>订单编号：{{ product_id * 65536 + shop_id * 256 + 834289395 }}</span><br>
				<span>订单创建时间：{{ create_time }}</span><br>
				<span>提货方式：{{ delivery_type }}</span><br>
			</div>
			<p/>
		</div>

		<p style="margin-top: 20px; margin-bottom: 0; font-weight: bold; font-size: 20px">可使用优惠券：</p>
		<ul
			v-if="coupons.filter(c =>
				(c.couponInfo.type || price * count >= c.couponInfo.triggerCondition)
				&& (c.couponInfo.shopId == -1 || c.couponInfo.shopId == shop_id ) && !c.isUsed).length != 0
				"
			style="overflow: auto; height: 200px; margin: 0; padding: 0">
			<el-card
				v-for="coupon in coupons.filter(c =>
				(c.couponInfo.type || price * count >= c.couponInfo.triggerCondition)
				&& (c.couponInfo.shopId == -1 || c.couponInfo.shopId == shop_id ) && !c.isUsed)"
				style="height: 150px"
				class="coupon-card"
			>
				<div style="display: flex; justify-content: space-between;">
				<span>
					{{ coupon.couponInfo.name.length > 12 ? coupon.couponInfo.name.substring(0, 12) + "..." : coupon.couponInfo.name }}
				</span>
					<div>
						<el-button
							v-if="couponSelected < 3
								&& !couponSelectedId.includes(coupon.couponInfo.couponId)
								&& (coupon.couponInfo.type || price * count >= coupon.couponInfo.triggerCondition)
								&& (coupon.couponInfo.type || finalPrice >= coupon.couponInfo.triggerCondition)"
							@click.prevent="handleSelectCoupon(coupon.couponInfo.couponId)"
							type="success">使用
						</el-button>
						<el-button
							v-if="couponSelected < 3
								&& !couponSelectedId.includes(coupon.couponInfo.couponId)
								&& (coupon.couponInfo.type && price * count >= coupon.couponInfo.triggerCondition)
								&& finalPrice < coupon.couponInfo.triggerCondition"
							disabled
							type="warning">使用
						</el-button>
						<el-button
							v-if="couponSelected >= 3 && !couponSelectedId.includes(coupon.couponInfo.couponId)"
							disabled
							type="success">使用
						</el-button>
						<el-button
							v-if="couponSelectedId.includes(coupon.couponInfo.couponId)"
							@click.prevent="handleCancelCoupon(coupon.couponInfo.couponId)"
							type="danger">取消
						</el-button>
					</div>
				</div>

				<el-divider style="margin: 5px" />
				<div style="display: flex; flex-direction: row; align-content: center">
					<img src="src/assets/img/coupon_green.png" alt="" style="width: 45px; height: 45px; margin: 10px">
					<div>
						<el-descriptions
							class="margin-top"
							:column="2"
							size="small"
							border
						>
							<el-descriptions-item>
								<template #label>
									<div class="cell-item">
										<el-icon><money/></el-icon>优惠类型
									</div>
								</template>
								<el-tag size="small" type="danger">{{ coupon.couponInfo.type ? "蓝鲸券" : "满减券" }}</el-tag>
							</el-descriptions-item>
							<el-descriptions-item>
								<template #label>
									<div class="cell-item">
										<el-icon><goods/></el-icon>优惠范围
									</div>
								</template>
								<el-tag size="small">{{ coupon.couponInfo.shopId == -1 ? "全局优惠券" : "门店优惠券" }}</el-tag>
								<!--					TODO: NEED TO GET SPECIFIC STORE NAME -->
							</el-descriptions-item>
							<el-descriptions-item>
								<template #label>
									<div class="cell-item">
										<el-icon><coin/></el-icon>优惠规则
									</div>
								</template>
								<span v-if="coupon.couponInfo.type">分段优惠</span>
								<span v-else>满 {{ coupon.couponInfo.triggerCondition }} 减 {{ coupon.couponInfo.triggerReduction }}</span>
							</el-descriptions-item>
						</el-descriptions>
					</div>
				</div>
			</el-card>
		</ul>
		<el-empty v-else description="暂无可使用的优惠券！"/>

		<p style="margin-top: 20px; margin-bottom: 10px; font-weight: bold; font-size: 20px">支付订单：</p>
		<span>支付方式：支付宝沙箱</span><br>

		<div v-if="couponSelectedId.length == 0">
			<span>实际金额：</span>
			<span style="font-weight: bold;">￥ {{ (count * price).toFixed(2) }}</span><br>
		</div>

		<div v-else>
			<span>实际金额：</span>
			<span style="font-weight: bold; text-decoration: line-through">￥ {{ (count * price).toFixed(2) }}</span>
			<span style="font-weight: bold; color: #ff6d12">
				￥ {{ finalPrice }}</span><br>
		</div>

		<el-icon><coin/></el-icon>
		<span style="font-weight: bold;"> 同一订单最多可使用三张优惠券，且使用顺序可能影响最终价格。</span><br>
		<el-icon><coin/></el-icon>
		<span style="font-weight: bold;"> 订单支付后即无法撤销，请仔细核对！</span>

		<template #footer>
			<div class="dialog-footer">
				<el-button type="primary" @click="handleConfirm">确认</el-button>
				<el-button type="info" @click="handleClose">关闭</el-button>
			</div>
		</template>
	</el-dialog>
</template>