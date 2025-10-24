<script lang="ts">

export default {
	methods: {
		handleConfirm() {
			// TODO: Remove the coupon
			ElMessage.success("删除优惠券成功！")
			location.reload();
		}
	},
	props: [
		"coupon_id"
	]
}

</script>

<template>
	<el-dialog
		title="删除优惠券"
		width="500"
		destroy-on-close
		center
	>
		<el-result
			icon="warning"
			title="删除优惠券"
			style="padding: 0"
			sub-title="您确认删除该优惠券吗？（确认后无法撤销）"
		/>
		<template #footer>
			<div class="dialog-footer">
				<el-button type="primary" @click="handleConfirm">确认</el-button>
			</div>
		</template>
	</el-dialog>
</template>