<script setup lang="ts">
import Header from "../../components/Header.vue"
import {ref, onMounted} from "vue";
import {fullScreenLoading} from "../../utils/visuals.ts";
import {storeInfo, storeCountInfo} from "../../api/store.ts";
import {addFavInfo, getFavInfo, unfavInfo} from "../../api/fav.ts";
import {getUserById} from "../../api/resolve_id.ts";
import {Star, StarFilled} from "@element-plus/icons-vue";

const page_loaded = ref(false);
const current_detail = ref(1);
const current_page = ref(1);

const favs = ref([]);
const shops = ref([]);
const sortMethod = ref("");
const storeCount = ref(0);
const searchText = ref("");

const role = sessionStorage.getItem('role');
const userId = sessionStorage.getItem('userId');
const staffShopId = ref();

const gen_url = (idx) => {
	return "/storeDetail/" + shops.value[idx].shopId
}

const switch_detail = (idx) => {
	current_detail.value = idx + 1;
}

const switch_fav = (sid) => {
	if ( favs.value.includes(sid) ) {
		unfavInfo({
			userId: parseInt(userId),
			sid: sid,
			type: 1,
		}).then(() => {
			ElMessage.success("取消收藏成功！");
			location.reload();
		})
	}
	else {
		addFavInfo({
			userId: parseInt(userId),
			sid: sid,
			type: 1,
		}).then(() => {
			ElMessage.success("收藏成功！");
			location.reload();
		})
	}
}

const switch_page = () => {
	page_loaded.value = false;
	storeInfo({
		page: current_page.value,
		pageSize: 8,
	    searchText: searchText.value,
	    sortBy: sortMethod.value
	}).then(res => {
		shops.value = res.data.result;
		page_loaded.value = true;
	})
}

onMounted(async () => {
	fullScreenLoading()
	switch_page();
    storeCountInfo().then(res => {
        storeCount.value = res.data.result
    })
	getFavInfo({
		userId: parseInt(sessionStorage.getItem('userId')),
		type: 1
	}).then(res => {
		res.data.result.forEach(e => {
			favs.value.push(e.type == 1 ? e.sid : (e.type == 2 ? e.pid : e.uid))
		});
	})
	if ( role == 'STAFF' ) {
		getUserById({
			userId: parseInt(userId),
		}).then(res => {
			staffShopId.value = res.data.result.shopId;
		})
	}
})
</script>

<template>
    <Header/>
    <body class="common-layout">
    <div
        class="empty"
        v-if="(page_loaded && !shops.length)"
        style="display: flex; flex-direction: column; text-align: center"
    >
	    <el-empty description=" " />
	    <h1>当前没有商店 :(</h1>
	    <div style="margin-bottom: 40px" class="btn">
		    <router-link to="/home" v-slot="{navigate}">
			    <el-button class="modal-btn-side" @click="navigate">返回主页</el-button>
		    </router-link>
		    <router-link v-if="role == 'MANAGER'" to="/createStore" v-slot="{navigate}">
			    <el-button class="modal-btn-side" type="success" @click="navigate">创建新的商店</el-button>
		    </router-link>
	    </div>
    </div>
        <el-container v-if="shops[current_detail - 1] !== undefined">
	        <el-aside width="640px" class="page-aside">
		        <h1 style="font-size: 88px; margin: 30px 0 50px 100px"> 商店列表</h1>
		        <el-form class="modal-form">
			        <div style="display: flex; flex-direction: row;">
				        <input type="text" v-model="searchText" placeholder="找不到想要的商店？试试搜索吧" required/>
				        <svg width="40px" @click="switch_page" color="white" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" data-v-ea893728=""><path fill="currentColor" d="m795.904 750.72 124.992 124.928a32 32 0 0 1-45.248 45.248L750.656 795.904a416 416 0 1 1 45.248-45.248zM480 832a352 352 0 1 0 0-704 352 352 0 0 0 0 704"></path></svg>
			            <el-select
			                v-model="sortMethod"
			                placeholder="请选择排序方式"
			                @change="switch_page"
			                style="width: 200px;margin-left: 10px;"
			            >
				            <el-option value="None" label="默认排序"/>
				            <el-option value="Alphabetic" label="按商店名称排序"/>
				            <el-option value="Score" label="按商店评分排序"/>
			            </el-select>
			        </div>
		        </el-form>
		        <el-scrollbar style="height: 500px; margin: 15px 0 15px 0; border-right: white dotted 12px;" height="600px">
			        <div
				        v-for="i in Math.min(8, shops.length)"
				        class="shop-box"
				        style="margin-left: 100px; margin-bottom: 20px">
					    <img
					        :src="shops[i - 1].picUrl"
					        alt=""
					        class="shop-pic"
					        @click="switch_detail(i - 1)"
					        style="width: 192px; height: 128px; min-width: 192px; min-height: 128px;"
					    >
					    <div class="info-box">
					        <span
						        v-if="role == 'STAFF' && staffShopId == shops[i - 1].shopId"
						        style="font-size: 30px; color: #ffc557">
					            ⭐{{shops[i - 1].name}}
					        </span>
						    <span
							    v-else style="font-size: 30px;color: #333">
					            {{shops[i - 1].name}}
					        </span>
						    <br>
						    <span>
							    {{shops[i - 1].description.length > 30 ? shops[i - 1].description.substring(0, 30) + "..." : shops[i - 1].description}}
						    </span>
					    </div>
		            </div>
		        </el-scrollbar>
		        <div style="display: flex; flex-direction: column; align-items: center;">
			        <el-pagination
			            background
			            layout="prev, pager, next"
			            v-model:current-page="current_page"
			            @current-change="switch_page"
			            :default-page-size=8
			            :total=storeCount
			        />
		        </div>
            </el-aside>
            <el-main>
		        <div
		            class="detail-container"
		            style="margin-top: 40px"
		            v-if="shops[current_detail - 1] !== undefined"
		        >
		        <img
		            style="width: 800px; height: 480px"
		            class="detail-image"
		            data-image-role="target"
		            :src="shops[current_detail - 1].picUrl"
		            alt=""
		        >
		        <el-card style="margin: 10px; border: 2px solid rgb(196, 196, 196); width: 900px; height: 170px">
			        <div style="display: flex; justify-content: space-between;">
				        <h1 style="padding: 0; margin: 0; font-size: 24px">
					        {{ shops[current_detail - 1].name }}
				        </h1>
                <div>
                  <el-icon :size="50" v-if="!favs.includes(shops[current_detail - 1].shopId)" @click="switch_fav(shops[current_detail - 1].shopId)"><Star /></el-icon>
                  <el-icon color="#ff0" :size="60" v-if="favs.includes(shops[current_detail - 1].shopId)" @click="switch_fav(shops[current_detail - 1].shopId)"><StarFilled /></el-icon>
                </div>
			        </div>

			        <el-rate v-if="shops[current_detail-1].score > 0"
			                 disabled
			                 show-score
			                 size="large"
			                 :model-value=shops[current_detail-1].score.toFixed(1)
			                 text-color="#ff6000"
			        />
			        <el-rate v-else
			                 disabled
			                 show-score
			                 size="large"
			                 :model-value=shops[current_detail-1].score
			                 score-template="当前商店暂无评分"
			                 text-color="#ff6000"
			        />
			        <br>
			        <span style="padding-bottom: 20px">{{ shops[current_detail - 1].description }}</span>
		        </el-card>
		        <div>
		            <router-link to="/home" v-slot="{navigate}">
		                <el-button class="modal-btn-side" @click="navigate">返回主页</el-button>
		             </router-link>
		            <router-link :to="gen_url(current_detail - 1)" v-if="shops[current_detail - 1] !== undefined" v-slot="{navigate}">
		                <el-button class="modal-btn" type="primary" @click="navigate">进入该商店</el-button>
		            </router-link>
		            <router-link to="/createStore" v-slot="{navigate}">
		                <el-button class="modal-btn-side" type="success" @click="navigate">创建新的商店</el-button>
		            </router-link>
	            </div>
		      </div>
            </el-main>
        </el-container>
  </body>
  <router-view />
</template>


<style scoped>
body {
	font-family: 'Nunito', sans-serif;
	font-weight: 400;
	color: #333;
	background-image: linear-gradient(to top left, #a0fdfd 0%, #409eff 100%);
    background-size: cover;
    overflow: hidden;
}

.modal-form {
	margin-left: 100px;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.modal-form input {
	font-size: 16px;
	width: 360px;
	height: 10px;
	padding: 12px;
	border: 1px solid #ddd;
	border-radius: 0.5rem;
	margin-left: 10px;
}

.shop-box {
	margin: 10px;
	display: flex;
	flex-direction: row;
}

.info-box {
	margin: 10px;
  display: block;
}

.shop-pic {
	border: 2px solid rgb(255, 255, 255);
	transition: transform 100ms ease-in-out;
}
.shop-pic:hover {
	transform: scale(1.1);
}

.detail-image {
	width: 80%;
	border: 8px solid rgb(255, 255, 255);
}

.detail-container {
	margin: 10px;
	display: flex;
	align-content: center;
	align-items: center;
	flex-direction: column;
}

.modal-btn {
	display: inline-block;
	font-size: 1.5rem;
	font-weight: 1000;
	width: 240px;
	height: 50px;
}

.modal-btn-side {
	display: inline-block;
	font-size: 1.3rem;
	font-weight: 800;
	width: 160px;
	height: 40px;
	margin-left: 10px;
	margin-right: 10px;
}

.btn {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
}

h1 {
  color: #333;
}
span {
  color: #333;
}
.star-icon {
  color: #ff0;
}

</style>
