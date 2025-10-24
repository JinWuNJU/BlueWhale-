<script lang="ts">
import {parseProductCategory, parseProductSubCategory} from "../../../utils";
import {STORE_MODULE} from "../../../api/_prefix.ts";
import {productModifyStock} from "../../../api/product.ts";
import {orderCreate} from "../../../api/order.ts";
import PayOrder from "../../../components/PayOrder.vue";
import comments from "../../../components/Comments.vue";
import {getOrderById} from "../../../api/resolve_id.ts";
import {addFavInfo, unfavInfo} from "../../../api/fav.ts";

export default {
	components: {PayOrder},
	mounted() {
		this.orderDialogVisible = false;
	},
	data() {
		return {
			number: 1,
            order_id: 0,
			current_comment_page: 1,
			centerDialogVisible: false,
			orderDialogVisible: false,
			deliveryMode: "DELIVERY",
			payOpen: false,
	        current_order: {},
	        current_create_time: "",
	        userName: sessionStorage.name,
			userId: sessionStorage.userId,
		}
	},
	methods: {
		parseProductSubCategory,
		parseProductCategory,
		switchOrderInfo() {
			this.orderDialogVisible = !this.orderDialogVisible
		},
		handleDelete() {
			// TODO - Deletion
			ElMessage.error(`抱歉，目前无法删除商品！`);
		},
		handleChange() {
			console.log(`${STORE_MODULE}/${this.shop_id}`)
			productModifyStock({
				shopId: this.shop_id,
				pId: this.id,
				reserve_num: this.number,
			}).then(() => {
				ElMessage.success(`修改商品信息成功！`);
				location.reload()
			})
		},
		switch_fav(pid) {
			if ( this.favs.includes(pid) ) {
				unfavInfo({
					userId: parseInt(this.userId),
					pid: pid,
					type: 2,
				}).then(() => {
					ElMessage.success("取消收藏成功！");
					location.reload();
				})
			}
			else {
				addFavInfo({
					userId: parseInt(this.userId),
					pid: pid,
					type: 2,
				}).then(() => {
					ElMessage.success("收藏成功！");
					location.reload();
				})
			}
		},
		createOrder() {
			orderCreate({
				type: "UNPAID",
				shopId: this.shop_id,
				pid: this.id,
				uid: parseInt(sessionStorage.userId),
				otherInfo: "No other information provided.",
				deliveryType: this.deliveryMode,
                num: this.number
			}).then(res => {
				if (res.data.code === '000' || res.data.code === '200') {
					ElMessage({
						message: "订单创建成功！",
						type: 'success',
						center: true,
					})
                    this.order_id = res.data.result
					this.completeOrder();
				} else if (res.data.code === '400') {
					ElMessage({
						message: res.data.msg,
						type: 'error',
						center: true,
					})
				}
			})
		},
		completeOrder() {
			ElMessageBox.confirm(
				'订单已完成创建！您可以选择立即支付或稍后支付。（未支付的订单将在24h后自动撤销）',
				'订单已创建',
				{
					confirmButtonText: '立即支付',
					cancelButtonText: '稍后支付',
					type: 'warning',
				}
			).then(() => {
				this.payOpen = true;
		        getOrderById({
		            orderId: this.order_id
		        }).then(res => {
			        this.current_order = res.data.result
			        this.current_create_time = this.current_order.createTime.substring(0,10) + ' ' + this.current_order.createTime.substring(11,19)
		        })
			}).catch(() =>
				location.reload()
			)
		}
	},
	props: [
		"id",
		"idx",
		"name",
		"description",
		"in_stock",
		"img",
		"category",
		"sub_category",
		"shop_id",
		"op_permission",
		"comments",
		"commenters",
		"price",
        "score",
		"favs",
	],
}
</script>

<template>
  <el-drawer
      :with-header="false"
      class="drawer"
      size="45%"
      style="padding: 10px"
  >
		<el-main class="main">
			<h1 style="font-size: 32px; margin: 10px 0 0 0">{{ this.name }}</h1>
			<p>{{ this.description }}</p>
			<p v-if="sub_category == 'OMITTED'" style="color: #9a9a9a">
				商品类型：{{ parseProductCategory(this.category) }}
			</p>
			<p v-else style="color: #9a9a9a">
				商品类型：{{ parseProductCategory(this.category) }} · {{ parseProductSubCategory(this.sub_category) }}
			</p>

			<el-carousel :interval="4000" type="card" height="200px">
				<el-carousel-item v-if="img.length == 0">
					<h3>当前产品暂无图片！</h3>
				</el-carousel-item>
				<el-carousel-item v-if="img.length != 0" v-for="i in img">
					<img :src=i style="width: 100%; height: 100%; object-fit: cover;" alt="">
				</el-carousel-item>
			</el-carousel>

			<div style="display: flex; flex-direction: column; align-items: center">
				<div style="background-color: #ffffff; margin: 10px; text-align: center; width: 200px">
					<span style="font-size: 32px">￥ {{ price }} </span>
				</div>
			</div>

			<div v-if="comments.length == 0" style="display: flex; flex-direction: column; align-items: center; margin: 50px;">
				<el-card style="margin-top: 10px; width: 300px; height: 60px">
					<h4 style="margin: 0">当前商品评论区空空如也 :(</h4>
				</el-card>
			</div>

			<p v-if="comments.length > 0">评分（共 {{ comments.length }} 次评论）</p>
			<el-rate
				v-if="comments.length > 0"
				disabled
				show-score
				size="large"
				:model-value=score.toFixed(1)
				text-color="#ff9900"
				score-template="{value}星"
			/>
			<p v-if="comments.length > 0">评论区 ({{ comments.length }})</p><p></p>
			<el-scrollbar v-if="comments.length > 0" style="margin: 15px 0 15px 0; height: 400px">
				<el-timeline style="max-width: 600px">
					<el-timeline-item v-for="i in Math.min(6, comments.length - (current_comment_page - 1) * 6)" timestamp="2024-4-14" placement="top">
						<el-card>
							<div v-if="commenters[i - 1]!=undefined" style="display: flex; flex-direction: row; ">
								<el-avatar :src="commenters[i - 1].avatarUrl" style="margin: 10px 5px 0 0"/>
								<h2>{{ commenters[i - 1].name }}</h2>
							</div>
							<el-rate
								disabled
								show-score
								size="small"
								:model-value=comments[i-1].score
								text-color="#ff9900"
							/>
							<p>{{ comments[i - 1].comment }}</p>
						</el-card>
					</el-timeline-item>
				</el-timeline>
			</el-scrollbar>
			<div style="display: flex; flex-direction: column; align-items: center;" v-if="comments.length">
				<el-pagination
					background
					layout="prev, pager, next"
					v-model:current-page="current_comment_page"
					:default-page-size=6
					:total=comments.length
				/>
			</div>

			<div class="buy" v-if="!op_permission">
				<p v-if="in_stock == 0" style="margin-top: 20px; color: orange">当前商品缺货！</p>
				<p v-if="in_stock > 0" style="margin-top: 20px">当前库存数量：{{ in_stock }}</p>
				<div v-if="!orderDialogVisible">
					<div class="btn" style="margin-top: 10px">
						<el-button
							class="modal-btn"
							type="info"
							v-if="!this.favs.includes(id)"
							@click.prevent="switch_fav(id)"
						>收藏</el-button>
						<el-button
							class="modal-btn"
							type="warning"
							v-if="this.favs.includes(id)"
							@click.prevent="switch_fav(id)"
						>取消收藏</el-button>
						<el-button
							v-if="in_stock > 0"
							class="modal-btn"
							type="primary"
							@click.prevent="switchOrderInfo"
						>购买
						</el-button>
					</div>
				</div>
				<div v-else style="margin-top: 25px">
					<span v-if="in_stock > 0">请选择购买数量：</span>
					<el-input-number
						v-if="in_stock > 0"
						v-model="number"
						:min="1"
						:max="in_stock"
					/><br><br>

					<span v-if="in_stock > 0">请选择提货方式：</span>
					<el-select
			           v-model="deliveryMode"
			           placeholder="请选择"
			           style="width: 150px;"
					>
						<el-option value="DELIVERY" label="快递送达"/>
						<el-option value="PICKUP" label="到店自提"/>
					</el-select><br><br>

					<div class="btn">
						<el-button class="modal-btn" @click="switchOrderInfo">收起</el-button>
						<el-button class="modal-btn" type="warning" @click="">加入购物车</el-button>
						<el-button class="modal-btn" type="primary" @click="createOrder">创建订单</el-button>
					</div>
				</div>

			</div>
			<div class="manage" v-if="op_permission">
				<p style="margin-top: 20px">当前库存数量：{{in_stock}}</p>
				<span>修改库存数量：</span>
				<el-input-number
					v-model="number"
					:min="0"
					:max="256"
				/>
			</div>
			<div class="btn">
				<el-button
					class="modal-btn"
					v-if="op_permission"
					@click.prevent="handleChange"
				>保存修改
				</el-button>
				<el-button
					class="modal-btn"
					v-if="op_permission"
					type="danger"
					@click.prevent="centerDialogVisible = true"
				>删除商品
				</el-button>
			</div>
			<el-dialog
				v-model="centerDialogVisible"
				title="删除商品"
				width="500"
				align-center
			>
				<span style="font-size: 18px">确认删除该商品吗？（不可撤销）</span>
				<template #footer>
					<div class="dialog-footer">
						<el-button @click="centerDialogVisible = false">取消</el-button>
						<el-button type="primary" @click="handleDelete">确认</el-button>
					</div>
				</template>
			</el-dialog>
		</el-main>
		<PayOrder
        v-model="payOpen"
        v-if = "current_order"
        :shop_id="shop_id"
        :product_id="id"
        :order_id="order_id"
        :name="name"
        :price="price"
        :count="number"
        :create_time="current_create_time"
        :delivery_type="deliveryMode"
    />

  </el-drawer>
</template>

<style scoped>

.drawer {
	margin: 0;
	padding: 0;
}

.main {
	padding-bottom: 150px;
	background-color: #eeeeee;
}

.el-carousel__item h3 {
	color: #475669;
	opacity: 0.75;
	line-height: 200px;
	margin: 0;
	text-align: center;
}

.el-carousel__item:nth-child(2n) {
	background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n + 1) {
	background-color: #d3dce6;
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