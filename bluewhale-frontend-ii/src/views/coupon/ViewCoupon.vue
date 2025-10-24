<script setup lang="ts">
import {computed, onMounted, ref} from "vue";

import {ComponentSize} from "element-plus";
import {Money, Calendar, Goods, Memo, Coin} from '@element-plus/icons-vue'
import {couponOwnedInfo} from "../../api/coupon.ts";
import {getCouponById, getStoreById} from "../../api/resolve_id.ts";

const role = ref(sessionStorage.role);
const userId = ref(sessionStorage.userId);
const size = ref<ComponentSize>('default');
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

const coupons = ref([]);

async function fetchOwnedCoupons() {
    const res1 = await couponOwnedInfo({
        userId: userId.value,
    })
	coupons.value = res1.data.result.filter(c => !c.isUsed);
	for ( let i = 0; i < coupons.value.length; i++ ) {
	    const res2 = await getCouponById({
		    couponId: coupons.value[i].couponGroupId
	    })
	    coupons.value[i].couponInfo = res2.data.result;
	    if ( coupons.value[i].couponInfo.shopId > -1 ) {
	        const res3 = await getStoreById({
		        storeId:coupons.value[i].couponInfo.shopId
	        })
	        coupons.value[i].shopName = res3.data.result.name
	    }
	}
}

onMounted(() => {
	fetchOwnedCoupons();
})

</script>

<template>
	<h1 v-if="role == 'CUSTOMER'">我的优惠券 ({{ coupons.length }})</h1>
	<div style="display: flex; flex-direction: row; align-content: center">
		<h1 v-if="role == 'STAFF'">当前商店优惠券 ({{ coupons.length }})</h1>
		<h1 v-if="role == 'CEO'">全部优惠券 ({{ coupons.length }})</h1>
	</div>
	<div style="height: 500px; margin-bottom: 20px">
		<el-scrollbar max-height="600px">
			<el-collapse accordion>
				<el-collapse-item
					v-for="c in coupons"
                    :title="c.couponInfo ? '💴 ' + c.couponInfo.name : '默认优惠券名称'"
				>
					<div v-if="c.couponInfo" style="display: flex; flex-direction: row; align-content: center">
						<img src="src/assets/img/coupon_green.png" alt="" style="width: 60px; height: 60px; margin: 10px">
						<p>{{ c.couponInfo.description }}</p>
					</div>
					<el-descriptions
						class="margin-top"
						v-if="true"
						:column="2"
						:size="size"
						border
					>
						<el-descriptions-item v-if="c.couponInfo">
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><money/></el-icon>优惠类型
								</div>
							</template>
							<el-tag size="small" type="danger">{{ c.couponInfo.type ? "蓝鲸券" : "满减券" }}</el-tag>
						</el-descriptions-item>
						<el-descriptions-item v-if="c.couponInfo">
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><goods/></el-icon>优惠范围
								</div>
							</template>
							<el-tag size="small">{{
                  c.couponInfo.isGlobal ? "全局优惠券" : `${c.shopName}`
                }}</el-tag>
						</el-descriptions-item>
						<el-descriptions-item v-if="c.couponInfo">
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><memo/></el-icon>优惠券编号
								</div>
							</template>
							010-{{ c.couponInfo.couponId * 65536 + userId * 256 }}
						</el-descriptions-item>
						<el-descriptions-item v-if="c.couponInfo">
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><calendar/></el-icon>截止日期
								</div>
							</template>
							{{ c.couponInfo.deadline.substring(0, 10) }}
						</el-descriptions-item>
						<el-descriptions-item v-if="c.couponInfo">
							<template #label>
								<div class="cell-item">
									<el-icon :style="iconStyle"><coin/></el-icon>优惠规则
								</div>
							</template>
							<el-tooltip
								v-if="c.couponInfo.type"
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
								<span v-if="c.couponInfo.type" style="text-decoration: underline">分段优惠 (?)</span>
							</el-tooltip>
							<span v-else>满 {{ c.couponInfo.triggerCondition }} 减 {{ c.couponInfo.triggerReduction }}</span>
						</el-descriptions-item>
					</el-descriptions>
				</el-collapse-item>
			</el-collapse>
		</el-scrollbar>
	</div>
</template>

<style scoped>

</style>