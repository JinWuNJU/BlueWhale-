<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {couponCreateInfo} from "../../api/coupon.ts";
import {getStoreById} from "../../api/resolve_id.ts";

const name = ref("");
const descriptions = ref("");
const isSpecial = ref(true);
const triggerCondition = ref(100);
const triggerReduction = ref(20);
const count = ref(1);
const deadline = ref("");

const role = ref(sessionStorage.role);
const shopId = ref(sessionStorage.storeId);
const shopName = ref("");

const hasNameInput = computed(() => name.value != '')
const hasDescInput = computed(() => descriptions.value != '')
const hasDDLInput = computed(() => deadline.value != '')
const allowSubmit = computed(() => {
	return (hasNameInput.value && hasDescInput.value && hasDDLInput.value)
})

const handleSubmit = () => {
	couponCreateInfo({
		name: name.value,
		description: descriptions.value,
		type: isSpecial.value,
		isGlobal: (role.value == 'CEO'),
		shopId: (role.value == 'CEO' ? -1 : shopId.value),
		count: count.value,
		deadline: deadline.value,
		triggerCondition: triggerCondition.value,
		triggerReduction: triggerReduction.value,
	}).then(() => {
		ElMessage.success("发布优惠券组成功！");
		location.reload();
	}).catch(() => {
		ElMessage.error("发布优惠券组失败！");
	})
}
const handleReset = () => {
	name.value = "";
	descriptions.value = "";
	isSpecial.value = true;
	triggerCondition.value = 100;
	triggerReduction.value = 20;
	count.value = 1;
	deadline.value = "";
	ElMessage.info("已清除待发布优惠券组信息！");
}
onMounted(() => {
  if(shopId.value !== 'null'){
    getStoreById({
      storeId: shopId.value
    }).then(res => {
      shopName.value = res.data.result.name
    })
  }
})
</script>

<template>
	<h1>发布新的优惠券</h1>
	<div class="main-form">
		<el-scrollbar style="height: 460px;">
			<el-form class="modal-form">
				<label>优惠券名称</label>
				<input type="text" v-model="name" placeholder="请输入优惠券名称" required/>
				<label>优惠券适用范围</label>
				<input type="text" v-if="role=='STAFF'" :placeholder="shopName" disabled/>
				<input type="text" v-if="role=='CEO'" placeholder="全局优惠券" disabled/>
				<label>优惠券描述</label>
				<textarea type="text" v-model="descriptions" placeholder="简单介绍一下你的优惠券吧" required/>
				<div style="display: flex; flex-direction: row; align-content: center">
					<div>
						<label>优惠券组类别：</label>
						<el-select
							v-model="isSpecial"
							placeholder="请选择"
							style="width: 150px; padding: 5px"
						>
							<el-option :value=false label="满减券"/>
							<el-option :value=true label="蓝鲸券"/>
						</el-select>
					</div>
					<div v-if="isSpecial==false">
						<label>触发最少金额：</label>
						<el-input-number
							v-model="triggerCondition"
							style="width: 120px; padding: 1px"
							:min="1"
							:max="65536"
						/>
					</div>
					<div v-if="isSpecial==false">
						<label>优惠额度：</label>
						<el-input-number
							v-model="triggerReduction"
							style="width: 120px; padding: 1px"
							:min="1"
							:max="triggerCondition"
						/>
					</div>
				</div>
				<label>发布数量（范围为1~10）：</label>
				<el-input-number
					v-model="count"
					style="width: 150px; padding: 5px"
					:min="1"
					:max="10"
				/>
				<label>优惠券截止日期：</label>
				<el-date-picker
					v-model="deadline"
					type="date"
					placeholder="请输入优惠券截止日期"
					size="large"
				/>
			</el-form>
			<br><br>
		</el-scrollbar>

		<div class="btn">
			<el-button
				class="modal-btn"
				@click.prevent="handleSubmit"
				:disabled="!allowSubmit"
				type="primary"
			>发布
			</el-button>
			<el-button
				class="modal-btn"
				@click.prevent="handleReset"
				type="warning"
			>清空
			</el-button>
		</div>

	</div>
</template>

<style scoped>
.modal-form {
	margin: 10px;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.modal-form label {
	font-size: 16px;
	font-weight: 750;
	margin-top: 10px;
}

.modal-form input {
	font-size: 16px;
	width: 400px;
	padding: 10px;
	border: 1px solid #ddd;
	border-radius: 0.5rem;
	margin: 10px;
}

.modal-form textarea {
	font-size: 16px;
	width: 400px;
	height: 80px;
	padding: 10px;
	border: 1px solid #ddd;
	border-radius: 0.5rem;
	margin: 10px;
}

.btn {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
}

.modal-btn {
	display: inline-block;
	font-size: 20px;
	font-weight: 1000;
	transition: all 0.3s;
	width: 160px;
	height: 40px;
	margin: 10px;
}
</style>