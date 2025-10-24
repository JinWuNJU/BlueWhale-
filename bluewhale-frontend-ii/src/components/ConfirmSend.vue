<script lang="ts">

import {orderDelivery} from "../api/order.ts";

export default {
	methods: {
		handleConfirm() {
			orderDelivery(this.order_id).then(() => {
				ElMessage.success("确认发货成功！")
				location.reload()
			})
		}
	},
	props: [
		"order_id"
	]
}

</script>

<template>
	<el-dialog
		width="500"
		destroy-on-close
    draggable
		center
	>
		<el-result
			icon="warning"
			title="确认发货"
			style="padding: 0"
			sub-title="您确认已经向顾客派送商品了吗？（确认后无法撤销）"
		/>
		<template #footer>
			<div class="dialog-footer">
				<el-button type="primary" @click="handleConfirm">确认</el-button>
			</div>
		</template>
	</el-dialog>
</template>