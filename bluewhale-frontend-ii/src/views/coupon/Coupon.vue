<script setup lang="ts">
import Header from "../../components/Header.vue";
import ViewCoupon from "./ViewCoupon.vue";
import ReceiveCoupon from "./ReceiveCoupon.vue";
import ReleaseCoupon from "./ReleaseCoupon.vue";
import {onMounted, ref} from "vue";
import {fullScreenLoadingShort} from "../../utils/visuals.ts";

const role = ref(sessionStorage.role);

onMounted(() => {
	fullScreenLoadingShort();
});
</script>

<template>
	<body>
		<Header/>
		<div style="display: flex; flex-direction: row">
			<h1 style="font-size: 88px; margin: 20px 0 0 100px; color: white">优惠券信息</h1>
			<router-link to="/home" v-slot="{navigate}">
				<el-button
					@click="navigate"
					style="margin: 80px 0 0 20px; font-size: 24px; width: 135px; height: 50px">
					返回主页
				</el-button>
			</router-link>
		</div>

		<el-main class="main-container">
			<el-card v-if="role == 'CUSTOMER'" class="left-card">
				<ViewCoupon/>
			</el-card>
			<el-card v-else class="left-card">
				<ReceiveCoupon/>
			</el-card>
			<el-card v-if="role == 'CUSTOMER'" class="right-card">
				<ReceiveCoupon/>
			</el-card>
			<el-card v-else-if="role != 'MANAGER'" class="right-card">
				<ReleaseCoupon/>
			</el-card>
		</el-main>
	</body>
</template>

<style scoped>
body {
	background-image: url("src/assets/img/background/supermarket2.jpg");
	background-size: cover;
}
.main-container {
	display: flex;
	flex-direction: row;
	padding: 15px;
	gap: 5px;
	justify-content: center;
}

.left-card {
	width: 55%;
	height: 650px;
	border: 5px solid rgb(196, 196, 196);
	transition: transform 100ms ease-in-out;
}
.right-card {
	width: 35%;
	border: 5px solid rgb(196, 196, 196);
	transition: transform 100ms ease-in-out;
}
.left-card:hover .right-card:hover {
	transform: scale(1.03);
}

</style>