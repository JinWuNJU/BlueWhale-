<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {Calendar, DocumentCopy, Goods, Money} from "@element-plus/icons-vue";
import {ComponentSize} from "element-plus";
import DeleteCoupon from "../../components/DeleteCoupon.vue";
import {couponInfo, couponOwnedInfo, couponReceiveInfo} from "../../api/coupon.ts";
import {getStoreById} from "../../api/resolve_id.ts";

const coupons = ref([]);
const alreadyOwned = ref([]);
const filterReceived = ref(false);
const delete_coupon_id = ref(0);
const delete_coupon_open = ref(false);

const role = ref(sessionStorage.role);
const userId = ref(sessionStorage.userId);
const storeId = ref(sessionStorage.storeId);

const fetchCoupons = (page, pageSize, shopId?, sortedBy?) => {
	couponInfo({
		page: page,
		pageSize: pageSize,
		shopId: shopId,
	}).then(res => {
		coupons.value = res.data.result;
		couponOwnedInfo({
			userId: userId.value,
		}).then(res => {
			for ( let i = 0; i < res.data.result.length; i++ ) {
				alreadyOwned.value.push(res.data.result[i].couponGroupId)
			}
		})
    for (let i = 0; i < res.data.result.length; i++) {
      console.log(coupons.value[i].shopId)
      if(coupons.value[i].shopId > -1){
        getStoreById({
          storeId:coupons.value[i].shopId
        }).then(res => {
          coupons.value[i].shopName = res.data.result.name
        })
      }
    }
	});
}

const size = ref<ComponentSize>('default')
const iconStyle = computed(() => {
	const marginMap = {
		large: '8px',
		default: '6px',
		small: '4px',
	}
	return {
		marginRight: marginMap[size.value] || marginMap.default,
	}
})

const handleReceive = (couponId) => {
	couponReceiveInfo({
		userId: userId.value,
		couponId: couponId,
	}).then(() => {
		ElMessage.success("领取优惠券成功！");
		location.reload();
	}).catch(res => {
		ElMessage.error("领取优惠券失败！");
	});
}
const handleDelete = (couponId) => {
	delete_coupon_id.value = couponId;
	delete_coupon_open.value = true;
	// TODO: Also receives coupons, but set the amount to 0 immediately.
}

onMounted(() => {
	fetchCoupons(1, 2 << 16, role.value != 'STAFF' ? -1 : storeId.value, "");
})

</script>

<template>
	<div style="display: flex; justify-content: space-between;">
		<h1>已发布优惠券 ({{ !filterReceived ? coupons.length :
			coupons.filter(c => (c.count > 0 && !alreadyOwned.includes(c.couponId))).length }})</h1>
		<div style="display: flex; justify-content: space-between; margin-top: 30px">
			<span style="margin: 5px 5px 0 0">仅查看可领取</span>
            <el-switch v-model="filterReceived"/>
		</div>
	</div>

  <el-empty v-if="coupons.length == 0 && role == 'CUSTOMER'" description="暂无可领取的优惠券！" style="height: 0; padding-top: 200px"/>
  <el-empty v-if="coupons.length == 0 && role != 'CUSTOMER'" description="暂无已发布的优惠券！" style="height: 0; padding-top: 200px"/>
	<el-scrollbar v-if="coupons.length != 0" height="460px">
		<el-card
			v-for="coupon in coupons.slice().reverse()
				.filter(c => (filterReceived ? (c.count > 0 && !alreadyOwned.includes(c.couponId)) : c))"
			style="height: 210px"
			class="coupon-card"
		>
			<div style="display: flex; justify-content: space-between;">
				<span v-if="!coupon.type" style="font-size: 28px; font-weight: bold; font-family: 'Comic Sans MS',serif; color: #ff6d12">
					-{{ coupon.triggerReduction }}￥
				</span>
				<span v-else style="font-size: 24px; font-weight: bold; font-family: 'Comic Sans MS',serif; color: #0f83fa">
					Bluewhale!
				</span>
				<span style="margin-top: 10px">
					{{ coupon.name.length > 12 ? coupon.name.substring(0, 12) + "..." : coupon.name }}
				</span>
				<p></p><p></p><p></p><p></p>
				<div v-if="role == 'CUSTOMER'">
					<el-button
						v-if="alreadyOwned.includes(coupon.couponId)"
						disabled
						type="success">已领取
					</el-button>
					<el-button
						@click.prevent="handleReceive(coupon.couponId)"
						v-if="!alreadyOwned.includes(coupon.couponId) && coupon.count > 0"
						type="primary">领取
					</el-button>
					<el-button
						v-if="coupon.count == 0"
						disabled
						type="warning">已领完
					</el-button>
				</div>
				<div v-if="role != 'CUSTOMER'">
					<el-button
						@click.prevent="handleDelete(coupon.couponId)"
						disabled
						type="danger">删除
					</el-button>
				</div>
			</div>

			<el-divider style="margin: 10px" />
			<div style="display: flex; flex-direction: row; align-content: center">
				<img src="src/assets/img/coupon_red.png" alt="" style="width: 60px; height: 60px; margin: 10px">
				<div>
					<el-descriptions
						class="margin-top"
						:column="2"
						:size="size"
						border
					>
						<el-descriptions-item>
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><money/></el-icon>优惠类型
								</div>
							</template>
							<el-tooltip
								v-if="coupon.type"
								content="
									优惠规则：
									0-100元区间打九五折；
									100-200元区间打九折；
									200-300元区间打八五折；
									300-400元区间打八折；
									400-500元区间打七五折；
									500元以上区间打七折。
								"
							>
								<el-tag size="small">蓝鲸券</el-tag>
							</el-tooltip>
							<el-tooltip
								v-else
								content="仅在消费价格达到指定数额时生效，减免固定金额。"
							>
								<el-tag size="small" type="danger">满减券</el-tag>
							</el-tooltip>
						</el-descriptions-item>
						<el-descriptions-item>
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><goods/></el-icon>优惠门店
								</div>
							</template>
							<el-tooltip
								v-if="coupon.isGlobal"
								content="在任何门店消费均可生效。"
							>
								<el-tag size="small" type="success">全局优惠</el-tag>
							</el-tooltip>
							<el-tooltip
								v-else
								:content="`仅在${coupon.shopName}门店生效`"
							>
								<el-tag size="small" type="success">{{coupon.shopName}}</el-tag>
							</el-tooltip>
						</el-descriptions-item>
						<el-descriptions-item>
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><calendar/></el-icon>截止日期
								</div>
							</template>
							{{ coupon.deadline.substring(0, 10) }}
						</el-descriptions-item>
						<el-descriptions-item>
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><document-copy/></el-icon>剩余张数
								</div>
							</template>
							{{ coupon.count }}
						</el-descriptions-item>
					</el-descriptions>
				</div>
			</div>
		</el-card>
	</el-scrollbar>
	<DeleteCoupon
		v-model="delete_coupon_open"
		:coupon_id="delete_coupon_id"
	/>
</template>

<style scoped>
.infinite-list {
	height: 300px;
	padding: 0;
	margin: 0;
	list-style: none;
}
.infinite-list .infinite-list-item + .list-item {
	margin-top: 10px;
}

.coupon-card {
	margin: 10px;
	height: 120px;
	border: 2px solid rgb(196, 196, 196);
}
</style>