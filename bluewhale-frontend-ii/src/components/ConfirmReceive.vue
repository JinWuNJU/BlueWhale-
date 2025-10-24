<script lang="ts">

import {orderReceive} from "../api/order.ts";

export default {
	data() {
		return {
			centerDialogVisible: false
		}
	},
	methods: {
		handleConfirm() {
			orderReceive(this.order_id).then(() => {
				ElMessage.success("确认收货成功！")
				location.reload()
			})
		}
	},
	props: [
		"order_id"
	],
	mounted() {
		this.centerDialogVisible = true;
	}
}
</script>

<template>
	<el-dialog
		v-model="centerDialogVisible"
		width="500"
		destroy-on-close
    draggable
		center
	>
		<el-result
			icon="warning"
			title="确认收货"
			style="padding: 0"
			sub-title="您确认已经收到商品了吗？（确认后无法撤销）"
		/>
		<template #footer>
			<div class="dialog-footer">
				<el-button type="primary" @click="handleConfirm">确认</el-button>
			</div>
		</template>
	</el-dialog>
</template>