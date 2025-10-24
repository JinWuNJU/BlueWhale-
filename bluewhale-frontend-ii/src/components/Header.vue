<script setup lang="ts">
import {router} from '../router'
import {parseRole} from "../utils"
import {User, Star, Message, House, SwitchButton} from "@element-plus/icons-vue"
import {ref} from "vue";   //图标

const role = sessionStorage.getItem('role')    //登录的时候插入的

const currentTime = ref(new Date().toLocaleString());
setInterval(getCurrentTime, 1000)

function getCurrentTime() {
	currentTime.value = new Date().toLocaleString();
}

function logout() {
    ElMessageBox.confirm(
        '是否要退出登录？',
        '提示',
        {
	        customClass: "customDialog",
	        confirmButtonText: '是',
	        cancelButtonText: '否',
	        type: "warning",
	        showClose: false,
	        roundButton: true,
	        center: true
        }
    ).then(() => {
	    sessionStorage.setItem('token', '');
	    router.push({path: "/entrance"});
	    location.reload();
    })
}
</script>


<template>
    <el-header class="custom-header" height="20">
	    <el-row :gutter="10">
		    <img
			    style="width: 150px"
			    src="../assets/img/logo.png"
			    class="nav__logo"
			    alt=""
		    />

	        <el-col :span="2.75" class="header-icon">
		        <router-link to="/home" v-slot="{navigate}" class="no-link">
		            <h1 @click="navigate" class="header-text"> 蓝鲸在线购物 </h1>
		        </router-link>
	        </el-col>

	        <el-col :span="2">
	            <el-tag class="role-tag" size="large" style="margin-top: 18px">{{ parseRole(role) }}版</el-tag>
	        </el-col>

	        <el-col :span="8"></el-col>
		    <el-col :span="2.75" class="header-icon">
			    <h1 class="header-text">{{currentTime}}</h1>
		    </el-col>

		    <el-col :span="1" class="logout">
			    <a @click="">
				    <router-link to="/home" v-slot="{navigate}" class="no-link">
					    <el-tooltip content="主页" effect="light">
					        <el-icon :size="50" color="white" style="margin: 5px 0 0 0;" ><House /></el-icon>
					    </el-tooltip>
				    </router-link>
			    </a>
		    </el-col>
	        <el-col :span="1" class="logout">
		        <a @click="">
			        <router-link to="/msg" v-slot="{navigate}" class="no-link">
				        <el-tooltip content="我的消息" effect="light">
					        <el-icon :size="50" color="white" style="margin: 5px 0 0 0;" ><Message /></el-icon>
				        </el-tooltip>
			        </router-link>
		        </a>
	        </el-col>
		    <el-col :span="1" class="logout">
			    <a @click="">
				    <router-link to="/favorite" v-slot="{navigate}" class="no-link">
					    <el-tooltip content="我的收藏" effect="light">
					        <el-icon :size="50" color="white" style="margin: 5px 0 0 0;" ><Star /></el-icon>
					    </el-tooltip>
				    </router-link>
			    </a>
		    </el-col>
		    <el-col :span="1" class="logout">
			    <a @click="">
				    <router-link to="/dashboard" v-slot="{navigate}" class="no-link">
					    <el-tooltip content="个人信息" effect="light">
				            <el-icon :size="50" color="white" style="margin: 5px 0 0 0;" ><User /></el-icon>
					    </el-tooltip>
				    </router-link>
			    </a>
		    </el-col>
		    <el-col :span="1" class="logout">
			    <a @click="logout">
				    <el-tooltip content="退出登录" effect="light">
				        <el-icon :size="50" color="white" style="margin: 5px 0 0 0;" ><SwitchButton /></el-icon>
				    </el-tooltip>
			    </a>
		    </el-col>

	    </el-row>
    </el-header>
</template>


<style scoped>
.custom-header {
	background-image: linear-gradient(to top left, #7fbcf8 0%, #0780fc 100%);
  //border-bottom-left-radius: 20px;
  //border-bottom-right-radius: 20px;

  display: flex;
  flex-direction: column;
}

.no-link {
  text-decoration: none;
}

.role-tag {
  margin-top: 20px;
  font-size: 20px;
}

.header-text {
  color:white;
  font-size: x-large;
  min-width: max-content;
  margin-top: 15px;
  margin-bottom: 15px;
}

.header-icon {
  display: flex;
  flex-direction: column;
  align-items:center;
  justify-content: center;
}

.logout {
	transition: transform 100ms ease-in-out;
}
.logout:hover {
	transform: scale(1.15);
}

</style>
