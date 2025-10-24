<script lang="ts">
import {uploadImage} from "../../../api/tools.ts";
import {productCreate} from "../../../api/product.ts";

export default {
	data() {
		return {
			number: 1,
			name: "",
			textarea: "",
			category: "",
			subCategory: "",
			imageFileList: [],
			logoUrls: [],
			price: 1,
			uploadingImgs: false,
		}
	},
	props: ["storeId_"],
	methods: {
		handleCancel() {
			this.name = ""
			this.number = 1
			this.textarea = ""
			this.imageFileList = []
			ElMessage.info(`已清空商品信息！`);
		},
		handleChangePic(file: any, fileList: any) {
			this.uploadingImgs = true
			this.imageFileList = fileList
			let formData = new FormData()
			formData.append('file', file.raw)
			uploadImage(formData).then(res => {
				this.logoUrls.push(res.data.result);
				this.uploadingImgs = false
			})
		},
		handleExceed() {
			ElMessage.warning(`当前限制选择 5 个文件`);
		},
		handleSubmit() {
			productCreate({
				shopId: parseInt(this.storeId_),
				reserve: this.number,
				name: this.name,
				type: this.category,
				subtype: this.subCategory,
				description: this.textarea,
				picUrl: this.logoUrls,
				price: this.price
			}).then(() => {
					ElMessage.success(`创建商品成功！`);
					location.reload();
				})
		},
		uploadHttpRequest() {
			return new XMLHttpRequest()
		}

	},
}
</script>

<template>
	<el-drawer
		size="40%"
		title="I am the title"
		:with-header="false"
		class="drawer">
		<h1 style="font-size: 32px; margin: 10px 0 0 0">添加新的商品</h1>
		<el-form class="modal-form">
			<label>商品名称</label>
			<el-input
				v-model="name"
				style="width: 450px"
				type="text"
				placeholder="请输入商品名称"
				required
			/>
			<label>商品描述</label>
			<el-input
				v-model="textarea"
				style="width: 450px"
				type="textarea"
				:rows="8"
				placeholder="描述一下你的商品"
				required
			/>
			<div style="display: flex; flex-direction: row; align-content: center; margin-top: 10px">
				<div>
					<label>商品类别</label><br>
					<el-select
						v-model="category"
						placeholder="请选择"
						style="width: 200px"
						@change="subCategory=''"
					>
						<el-option value="FOOD" label="食品"/>
						<el-option value="CLOTHES" label="服饰"/>
						<el-option value="FURNITURE" label="家具"/>
						<el-option value="ELECTRONICS" label="电子产品"/>
						<el-option value="ENTERTAINMENT" label="娱乐"/>
						<el-option value="SPORTS" label="体育产品"/>
						<el-option value="LUXURY" label="奢侈品"/>
					</el-select>
				</div>
				<div style="margin-left: 50px">
					<label v-if="category">
						商品二级类别
					</label><br>
					<el-select
						v-if="category === 'FOOD'"
						v-model="subCategory"
						placeholder="请选择"
						style="width: 200px"
					>
						<el-option value="FRUIT" label="水果"/>
						<el-option value="VEGETABLE" label="蔬菜"/>
						<el-option value="MEAT" label="肉类"/>
						<el-option value="SEAFOOD" label="海鲜"/>
						<el-option value="SNACKS" label="零食"/>
						<el-option value="DRINK" label="饮品"/>
						<el-option value="OTHERS" label="其它"/>
					</el-select>
					<el-select
						v-if="category === 'CLOTHES'"
						v-model="subCategory"
						placeholder="请选择"
						style="width: 200px"
					>
						<el-option value="MALE" label="男士"/>
						<el-option value="FEMALE" label="女士"/>
						<el-option value="CHILD" label="童装"/>
						<el-option value="OTHERS" label="其它"/>
					</el-select>
					<el-select
						v-if="category === 'ELECTRONICS'"
						v-model="subCategory"
						placeholder="请选择"
						style="width: 200px"
					>
						<el-option value="MOBILES" label="手机"/>
						<el-option value="PCS" label="电脑"/>
						<el-option value="CAMERAS" label="相机"/>
						<el-option value="AUDIOS" label="音响"/>
						<el-option value="OTHERS" label="其它"/>
					</el-select>
          <el-select
              v-if="category === 'FURNITURE'"
              v-model="subCategory"
              placeholder="请选择"
              style="width: 200px"
          >
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
              v-if="category === 'ENTERTAINMENT'"
              v-model="subCategory"
              placeholder="请选择"
              style="width: 200px"
          >
            <el-option value="MAHJONG" label="麻将"/>
            <el-option value="POKER" label="扑克"/>
            <el-option value="TOY" label="玩具"/>
            <el-option value="CINEMA_TICKET" label="电影票"/>
            <el-option value="CONCERT_TICKET" label="演唱会票"/>
            <el-option value="OTHERS" label="其它"/>
          </el-select>

          <el-select
              v-if="category === 'SPORTS'"
              v-model="subCategory"
              placeholder="请选择"
              style="width: 200px"
          >
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
              v-if="category === 'LUXURY'"
              v-model="subCategory"
              placeholder="请选择"
              style="width: 200px"
          >
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
			</div>

			<label>商品定价</label>
			<el-input-number
				style="margin: 0px;"
				v-model="price"
				:min="0"
				:max="65536"
			/>
			<label>初始库存（范围为0到256之间）</label>
			<el-input-number
				style="margin-top: 10px;"
				v-model="number"
				:min="0"
				:max="256"
			/>
		</el-form>
		<el-form class="modal-form">
			<label>商店图片</label>
			<el-upload
				v-model:file-list="this.imageFileList"
				:limit="5"
				:on-change="handleChangePic"
				:on-exceed="handleExceed"
				:on-remove="handleChangePic"
				class="upload-demo"
				list-type="picture"
				:http-request="uploadHttpRequest"
				drag>
				<el-icon class="el-icon--upload">
					<p>⭐</p>
				</el-icon>
				<div class="el-upload__text">
					将文件拖到此处或单击此处上传。
				</div>
				<label v-if="uploadingImgs">图片正在上传，请稍候...</label>
			</el-upload>
			<p></p>
			<div class="btn">
				<el-button
					class="modal-btn"
					@click.prevent="handleSubmit"
					:disabled="name=='' || textarea=='' || imageFileList==[] || category=='' || uploadingImgs"
					type="primary"
				>创建</el-button>
				<el-button
					class="modal-btn"
					@click.prevent="handleCancel"
					type="danger"
				>清空</el-button>
			</div>
		</el-form>
	</el-drawer>
</template>

<style scoped>
.modal-form {
	margin: 0 3rem;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.modal-form el-upload {
	width: 1000px;
}

.modal-form label {
	font-size: 16px;
	font-weight: 750;
	margin-top: 20px;
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
}
</style>