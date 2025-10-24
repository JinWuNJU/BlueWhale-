<!--Lab2新增-商店详情界面-->
<script setup lang="ts">
import Header from "../../components/Header.vue"
import ViewProduct from "./products/ViewProduct.vue"
import CreateProduct from "./products/CreateProduct.vue";
import {fullScreenLoading} from "../../utils/visuals.ts";

import {onMounted, ref, watch} from "vue";
import { useRoute } from 'vue-router';
import {parseProductCategory, parseProductSubCategory} from "../../utils";
import {productCountInfo, productInfo} from "../../api/product.ts";
import {commentInfo} from "../../api/comment.ts";
import {storeInfo} from "../../api/store.ts";
import {getStoreById, getUserById} from "../../api/resolve_id.ts";
import {getFavInfo} from "../../api/fav.ts";

const route = useRoute();
const storeId_ = route.params.storeId.toString();
const storeContent = ref({});

const viewing = ref(-1);
const products = ref([]);
const comments = ref([]);
const commenters = ref([]);
const store_img_url = ref('')
const productCount = ref(0)
const favs = ref([])

const drawer = ref(false);
const creating_product = ref(false);

const page_loaded = ref(false);
const current_page = ref(1);
const searchText = ref("");

const sortMethod = ref("");
const filterCategory = ref("");
const filterSubCategory = ref("");
const filterPriceMin = ref();
const filterPriceMax = ref();
const search = ref(false)

const op_permission = ref(
    sessionStorage.role == 'STAFF' &&
    sessionStorage.storeId == storeId_
)

const handleDetail = (idx) => {
  drawer.value = true;
  viewing.value = idx
}

const createProduct = () => {
  creating_product.value = true;
}

const switch_page = (isSearch:boolean) => {
  page_loaded.value = false
  productInfo({
    shopId: parseInt(storeId_),
    page: current_page.value,
    pageSize: 12,
    sortBy: sortMethod.value,
    type: filterCategory.value,
    subtype: filterSubCategory.value,
    priceMin: filterPriceMin.value,
    priceMax: filterPriceMax.value,
    searchText: searchText.value
  }).then(res => {
    products.value = res.data.result;
    page_loaded.value = true
  })
  search.value = isSearch
}

const highlightKeyword = (text:string, keyword:string) => {
  const keywords = keyword.split("");
  const escapedKeywords = keywords.map(keyword =>
      keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') // 使用正则表达式转义元字符
  );
  const keywordPattern = escapedKeywords.join("|");
  // 使用正则表达式进行全局大小写不敏感的匹配
  const regex = new RegExp(keywordPattern, 'gi');
  // 使用replace方法替换匹配到的关键词，并用<span class="highlight">包裹起来
  const res = text.replace(regex, match =>
      `<span class="highlight">${match}</span>`
  );
  return res;
}


watch(viewing, (newVal, _) => {
  commentInfo({
    shopId: parseInt(storeId_),
    pid: products.value[newVal].pid
  }).then(res => {
    comments.value = res.data.result;
    for ( let i = 0; i < comments.value.length; i++ ) {
      getUserById({
        userId: comments.value[i].userId
      }).then(res => {
        commenters.value.push(res.data.result)
      })
    }
  })
})

onMounted(async () => {
    fullScreenLoading()
    switch_page(false);
    getStoreById({
        storeId: parseInt(storeId_),
    }).then(res => {
	    storeContent.value = res.data.result;
	    store_img_url.value = storeContent.value.picUrl
    })
    productCountInfo({
        shopId:parseInt(storeId_)
    }).then(res => {
        productCount.value = res.data.result
    })
	getFavInfo({
		userId: parseInt(sessionStorage.getItem('userId')),
		type: 2
	}).then(res => {
		res.data.result.forEach(e => {
			favs.value.push(e.type == 1 ? e.sid : (e.type == 2 ? e.pid : e.uid))
		});
	})
})
</script>

<template>
  <Header/>
  <body>
  <el-container class="main-container">
    <el-aside width="25%" class="page-aside">
      <img style="width: 336px; height: 224px; margin: 30px; border: 2px solid rgb(255, 255, 255)"
           :src="store_img_url" alt=""/>
      <el-card class="box-card" style="box-shadow: 5px 5px 0 0 #b0b0b0; height: 300px">
        <div slot="header" class="clearfix">
          <div v-if="page_loaded">
            <h1 style="font-size: 24px; margin: 20px 0 0 0;">{{ storeContent.name }}</h1>
            <p v-if="storeContent.description == undefined" style="color: #999999">该店主很懒，还没有写介绍......</p>
            <el-scrollbar v-else style="height: 160px;">
              <p style="color: #999999">{{ storeContent.description }}</p>
            </el-scrollbar>
          </div>
          <h1 v-else>Loading...</h1>
          <router-link to="/allStore" v-slot="{navigate}">
            <el-button
                style="float: right; padding: 3px 0" type="text"
                @click="navigate"
            >
              返回商店列表
            </el-button>
          </router-link>
        </div>
      </el-card>
      <el-button
          v-if="op_permission"
          class="modal-btn"
          @click.prevent="createProduct"
      >新增商品</el-button>
    </el-aside>

    <el-main>
      <el-form class="modal-form">
        <div style="display: flex; flex-direction: row;">
          <input type="text" v-model="searchText" @input="search=false" placeholder="按照商品名称搜索..." required/>
          <svg width="40px" @click="switch_page(true)" color="white" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" data-v-ea893728=""><path fill="currentColor" d="m795.904 750.72 124.992 124.928a32 32 0 0 1-45.248 45.248L750.656 795.904a416 416 0 1 1 45.248-45.248zM480 832a352 352 0 1 0 0-704 352 352 0 0 0 0 704"></path></svg>
          <label style="margin: 5px 0 0 20px; font-weight: bold">价格区间：</label>
          <el-input-number
              size="large"
              v-model="filterPriceMin"
              placeholder="自定义最低价"
              :min="1"
              :max=filterPriceMax
              style="width: 115px; margin-left: 0"
              :controls=false
              @change="switch_page(false)"
          />
          <span style="font-weight: bold; font-size: 24px;">~</span>
          <el-input-number
              size="large"
              v-model="filterPriceMax"
              placeholder="自定义最高价"
              :min=filterPriceMin
              :max="65536"
              style="width: 115px; margin-left: 0"
              :controls=false
              @change="switch_page(false)"
          />

          <el-select
              v-model="sortMethod"
              placeholder="请选择排序方式"
              @change="switch_page(false)"
              style="width: 160px; margin-left: 50px;"
          >
            <el-option value="None" label="默认排序"/>
            <el-option value="UpwardPriceOrder" label="按价格升序排序"/>
            <el-option value="FallingPriceOrder" label="按价格降序排序"/>
            <el-option value="UpwardScoreOrder" label="按评价升序排序"/>
            <el-option value="FallingScoreOrder" label="按评价降序排序"/>
          </el-select>
          <el-select
              v-model="filterCategory"
              placeholder="请选择商品类型"
              @change="filterSubCategory='ALL';switch_page(false)"
              style="width: 160px;margin-left: 10px;"
          >
            <el-option value="ALL" label="全部商品"/>
            <el-option value="FOOD" label="食品"/>
            <el-option value="CLOTHES" label="服饰"/>
            <el-option value="FURNITURE" label="家具"/>
            <el-option value="ELECTRONICS" label="电子产品"/>
            <el-option value="ENTERTAINMENT" label="娱乐"/>
            <el-option value="SPORTS" label="体育产品"/>
            <el-option value="LUXURY" label="奢侈品"/>
          </el-select>

	        <el-select
		        v-model="filterSubCategory"
		        v-if="filterCategory == 'ELECTRONICS'"
		        placeholder="请选择商品二级类型"
		        @change="switch_page(false)"
		        style="width: 160px;margin-left: 10px;"
	        >
		        <el-option value="ALL" label="全部商品"/>
		        <el-option value="MOBILES" label="手机"/>
		        <el-option value="PCS" label="电脑"/>
		        <el-option value="CAMERAS" label="相机"/>
		        <el-option value="AUDIOS" label="音响"/>
		        <el-option value="OTHERS" label="其它"/>
	        </el-select>

	        <el-select
		        v-model="filterSubCategory"
		        v-if="filterCategory == 'FOOD'"
		        placeholder="请选择商品二级类型"
		        @change="switch_page(false)"
		        style="width: 160px;margin-left: 10px;"
	        >
		        <el-option value="ALL" label="全部商品"/>
		        <el-option value="FRUIT" label="水果"/>
		        <el-option value="VEGETABLE" label="蔬菜"/>
		        <el-option value="MEAT" label="肉类"/>
		        <el-option value="SEAFOOD" label="海鲜"/>
		        <el-option value="SNACKS" label="零食"/>
		        <el-option value="DRINK" label="饮品"/>
		        <el-option value="OTHERS" label="其它"/>
	        </el-select>

	        <el-select
		        v-model="filterSubCategory"
		        v-if="filterCategory == 'CLOTHES'"
		        placeholder="请选择商品二级类型"
		        @change="switch_page(false)"
		        style="width: 160px;margin-left: 10px;"
	        >
		        <el-option value="ALL" label="全部商品"/>
		        <el-option value="MALE" label="男士"/>
		        <el-option value="FEMALE" label="女士"/>
		        <el-option value="CHILD" label="童装"/>
		        <el-option value="OTHERS" label="其它"/>
	        </el-select>

          <el-select
              v-model="filterSubCategory"
              v-if="filterCategory == 'FURNITURE'"
              placeholder="请选择商品二级类型"
              @change="switch_page(false)"
              style="width: 160px;margin-left: 10px;"
          >
            <el-option value="ALL" label="全部商品"/>
            <el-option value="SOFA" label="沙发"/>
            <el-option value="BED" label="床"/>
            <el-option value="TABLE" label="桌子"/>
            <el-option value="CHAIR" label="椅子"/>
            <el-option value="WARDROBE" label="衣柜"/>
            <el-option value="DESK" label="书桌"/>
            <el-option value="BOOKSHELF" label="书架"/>
            <el-option value="MIRROR" label="镜子"/>
            <el-option value="LAMP" label="灯"/>
            <el-option value="SIDEBOARD" label="餐具柜"/>
            <el-option value="TOILET" label="马桶"/>
            <el-option value="OTHERS" label="其它"/>
          </el-select>

          <el-select
              v-model="filterSubCategory"
              v-if="filterCategory == 'ENTERTAINMENT'"
              placeholder="请选择商品二级类型"
              @change="switch_page(false)"
              style="width: 160px;margin-left: 10px;"
          >
            <el-option value="ALL" label="全部商品"/>
            <el-option value="MAHJONG" label="麻将"/>
            <el-option value="POKER" label="扑克"/>
            <el-option value="TOY" label="玩具"/>
            <el-option value="CINEMA_TICKET" label="电影票"/>
            <el-option value="CONCERT_TICKET" label="演唱会票"/>
            <el-option value="OTHERS" label="其它"/>
          </el-select>

          <el-select
              v-model="filterSubCategory"
              v-if="filterCategory == 'SPORTS'"
              placeholder="请选择商品二级类型"
              @change="switch_page(false)"
              style="width: 160px;margin-left: 10px;"
          >
            <el-option value="ALL" label="全部商品"/>
            <el-option value="MOUNTAIN_BIKE" label="山地车"/>
            <el-option value="BASKETBALL" label="篮球"/>
            <el-option value="FOOTBALL" label="足球"/>
            <el-option value="BATTLEDORE" label="羽毛球拍"/>
            <el-option value="PING_PONG" label="乒乓球"/>
            <el-option value="TREADMILL" label="跑步机"/>
            <el-option value="ALPENSTOCK" label="登山杖"/>
            <el-option value="OTHERS" label="其它"/>
          </el-select>

          <el-select
              v-model="filterSubCategory"
              v-if="filterCategory == 'LUXURY'"
              placeholder="请选择商品二级类型"
              @change="switch_page(false)"
              style="width: 160px;margin-left: 10px;"
          >
            <el-option value="ALL" label="全部商品"/>
            <el-option value="GUCCI" label="古驰"/>
            <el-option value="HERMES" label="爱马仕"/>
            <el-option value="CHANEL" label="香奈儿"/>
            <el-option value="LOUIS_VUITTON" label="路易威登"/>
            <el-option value="DIOR" label="迪奥"/>
            <el-option value="PRADA" label="普拉达"/>
            <el-option value="BALENCIAGA" label="巴黎世家"/>
            <el-option value="OTHERS" label="其它"/>
          </el-select>

        </div>
      </el-form>
      <el-scrollbar style="margin: 10px 0 10px 0" max-height="700px" height="700px">
        <div
            class="empty"
            v-if="page_loaded && !products.length"
            style="display: flex; flex-direction: column; text-align: center"
        >
          <el-empty description=" " style="margin-top: 120px"/>
          <h1>当前商店尚未上架任何商品 :(</h1>
          <router-link to="/home" v-slot="{navigate}">
            <el-button class="modal-btn-side" @click="navigate">返回主页</el-button>
          </router-link>
        </div>
        <el-row>
          <el-col
              v-if="page_loaded"
              v-for="i in Math.min(12, products.length)"
              :span="6"
              :key="i"
              :offset="0.5"
          >
            <el-card
                class="product-card"
                style="margin: 20px; height: 520px"
                v-if="page_loaded">
              <img v-if="products[i - 1].picUrl.length != 0" :src="products[i - 1].picUrl[0]" class="image" alt="">
              <img v-if="products[i - 1].picUrl.length == 0" src="../../assets/img/background/default1.jpg" class="image" alt="">

              <div style="padding: 14px;">
                <p style="font-size: 20px" v-html="highlightKeyword(products[i - 1].name.length > 15 ?
												products[i - 1].name.substring(0, 15) + '...' : products[i - 1].name,searchText)"
                v-if="search">
                </p>
                <p style="font-size: 20px" v-else>
                  {{
                    products[i - 1].name.length > 15 ?
                        products[i - 1].name.substring(0, 15) + "..." :
                        products[i - 1].name
                  }}
                </p>
                <p style="white-space: normal">{{
                    products[i - 1].description.length > 30 ?
                        products[i - 1].description.substring(0, 30) + "......" :
                        products[i - 1].description
                  }}</p>
                <div style="text-align: center"><p style="font-size: 20px; color: #ef7900">￥ {{ products[i - 1].price }}</p></div>
                <div class="bottom clearfix">
                  <time v-if="products[i - 1].subtype === 'OMITTED'" class="time">
	                  {{ parseProductCategory(products[i - 1].type) }}
                  </time>
	                <time v-else class="time">
		                {{ parseProductCategory(products[i - 1].type) }} · {{ parseProductSubCategory(products[i - 1].subtype) }}
	                </time>
                  <el-button
                      type="text"
                      class="button"
                      @click="handleDetail(i - 1)">查看详情</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-scrollbar>

      <div style="display: flex; flex-direction: column; align-items: center;">
        <el-pagination
            background
            layout="prev, pager, next"
            v-model:current-page="current_page"
            @current-change="switch_page(false)"
            :disabled="products.length == 0"
            :default-page-size=12
            :total=productCount
        />
      </div>

      <ViewProduct
          v-if="page_loaded && products.length > 0 && viewing > -1"
          v-model="drawer"
          title="I am the title"
          :id="products[viewing].pid"
          :name="products[viewing].name"
          :description="products[viewing].description"
          :img="products[viewing].picUrl"
          :in_stock="products[viewing].reserve"
          :category="products[viewing].type"
          :sub_category="products[viewing].subtype"
          :shop_id="products[viewing].shopId"
          :comments="comments"
          :commenters="commenters"
          :score="products[viewing].score"
          :price="products[viewing].price"
          :op_permission="op_permission"
          :favs="favs"
      />
      <CreateProduct
          v-if="page_loaded"
          v-model="creating_product"
          :store-id_="storeId_"
      />
    </el-main>
  </el-container>
  </body>
</template>


<style scoped>
body {
  background-image: linear-gradient(to top left, #a0fdfd 0%, #409eff 100%);
}

.main-container {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 400;
  color: #333;
}

.page-aside {
  display: flex;
  align-content: center;
  align-items: center;
  flex-direction: column;
  border-right: white dotted 12px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  width: 320px;
  height: 240px;
  background: #f1f1f1;
}

.time {
  font-size: 13px;
  color: #999;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
}

.button {
  padding: 0;
  float: right;
}

.image {
  height: 220px;
  width: 220px;
  display: block;
}

.modal-form {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.modal-form input el-input-number {
  font-size: 16px;
  width: 360px;
  height: 10px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 0.5rem;
  margin-left: 10px;
}

.modal-btn {
  display: inline-block;
  font-size: 20px;
  font-weight: 1000;
  transition: all 0.3s;
  width: 160px;
  height: 50px;
  margin: 20px;
}

.product-card {
  border: 2px solid rgb(196, 196, 196);
  transition: transform 100ms ease-in-out;
}
.product-card:hover {
  transform: scale(1.1);
}

p >>>.highlight {
  color: #FF7F50;
  font-weight: bold;
}

.pagination {
  text-align: center;
  align-items: center;
  align-content: center;
}


</style>