<script lang="ts">
import {commentPost} from "../api/comment.ts";

export default {
	data() {
		return {
			score: 0,
			comment: "",
		}
	},
	methods: {
		resetComments() {
			this.score = 0;
			this.comment = "";
			ElMessage.info("已清空您的评论！");
		},
		handleSubmit() {
			commentPost({
				pid: this.product_id,
				shopId: this.shop_id,
				userId: this.user_id,
				comment: this.comment,
				score: this.score,
        oid:this.order_id,
			}).then(() => {
				ElMessage.success("评价成功！")
				location.reload()
			})
		}
	},
	props: [
		"product_id",
		"shop_id",
		"user_id",
    "order_id"
	],
	mounted() {

	}
}
</script>

<template>
	<el-dialog title="评价该商品" width="500" center>
		<div class="box">
			<span>感谢您的购买！请您对商品进行评价：</span>
			<el-rate
				v-model="this.score"
				size="large"
				allow-half
			/>
			<el-input
				v-model="this.comment"
				style="width: 450px; margin: 20px"
				:rows="6"
				type="textarea"
				placeholder="请输入您的评价..."
			/>
				<div class="dialog-footer">
					<el-button @click="resetComments">清空</el-button>
					<el-button type="primary" @click="handleSubmit">确认</el-button>
				</div>
		</div>

	</el-dialog>
</template>

<style scoped>
.box {
	display: flex;
	flex-direction: column;
	align-items: center;
}
</style>