<script setup lang="ts">
import {onMounted, ref} from "vue";
import {fullScreenLoadingShort} from "../../utils/visuals.ts";
import {Location, ShoppingCart, User,} from '@element-plus/icons-vue'
import Header from "../../components/Header.vue";
import {getFavInfo, unfavInfo} from "../../api/fav.ts";
import {getProductById, getStoreById, getUserById} from "../../api/resolve_id.ts";


onMounted(() => {
	fullScreenLoadingShort();
	getItems();
});

const items = ref([])
const viewingItem = ref(1);
const current_page = ref(1);

const userId = ref(sessionStorage.userId);

const gen_url = (id) => {
	return "/storeDetail/" + id;
}

const switchPages = () => {

}

const switchViews = (item) => {
	viewingItem.value = item;
	getItems();
}

const removeFav = (id, type) => {
	unfavInfo({
		userId: parseInt(userId.value),
		sid: type == 1 ? id : -1,
		pid: type == 2 ? id : -1,
		uid: type == 3 ? id : -1,
		type: type,
	}).then(() => {
		ElMessage.success("取消收藏成功！");
		getItems();
	})
}

const getItems = () => {
	getFavInfo({
		userId: userId.value,
		type: viewingItem.value,
	}).then(res => {
		items.value = res.data.result;
		for ( let i = 0; i < items.value.length; i++ ) {
			switch ( viewingItem.value ) {
				case 1:
					getStoreById({storeId: items.value[i].sid})
						.then(res => {items.value[i].detail = res.data.result;})
					break;
				case 2:
					getProductById({productId: items.value[i].pid})
						.then(res => {items.value[i].detail = res.data.result;})
					break;
				case 3:
					getUserById({userId: items.value[i].uid})
						.then(res => {items.value[i].detail = res.data.result;})
					break;
			}
		}
	})
}
</script>

<template>
	<body>
		<Header/>
		<el-main class="main">
			<div style="display: flex; flex-direction: row">
				<h1 style="font-size: 88px; margin: 20px 0 0 100px; color: #363636">我的收藏</h1>
				<router-link to="/home" v-slot="{navigate}">
					<el-button
						@click="navigate"
						style="margin: 80px 0 0 20px; font-size: 24px; width: 135px; height: 50px">
						返回主页
					</el-button>
				</router-link>
			</div>
			<div style="display: flex; flex-direction: row; align-content: center">
				<el-row style="margin: 20px 0 0 100px; padding: 0; width: 300px">
					<el-col style="width: 200px">
						<el-menu default-active="1">
							<el-menu-item index="1" @click="switchViews(1)">
								<el-icon><Location /></el-icon>
								<span>已收藏的商店</span>
							</el-menu-item>
							<el-menu-item index="2" @click="switchViews(2)">
								<el-icon><ShoppingCart /></el-icon>
								<span>已收藏的商品</span>
							</el-menu-item>
							<el-menu-item index="3" @click="switchViews(3)">
								<el-icon><User /></el-icon>
								<span>已关注的用户</span>
							</el-menu-item>
						</el-menu>
					</el-col>
				</el-row>

				<el-card
					class="right-card"
					style="width: 1100px; height: 600px; border: 5px solid rgb(196, 196, 196); margin: 12px"
				>
					<h1 v-if="viewingItem == 1">已收藏的门店</h1>
					<h1 v-if="viewingItem == 2">已收藏的商品</h1>
					<h1 v-if="viewingItem == 3">已关注的用户</h1>

					<div style="display: flex; flex-direction: column; align-content: center">
						<div style="display: flex; flex-direction: row; align-content: center">
							<el-empty v-if="viewingItem == 3" style=" margin: 40px 0 0 450px" description="暂不支持用户关注！"/>
							<el-card
								class="item-card"
								v-for="i in Math.min(4, items.length)"
								v-if="viewingItem == 1 || viewingItem == 2"
								style="margin: 10px; width: 240px; height: 400px"
							>
								<div v-if="items[i - 1].detail != undefined">
									<img
										alt=""
										v-if="viewingItem == 1"
										:src="items[i - 1].detail.picUrl"
										style="width: 200px; height: 150px"
									/>
									<img
										alt=""
										v-if="viewingItem == 2"
										:src="items[i - 1].detail.picUrl[0]"
										style="width: 200px; height: 150px"
									/>
									<el-divider style="margin: 10px 0 0 0"/>
									<h3>{{ items[i - 1].detail.name }}</h3>
									<p style="word-wrap: break-word; height: 90px">
										{{ items[i - 1].detail.description.length > 50
										? items[i - 1].detail.description.substring(0, 50) + "..."
										: items[i - 1].detail.description }}
									</p>
									<div style="float: right">
										<el-button
											type="info"
											@click="removeFav(
												viewingItem == 1 ? items[i - 1].sid : items[i - 1].pid, viewingItem)"
										>移除
										</el-button>
										<router-link
											:to="gen_url(viewingItem == 1 ? items[i - 1].sid : items[i - 1].detail.shopId)"
											v-slot="{navigate}">
											<el-button
												@click="navigate"
												type="primary"
											>浏览
											</el-button>
										</router-link>
									</div>
								</div>
							</el-card>
						</div>
						<div style="display: flex; flex-direction: column; align-content: center">
							<el-pagination
								v-model:current-page="current_page"
								v-if="viewingItem != 3 && items.length > 0"
								background
								style="margin-left: 480px"
								layout="prev, pager, next"
								@current-change="switchPages"
								:disabled="items.length == 0"
								:default-page-size=4
								:total=items.length
							/>
						</div>
					</div>
				</el-card>
			</div>
		</el-main>
	</body>
</template>

<style scoped>
body {
	background-image: url("../../assets/img/background/collection.jpg");
	background-size: cover;
}
.item-card {
	border: 2px solid rgb(196, 196, 196);
	transition: transform 100ms ease-in-out;
}
.item-card:hover {
	transform: scale(1.1);
}
</style>