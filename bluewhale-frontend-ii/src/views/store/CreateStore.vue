<!--Lab2新增-创建商店界面-->
<!--你可以选择把创建商店改成一个弹窗或其他界面的一个部分。
这样的话，就不需要有这样一个views下的创建商店文件了，因为views下的文件一般会是一个单独的界面。
但如果你觉得这一个部分的代码较多，合到其他文件里会使那个文件太长，
可以在store文件夹下创建一个components子文件夹，里面存放store模块下产生的组件界面。把这个创建商店子组件放在里面
这个传递数据的过程可能需要用到props-->

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {uploadImage} from '../../api/tools'
import Header from "../../components/Header.vue"
import axios from "axios";
import {fullScreenLoading, fullScreenLoadingShort} from "../../utils/visuals.ts";
import {storeCreate} from "../../api/store.ts";

const title = ref("")
const descriptions = ref("")
const other_infos = ref("")

const hasTitleInput = computed(() => title.value != '')
const hasDescInput = computed(() => descriptions.value != '')
const hasImgInput = computed(() => imageFileList.value != '')
const allowSubmit = computed(() => {
	return (hasTitleInput.value && hasDescInput.value && hasImgInput.value && !uploadingImgs.value)
})

//这里为大家提供上传且仅能上传1张图片的代码实现。
const imageFileList = ref([])
const logoUrl = ref('')
const uploadingImgs = ref(false);

function handleChange(file: any, fileList: any) {
	uploadingImgs.value = true;
	imageFileList.value = fileList
	let formData = new FormData()
	formData.append('file', file.raw)
	uploadImage(formData).then(res => {
		logoUrl.value = res.data.result
		uploadingImgs.value = false;
	})
}

function handleExceed() {
	ElMessage.warning(`当前限制选择 1 个文件`);
}

function uploadHttpRequest() {
	return new XMLHttpRequest()
}

function handleSubmit() {
	storeCreate({
		name: title.value,
		picUrl: logoUrl.value,
		description: descriptions.value,
		otherInfo: other_infos.value,
	}).then(() => {
		ElMessage.success(`创建商店成功！`);
		location.reload();
	})
}

onMounted(() => {
	fullScreenLoading();
})
</script>


<template>
	<Header/>
  <el-main class="main">
	<h1 style="font-size: 88px">创建新的商店...</h1>
    <div class="main-form">
	    <el-form class="modal-form">
		    <label>商店名称</label>
		    <input type="text" v-model="title" placeholder="给你的商店起个名字" required/>
		    <label>商店描述</label>
		    <input type="text" v-model="descriptions" placeholder="简单介绍一下你的商店吧" required/>
		    <label>其他信息</label>
		    <input type="text" v-model="other_infos" placeholder="请输入其他信息（可选）" />
	    </el-form>
	    <p></p>
        <el-form class="modal-form">
	      <label>商店Logo</label>
        <el-upload
            v-model:file-list="imageFileList"
            :limit="1"
            :on-change="handleChange"
            :on-exceed="handleExceed"
            :on-remove="handleChange"
            class="upload-demo"
            list-type="picture"
            :http-request="uploadHttpRequest"
            drag>
          <el-icon class="el-icon--upload">
	          <p>⭐</p>
            <upload-filled/>
          </el-icon>
          <div class="el-upload__text">
            将文件拖到此处或单击此处上传。
          </div>
        </el-upload>
	        <p></p>
      </el-form>
	    <label v-if="uploadingImgs">图片正在上传，请稍候...</label>
	    <div class="btn">
		    <el-button :disabled=!allowSubmit class="modal-btn" @click.prevent="handleSubmit" type="primary">创建</el-button>
		    <router-link to="/home">
			    <el-button class="modal-btn">返回主页</el-button>
		    </router-link>
	    </div>

    </div>

  </el-main>
</template>


<style scoped>

.main-form {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
}
.main {
	display: flex;
	align-items: center;
	justify-content: center;
	//flex-direction: column;
	font-family: 'Nunito', sans-serif;
	font-weight: 400;
	height: 100vh;
	color: #333;
	//background-image: linear-gradient(to top left, #9bfde6 0%, #409eff 100%);
	background-image: url("../../assets/img/background/shopping2.jpg");
	background-size: cover;
}
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
	font-size: 1.25rem;
	font-weight: 750;
}

.modal-form input {
	font-size: 1rem;
	width: 500px;
	padding: 1rem 1.5rem;
	border: 1px solid #ddd;
	border-radius: 0.5rem;
	margin: 10px;
}

.btn {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
}

.modal-btn {
	display: inline-block;
	font-size: 1.6rem;
	font-weight: 1000;
	transition: all 0.3s;
	width: 200px;
	height: 50px;
	margin: 10px;
}

h1 {
	color: white;
}
label {
	color: white;
}

</style>
