<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {userInfo, userInfoUpdate} from '../../api/user.ts'
import {parseRole, parseTime} from "../../utils"
import {router} from '../../router'
import {UserFilled} from "@element-plus/icons-vue"
import Header from "../../components/Header.vue";
import {uploadImage} from "../../api/tools.ts";
import {md5} from 'js-md5';

const role = sessionStorage.getItem("role")

const tel = ref('')
const name = ref('')
const avatar = ref('')
const address = ref('')
const regTime = ref()
const newName = ref('')
const password = ref('')
const storeName = ref('')
const confirmPassword = ref('')
const displayInfoCard = ref(false)

const imageFileList = ref([])
const logoUrl = ref('')
const uploadingImgs = ref(false);

const hasConfirmPasswordInput = computed(() => confirmPassword.value != '')
const isPasswordIdentical = computed(() => password.value == confirmPassword.value)
const changeDisabled = computed(() => {
	return !(hasConfirmPasswordInput.value && isPasswordIdentical.value)
})

onMounted(() => {
	getUserInfo()
})

const handleExceed = () => {
    ElMessage.warning(`当前限制选择 1 个文件`);
}

const handleChange = (file: any, fileList: any) => {
    uploadingImgs.value = true;
    imageFileList.value = fileList
    let formData = new FormData()
    formData.append('file', file.raw)
    uploadImage(formData).then(res => {
	    logoUrl.value = res.data.result
	    uploadingImgs.value = false;
    })
}
const uploadHttpRequest = () => {
	return new XMLHttpRequest()
}

const getUserInfo = () => {
	userInfo().then(res => {
		name.value = res.data.result.name
		tel.value = res.data.result.phone
		storeName.value = res.data.result.shopName
		address.value = res.data.result.address
		regTime.value = parseTime(res.data.result.createTime)
		newName.value = name.value
        avatar.value = res.data.result.avatarUrl
    })
}

const updateInfo = () => {
	userInfoUpdate({
		name: newName.value,
		password: undefined,
		address: address.value,
        avatarUrl: logoUrl.value == '' ? avatar.value : logoUrl.value,
	}).then(res => {
	    if (res.data.code === '000' || res.data.code === '200') {
		    ElMessage({
				customClass: 'customMessage',
				type: 'success',
				message: '更新成功！',
			})
		    location.reload();
		} else if (res.data.code === '400') {
			ElMessage({
				customClass: 'customMessage',
				type: 'error',
				message: res.data.msg,
			})
		}
	})
}

const updatePassword = () => {
	const hashedPassword = md5.create().update(password.value);
	userInfoUpdate({
		name: undefined,
		password: hashedPassword.hex(),
		address: undefined
	}).then(res => {
		if ( res.data.code === '000' || res.data.code === '200' ) {
			password.value = ''
			confirmPassword.value = ''
			ElMessageBox.alert(
				`请重新登录`,
				'修改成功',
				{
					customClass: "customDialog",
					confirmButtonText: '跳转到登录',
					type: "success",
					showClose: false,
					roundButton: true,
					center: true
				}).then(() => router.push({path: "/login"}))
		}
		else if (res.data.code === '400') {
			ElMessage({
				customClass: 'customMessage',
				type: 'error',
				message: res.data.msg,
			})
			password.value = ''
			confirmPassword.value = ''
		}
	})
}
</script>


<template>
	<Header/>
	<el-main class="main-container">
		<el-card class="aside-card">
			<div class="avatar-area">
                <el-avatar class="avatar-img" :src="avatar" :size="100" @click=""/>
				<span class="avatar-text">{{ name }}</span>
			</div>

			<el-divider/>

			<el-descriptions
				:column="1"
				border
				title="个人信息"
			>
				<template #extra>
					<router-link to="/home" v-slot="{navigate}">
						<el-button class="modal-btn" type="primary" @click="navigate">返回主页</el-button>
					</router-link>
				</template>

				<el-descriptions-item label="身份">
					<el-tag>{{ parseRole(role) }}</el-tag>
				</el-descriptions-item>

				<el-descriptions-item label="所属商店" v-if="role === 'STAFF'">
					{{ storeName }}
				</el-descriptions-item>

				<el-descriptions-item label="联系电话">
					{{ tel }}
				</el-descriptions-item>

				<el-descriptions-item label="地址" v-if="role === 'CUSTOMER' || role === 'STAFF'">
					{{ address }}
				</el-descriptions-item>

				<el-descriptions-item label="注册时间">
					{{ regTime }}
				</el-descriptions-item>
			</el-descriptions>
		</el-card>

		<el-card v-if="displayInfoCard" class="change-card">
			<template #header>
				<div class="card-header">
					<span>个人信息</span>
					<div>
						<el-button @click="displayInfoCard = !displayInfoCard;">修改密码</el-button>
						<el-button type="primary" @click="updateInfo" :disabled="uploadingImgs">确认修改</el-button>
					</div>
				</div>
			</template>

			<el-form>
		        <label for="name">头像</label>
		        <el-upload
		            v-model:file-list="imageFileList"
		            style="width: 300px"
		            :limit="1"
		            :on-change="handleChange"
		            :on-exceed="handleExceed"
		            :on-remove="handleChange"
		            class="upload-demo"
		            list-type="picture"
		            :http-request="uploadHttpRequest"
		            drag
		        >
		            <el-icon class="el-icon--upload"><p>⚽</p>
		                <upload-filled/>
		            </el-icon>
		            <div class="el-upload__text">
		                将文件拖到此处或单击此处上传。
		            </div>
		        </el-upload>
				<label v-if="uploadingImgs" style="color: #ff6d12">图片正在上传，请稍候...</label>

                <el-form-item>
					<label for="name">昵称</label>
					<el-input type="text" id="name" v-model="newName"/>
				</el-form-item>

				<el-form-item>
					<label for="phone">手机号</label>
					<el-input id="phone" v-model="tel" disabled/>
				</el-form-item>

				<el-form-item v-if="role === 'CUSTOMER' || role === 'STAFF'">
					<label for="address">收货地址</label>
					<el-input
						id="address"
						type="textarea"
						rows="4"
						v-model="address"
						placeholder="中华门">
					</el-input>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card v-if="!displayInfoCard" class="change-card">
			<template #header>
				<div class="card-header">
					<span>修改密码</span>
					<div>
						<el-button @click="displayInfoCard = !displayInfoCard;">修改个人信息</el-button>
						<el-button type="primary" @click="updatePassword" :disabled="changeDisabled">确认修改</el-button>
					</div>
				</div>
			</template>

			<el-form>
				<el-form-item>
					<label for="password">密码</label>
					<el-input type="password" id="password" v-model="password" placeholder="•••••••••" required/>
				</el-form-item>
				<el-form-item>
					<label v-if="!hasConfirmPasswordInput" for="confirm_password">确认密码</label>
					<label v-else-if="!isPasswordIdentical" for="confirm_password" class="error-warn">密码不一致</label>
					<label v-else for="confirm_password">确认密码</label>

					<el-input type="password" id="confirm_password" v-model="confirmPassword"
					          :class="{'error-warn-input' :(hasConfirmPasswordInput && !isPasswordIdentical)}"
					          placeholder="•••••••••" required/>
				</el-form-item>
			</el-form>

		</el-card>
	</el-main>
</template>


<style scoped>

.main-container {
	background-image: linear-gradient(to top left, #90edfa 0%, #409eff 100%);
	display: flex;
	flex-direction: row;
	padding: 15px;
	gap: 5px;
	justify-content: center;
}

.change-card {
	width: 66%;
}

.error-warn {
	color: red;
}

.error-warn-input {
	--el-input-focus-border-color: red;
}

.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.avatar-area {
	display: flex;
	justify-content: space-around;
	align-items: center;
	gap: 30px;
}

.avatar-text {
	font-size: 30px;
	font-weight: bolder;
	padding-right: 40px;
}

.avatar-img {
	//border: 2px solid rgb(0, 0, 0);
	transition: transform 100ms ease-in-out;
}
.avatar-img:hover {
	transform: scale(1.14);
}

</style>
