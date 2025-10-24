<script lang="ts">
import {commentInfo} from "../api/comment.ts";


export default {
	data() {
		return {
			comments: [],
		}
	},
	props: [
		"product_id",
		"user_id"
	],
	mounted() {
		commentInfo({
			pid: 0,
			shopId: 0,
		}).then(res => {
			this.comments = res.data.result;
		})
	}
}

</script>

<template>
	<el-timeline style="max-width: 600px">
		<el-timeline-item timestamp="2018/4/12" placement="top">
			<el-card>
				<h4>Update Github template</h4>
				<p>Tom committed 2018/4/12 20:46</p>
			</el-card>
		</el-timeline-item>
		<el-timeline-item timestamp="2018/4/3" placement="top">
			<el-card>
				<h4>Update Github template</h4>
				<p>Tom committed 2018/4/3 20:46</p>
			</el-card>
		</el-timeline-item>
		<el-timeline-item timestamp="2018/4/2" placement="top">
			<el-card>
				<h4>Update Github template</h4>
				<p>Tom committed 2018/4/2 20:46</p>
			</el-card>
		</el-timeline-item>
	</el-timeline>
</template>

<style scoped>

</style>